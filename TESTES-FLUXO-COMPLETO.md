# Guia de testes – fluxo completo do sistema de vídeos

## Visão geral do fluxo

- **Entrada HTTP**: o cliente chama o **API Gateway** (`http://localhost:8080`) ou usa o **Web App** (`http://localhost:4200`), que também fala com o gateway.
- **Autenticação**: o gateway roteia `/api/auth/**` para o **auth-service** (registro, login e validação de JWT).
- **Vídeos**: o gateway roteia `/api/videos/**` para o **video-service** (upload, listagem, detalhes).
- **Mensageria**: o `video-service` publica o pedido de processamento na exchange `video.processing.exchange` (RabbitMQ), routing key `video.processing.requested`.
- **Processamento**: o `processing-service` consome a fila `video.processing.queue`, lê o vídeo do **volume compartilhado** (`/shared/videos` no container) e processa com FFmpeg; depois publica eventos na exchange `video.processing.events.exchange` (routing key `video.processing.completed`, com campo `success` no payload). Não expõe API REST.
- **Atualização**: o `video-service` consome da fila `video.processing.completed.video-service` e atualiza o status do vídeo no PostgreSQL.
- **Notificação em erro**: o `notification-service` consome da fila `video.processing.notification-service.queue`. Quando `success=false`, obtém o e-mail no auth-service (rota interna `/internal/users/by-uuid/{uuid}`) e envia e-mail via SMTP. Em dev os e-mails chegam no **Mailtrap** (caixa do serviço), não no e-mail real do usuário.

---

## Pré-requisitos

- **Docker e Docker Compose** instalados e funcionando.
- Portas livres: **8080** (gateway), **4200** (web-app) e, opcionalmente, 8081/8082/8083/8084, 5434, 5672, 15672, 27017.
- Um **arquivo de vídeo** local (ex.: `sample.mp4`) para testes de upload.
- **Opção 1 – Postman:** use a collection de fluxo completo na raiz do projeto:
  - **`resources/Fluxo-Completo.postman_collection.json`** (registro → login → upload → listagem)
  - Ou a collection do gateway: **`api-gateway/docs/Api-Gateway.postman_collection.json`**
- **Opção 2 – Web App:** após subir a stack, acesse **http://localhost:4200** e use a interface (login, upload, listagem, detalhe). Ver [web-app/README.md](web-app/README.md).
 
---

## Passo 1 – Subir toda a stack com Docker Compose

Na raiz do projeto:

```bash
docker compose up -d
```

Se as imagens dos microsserviços não estiverem no GHCR ou você quiser build local, use:

```bash
docker compose -f compose.yml -f compose.dev.yml up -d
```

A stack sobe:

- **api-gateway** (8080)
- **auth-service** (8081)
- **video-service** (8082)
- **processing-service** (8083)
- **notification-service** (8084)
- **web-app** (4200) — interface Angular
- **rabbitmq** (5672 AMQP, 15672 console)
- **postgres-video** (5434 no host, 5432 no container)
- **mongodb** (27017)

O `video-service` e o `processing-service` compartilham o **volume nomeado** `video_shared`, montado como `/shared/videos` dentro dos containers, para leitura/gravação dos vídeos e ZIPs.

**Verificações rápidas:**

```bash
curl http://localhost:8080/actuator/health
# Resposta esperada: {"status":"UP"} ou similar

curl http://localhost:8080/api/videos/health
# Health do video-service via gateway
```

