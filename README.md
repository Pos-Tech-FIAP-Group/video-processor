# Video Processor - FIAP X

Sistema de Processamento de Vídeos com Arquitetura de Microsserviços.

## Estrutura padrão (Arquitetura Hexagonal)

Cada microsserviço segue o **mesmo padrão de pastas** para facilitar o trabalho em equipe. A estrutura já está criada; cada desenvolvedor implementa as classes na sua pasta.

**Leia primeiro:** [ARQUITETURA-HEXAGONAL.md](./ARQUITETURA-HEXAGONAL.md) — explica o que vai em cada pasta e as regras do padrão.

## Como rodar (Docker)

Na raiz do projeto há dois arquivos Compose:

- **`compose.yml`** — define a stack completa: api-gateway, auth-service, video-service e processing-service usam **imagens pré-construídas** no GitHub Container Registry (GHCR). O web-app é buildado localmente; RabbitMQ, MongoDB, Redis e Postgres usam imagens públicas.
- **`compose.dev.yml`** — arquivo de **override**: não substitui o `compose.yml`, e sim é carregado **em conjunto** com ele. Quando usado, adiciona `build` local aos quatro microsserviços e define `pull_policy: never`, dispensando o download de imagens do GHCR para desenvolvimento.

A ordem dos arquivos importa: o segundo sobrescreve/estende o primeiro.

### Dois modos de uso

**1. Rodar com imagens do GHCR** (recomendado para quem só vai subir a stack):

```bash
docker compose up -d
```

Exige estar logado no GHCR para o pull das imagens dos quatro serviços (api-gateway, auth-service, video-service, processing-service). Veja a seção [Autenticação no GHCR](#autenticação-no-github-container-registry-ghcr) abaixo.

**2. Rodar em modo desenvolvimento** (build local dos microsserviços):

```bash
docker compose -f compose.yml -f compose.dev.yml up -d
```

Os quatro microsserviços são buildados a partir do código local; **não é necessário** pull do GHCR para eles. Útil para quem altera código e quer testar sem publicar imagem.

### Autenticação no GitHub Container Registry (GHCR)

As imagens dos serviços estão em `ghcr.io/pos-tech-fiap-group/video-processor-*:latest`. Se o repositório de packages for privado, `docker compose up -d` (sem o override dev) falha no pull sem autenticação.

**Criar um Personal Access Token (PAT) no GitHub:**

- Acesse **Settings → Developer settings → Personal access tokens** (classic ou fine-grained).
- **Classic:** use o escopo `read:packages` (e `write:packages` se for fazer push de imagens).
- **Fine-grained:** conceda permissão de leitura nos packages da organização/repositório.

**Fazer login no GHCR:**

```bash
echo "<SEU_TOKEN>" | docker login ghcr.io -u <SEU_USUARIO_GITHUB> --password-stdin
```

Ou usando variável de ambiente (recomendado; **não coloque o token no README ou no repositório**):

```bash
echo "$GITHUB_TOKEN" | docker login ghcr.io -u <USUARIO> --password-stdin
```

Defina `GITHUB_TOKEN` no seu ambiente ou em um arquivo `.env` na raiz (e garanta que `.env` está no `.gitignore`).

**Resumo:** sem login no GHCR, use o modo desenvolvimento (`-f compose.yml -f compose.dev.yml`) para subir os quatro serviços via build local. Com login no GHCR, `docker compose up -d` consegue baixar as imagens do registry.

### Serviços disponíveis

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

O `compose.yml` lê variáveis do ambiente ou de um arquivo `.env` na raiz. Para autenticação no GHCR (modo 1), você pode usar `GITHUB_TOKEN` no `.env` apenas como referência para o comando `docker login` — não exponha o valor no repositório.

Para o **processing-service** enviar os zips de frames para o **S3** (em vez de só guardar em disco), defina:

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
## Teste de carga com k6

Foi adicionada uma estrutura dedicada de carga em `load-tests/`.

### Windows (PowerShell)

Execucao basica com upload de video:

```powershell
./load-tests/k6/run-k6.ps1 `
  -VideoFile "C:\caminho\para\sample.mp4"
```

Exemplo com mais carga:

```powershell
./load-tests/k6/run-k6.ps1 `
  -VideoFile "C:\caminho\para\sample.mp4" `
  -UploadVus 5 `
  -UploadDuration "1m"
```

### Linux / Ubuntu (bash)

Primeira vez, torne o script executavel:

```bash
chmod +x load-tests/k6/run-k6.sh
```

Execucao basica com upload de video:

```bash
./load-tests/k6/run-k6.sh \
  -VideoFile "/caminho/para/sample.mp4"
```

Exemplo com mais carga:

```bash
./load-tests/k6/run-k6.sh \
  -VideoFile "/caminho/para/sample.mp4" \
  -UploadVus 5 \
  -UploadDuration "1m"
```

Relatorios gerados por execucao (em ambos os scripts):

- `load-tests/reports/k6-<timestamp>-raw.json`
- `load-tests/reports/k6-<timestamp>-summary.json`
- `load-tests/reports/k6-<timestamp>.html`

Mais detalhes (parametros suportados, observacoes) estao em `load-tests/README.md`.
