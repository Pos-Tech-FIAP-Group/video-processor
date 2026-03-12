# Video Processor - FIAP X

Sistema de Processamento de Vídeos com Arquitetura de Microsserviços.

## Estrutura padrão (Arquitetura Hexagonal)

Cada microsserviço segue o **mesmo padrão de pastas** para facilitar o trabalho em equipe. A estrutura já está criada; cada desenvolvedor implementa as classes na sua pasta.

**Leia primeiro:** [ARQUITETURA-HEXAGONAL.md](./ARQUITETURA-HEXAGONAL.md) — explica o que vai em cada pasta e as regras do padrão.

## Como rodar (Docker)

Na raiz do projeto:

```bash
docker compose up -d
```

Serviços disponíveis:

| Serviço            | Porta | URL base              |
|--------------------|-------|------------------------|
| Web App (UI)       | 4200  | http://localhost:4200  |
| API Gateway        | 8080  | http://localhost:8080  |
| Auth Service       | 8081  | http://localhost:8081  |
| Video Service      | 8082  | (via gateway)          |
| Processing Service | 8083  | (interno; sem API REST, só fila RabbitMQ) |
| RabbitMQ            | 5672 / 15672 | (interno / management) |
| MongoDB             | 27017 | (interno ao auth)      |

Todas as chamadas do cliente devem ir pelo **gateway** (8080). O gateway encaminha `/api/auth/**` para o auth-service e `/api/videos/**` para o video-service. Processing e notification são serviços internos, chamados por outros microsserviços — não possuem rota no gateway.

### Variáveis de ambiente (Docker Compose)

O `compose.yml` lê variáveis do ambiente ou de um arquivo `.env` na raiz. Para o **processing-service** enviar os zips de frames para o **S3** (em vez de só guardar em disco), defina:

| Variável | Descrição |
|----------|-----------|
| `AWS_ACCESS_KEY_ID` | Access key do usuário IAM com permissão de escrita no bucket |
| `AWS_SECRET_ACCESS_KEY` | Secret da access key |
| `AWS_REGION` | Região do bucket (ex.: `sa-east-1`) |
| `PROCESSING_STORAGE_S3_BUCKET` | Nome do bucket (ex.: `video-processor-zip-artifacts`) |

Se essas variáveis não forem definidas, o processing-service continua funcionando: o zip é gerado em disco e o caminho local é publicado na fila (o video-service e a UI podem usar o endpoint de download do backend). Com S3 configurado, o zip é enviado ao bucket (organizado por user UUID: `{userUuid}/{videoId}.zip`), a URL pública é publicada na fila e a UI pode baixar direto pelo link. Detalhes da configuração do bucket (leitura pública, escrita restrita) e do fluxo estão em [processing-service/README.md](processing-service/README.md#armazenamento-s3-opcional).

## Microsserviços

| Serviço              | Pasta                    |
|----------------------|---------------------------|
| API Gateway          | `api-gateway/`            |
| Auth Service         | `auth-service/`           |
| Video Service        | `video-service/`          |
| Processing Service   | `processing-service/`    |
| Notification Service | `notification-service/`  |

---

### API Gateway (`api-gateway/`)

- **Porta:** 8080  
- **Função:** Ponto de entrada único. Roteia requisições e valida JWT nas rotas protegidas.
- **Rotas expostas:**
  - `/api/auth/**` → auth-service (8081)
  - `/api/videos/**` → video-service (8082), quando o serviço estiver no compose
- **Rotas públicas (sem token):** `/api/auth/register`, `/api/auth/login`, `/api/auth/validate`, `/actuator/**`
- **Rotas protegidas:** exigem header `Authorization: Bearer <token>` (ex.: `/api/auth/users/{id}`)
- **Configuração:** Um único `application.yml`; a URL do auth-service é definida no entrypoint (Docker). Não há suporte a execução local — uso apenas via Docker.
- **Postman:** [api-gateway/docs/Api-Gateway.postman_collection.json](./api-gateway/docs/Api-Gateway.postman_collection.json) — importe no Postman para testar registro, login, validação e rotas protegidas via gateway.

### Auth Service (`auth-service/`)

- **Porta:** 8081  
- **Função:** Registro de usuários, login (JWT), validação de token e consulta de usuário por ID.
- **Banco:** MongoDB (serviço `mongodb` no compose, URI `mongodb://mongodb:27017/auth_db`).
- **Endpoints (via gateway em `/api/auth/...`):**
  - `POST /api/auth/register` — criar usuário (público)
  - `POST /api/auth/login` — obter token (público)
  - `GET /api/auth/validate?token=...` — validar JWT (público)
  - `GET /api/auth/users/{id}` — buscar usuário (protegido)
- **Configuração:** Um único `application.yml` para Docker (MongoDB pelo nome do serviço). Sem perfil local.
- **Postman:** [auth-service/docs/Auth-Service.postman_collection.json](./auth-service/docs/Auth-Service.postman_collection.json) — testes diretos no auth-service (porta 8081). Para testar pelo gateway, use a collection do API Gateway.

### Video Service e Processing Service

- **Video Service:** ver `video-service/README.md` (upload de vídeo, listagem, download do ZIP, consumo da fila de processamento concluído).
- **Processing Service:** ver `processing-service/README.md` (processamento via fila RabbitMQ com FFmpeg, upload opcional para S3; **sem API REST**).