**Console do RabbitMQ:** `http://localhost:15672` — usuário `admin`, senha `admin123` (valores default do `compose.yml`).
 
 ---
 
 ## Passo 2 – Exercitar o fluxo de autenticação via API Gateway
 
 Use sempre o **gateway** como ponto de entrada: `http://localhost:8080`.
 Você pode usar `curl` ou a collection do Postman do API Gateway.
 
 ### 2.1 Registrar usuário
 
 ```bash
 curl -X POST http://localhost:8080/api/auth/register \
   -H "Content-Type: application/json" \
   -d '{
     "username": "postman_user",
     "email": "postman@example.com",
     "password": "senha123"
   }'
 ```
 
 - Esperado: `201 Created` com JSON do usuário.
 - Guarde:
   - `id` (para possíveis chamadas a `/api/auth/users/{id}`).
   - `userUuid` (na collection do gateway ele é usado como `videoUserId` e como `X-User-Id` no upload de vídeos).
 
 ### 2.2 Login para obter JWT
 
 ```bash
 curl -X POST http://localhost:8080/api/auth/login \
   -H "Content-Type: application/json" \
   -d '{
     "username": "postman_user",
     "password": "senha123"
   }'
 ```
 
 - Esperado: `200 OK` com resposta no formato:
 
 ```json
 {
   "token": "<JWT_AQUI>",
   "type": "Bearer",
   "expiresIn": 86400000,
   "username": "postman_user",
   "userId": "<id_mongo>",
   "userUuid": "<uuid_do_usuario>"
 }
 ```
 
 - O campo `expiresIn` está em **milissegundos** (ex.: 86400000 = 24 h). Use o `token` no header `Authorization: Bearer <JWT_AQUI>` e o `userUuid` como `X-User-Id` no upload e como `userId` na listagem de vídeos.
 
 ### 2.3 (Opcional) Validar token
 
 ```bash
 curl "http://localhost:8080/api/auth/validate?token=<JWT_AQUI>"
 ```
 
 - Esperado: JSON com `valid = true` e o `subject` correspondente ao usuário.
 
 ---
 
 ## Passo 3 – Upload de vídeo via API Gateway (Video Service)
 
 Suponha:
 
 - `VIDEO_USER_ID` = `userUuid` retornado pelo `auth-service` (ou `videoUserId` atribuído pela collection).
 - `JWT` = token retornado no login.
 - `CAMINHO_VIDEO` = caminho local do arquivo de vídeo (por exemplo, `/Users/seuuser/Videos/sample.mp4`).
 
 ### 3.1 Fazer upload do vídeo
 
 Endpoint via gateway:
 
 ```bash
 curl -X POST http://localhost:8080/api/videos \
   -H "Authorization: Bearer <JWT>" \
   -H "X-User-Id: <VIDEO_USER_ID>" \
   -F "file=@<CAMINHO_VIDEO>"
 ```
 
 - Esperado: `202 Accepted` com body JSON contendo o vídeo criado (ex.: `id`, `status`, `createdAt`).
 - O `video-service` irá:
   - Gravar o arquivo no volume compartilhado `video_shared` (path `/shared/videos` no container).
   - Persistir os metadados no PostgreSQL.
   - Publicar uma mensagem no RabbitMQ (exchange `video.processing.exchange`, routing key `video.processing.requested`) com `videoId`, `userId`, `videoPath` (path absoluto no container, ex.: `/shared/videos/<userId>/<uuid>_arquivo.mp4`) e opcionalmente `frameIntervalSeconds`.
 - O `processing-service` usa o **mesmo volume** `video_shared` (`/shared/videos`), então lê o arquivo pelo `videoPath` da mensagem.
 
 ### 3.2 Confirmar que o vídeo foi persistido
 
 ```bash
 curl "http://localhost:8080/api/videos?userId=<VIDEO_USER_ID>&page=0&size=10" \
   -H "Authorization: Bearer <JWT>"
 ```
 
 - Esperado: página com o novo vídeo; status inicial normalmente `PENDING` ou `PROCESSING`; metadados (nome, contentType, etc.).
 
 Opcionalmente, é possível buscar por ID específico:
 
 ```bash
 curl "http://localhost:8080/api/videos/<VIDEO_ID>" \
   -H "Authorization: Bearer <JWT>"
 ```
 
 ---
 
 ## Passo 4 – Conferir publicação da mensagem no RabbitMQ
 
 Acesse o console do RabbitMQ em `http://localhost:15672` e vá até:
 
 - Aba **Exchanges** → procure `video.processing.exchange`.
 
 Verifique os bindings e filas relevantes (detalhados em `QUEUES.md`):
 
 - **Produtor (video-service):** exchange `video.processing.exchange`, routing key `video.processing.requested`. Payload: `videoId`, `userId`, `videoPath` (path no container, ex.: `/shared/videos/<userId>/<uuid>_arquivo.mp4`), `frameIntervalSeconds` (opcional, default 1,0 s).
 - **Consumidor (processing-service):** fila `video.processing.queue` (binding `video.processing.requested`). Lê o vídeo em `/shared/videos` (volume `video_shared`).
 
 Detalhes das filas e exchanges em [QUEUES.md](QUEUES.md).
 
 ---
 
 ## Passo 5 – Validar consumo pelo Processing Service
 
 Após o upload (Passo 3), a mensagem publicada pelo video-service deve ser consumida automaticamente pelo processing-service:
 
 - Fila principal: `video.processing.queue` (binding com routing key `video.processing.requested`).
 
 No console do RabbitMQ:
 
 - Aba **Queues** → clique em `video.processing.queue`:
   - Verifique se há consumidores ativos (o `processing-service`).
   - Acompanhe o count de mensagens prontas/consumidas.
 
 Você também pode acompanhar os logs do container:
 
 ```bash
 docker logs -f processing-service
 ```
 
 O esperado nos logs:
 
 - Indicação de que uma mensagem de pedido de processamento foi recebida (incluindo `videoId`, caminho do vídeo, etc.).
 - Execução do processamento (FFprobe/FFmpeg, extração de frames, criação do ZIP).
 - Em caso de falha, mensagens de erro e possíveis redirecionamentos para DLQ.
 
 ### Eventos de conclusão
 
 O `processing-service` publica **sempre** na exchange `video.processing.events.exchange` com routing key `video.processing.completed`. O payload inclui o campo **`success`** (true/false). Duas filas consomem:
 
 - **video.processing.completed.video-service** — o `video-service` atualiza o status do vídeo (e `zipPath` em sucesso).
 - **video.processing.notification-service.queue** — o `notification-service` só age quando `success=false` (busca e-mail no auth e envia notificação).
 
 **DLQ:** mensagens da fila principal que falham após retentativas vão para `video.processing.dlq` (exchange `video.processing.dlq.exchange`). Ver [QUEUES.md](QUEUES.md).
 
 ---
 
 ## Passo 6 – Confirmar atualização de status no Video Service
 
 Após o processamento (real ou simulado):
 
 ### 6.1 Consultar via API (gateway)
 
 ```bash
 curl "http://localhost:8080/api/videos?userId=<VIDEO_USER_ID>&page=0&size=10" \
   -H "Authorization: Bearer <JWT>"
 ```
 
 ou:
 
 ```bash
 curl "http://localhost:8080/api/videos/<VIDEO_ID>" \
   -H "Authorization: Bearer <JWT>"
 ```
 
 Esperado:
 
 - Status do vídeo: `COMPLETED` (sucesso) ou `FAILED` (erro). Em sucesso, o campo `zipPath` traz o caminho do ZIP (ou URL se S3 estiver configurado).

 ### 6.2 Conferir no banco (opcional)
 
 ```bash
 docker exec -it postgres-video psql -U video_user -d video_db
 ```
 
 Dentro do `psql`, inspecione a tabela de vídeos:
 
 ```sql
 SELECT * FROM videos;
 ```
 
 Verifique:
 
 - Status do vídeo.
 - Campos de caminho do ZIP / resultado do processamento.
 
 ---
 
 ## Passo 7 – Explorar casos de erro e DLQ
 
 Para testar erro e DLQ:
 
 - Envie um vídeo inválido ou provoque falha no processamento.
 - No RabbitMQ: verifique a fila DLQ `video.processing.dlq` (mensagens que falharam após retentativas).
 - Via API ou banco: o vídeo deve constar com status de falha no `video-service`.

---

## Notificação em erro (notification-service)

Quando o processamento **falha**, o **notification-service** é acionado automaticamente:

1. O `processing-service` publica na exchange `video.processing.events.exchange` (routing key `video.processing.completed`) com `success=false` e `userId` no payload.
2. O `notification-service` consome da fila `video.processing.notification-service.queue`, vê `success=false`, chama o auth-service em **`GET /internal/users/by-uuid/{uuid}`** para obter o e-mail e envia o e-mail via SMTP.

**Como validar:** provoque uma falha (ex.: vídeo inválido). Os e-mails são enviados para o **Mailtrap** (variáveis `MAIL_*` no `compose.yml`); confira a inbox em [mailtrap.io](https://mailtrap.io). Eles **não** chegam no e-mail real do usuário. O notification-service não expõe API REST.

---
 
## Automação de testes end-to-end (opcional)

- Use **Newman** para rodar a collection **`resources/Fluxo-Completo.postman_collection.json`** com a stack no ar (`docker compose up -d`).
- Testes E2E podem validar respostas HTTP, estado das filas no RabbitMQ e registros no PostgreSQL (`videos`), cobrindo gateway, auth-service, video-service, processing-service, notification-service, RabbitMQ e PostgreSQL.
 
