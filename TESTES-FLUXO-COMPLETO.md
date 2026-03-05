 # Guia de testes – fluxo completo do sistema de vídeos
 
 ## Visão geral do fluxo
 
 - **Entrada HTTP**: o cliente chama o **API Gateway** (`http://localhost:8080`).
 - **Autenticação**: o gateway roteia `/api/auth/**` para o **auth-service** (registro, login e validação de JWT).
 - **Vídeos**: o gateway roteia `/api/videos/**` para o **video-service** (upload, listagem, detalhes).
 - **Mensageria**: o `video-service` publica o pedido de processamento na exchange `video.processing.exchange` (RabbitMQ), com routing key `video.processing.requested`.
 - **Processamento**: o `processing-service` consome a fila principal (binding com a mesma routing key), lê o vídeo da **mesma pasta em disco** (`/shared/videos`) e processa com FFmpeg; em seguida publica eventos de sucesso/erro.
 - **Atualização**: o `video-service` consome eventos de conclusão e atualiza o status do vídeo no PostgreSQL.
 
 ---
 
 ## Pré‑requisitos
 
 - **Docker e Docker Compose** instalados e funcionando.
 - Porta **8080** livre (gateway) e, opcionalmente, 8081/8082/8083, 5434, 5672, 15672, 27017.
 - Um **arquivo de vídeo pequeno** local (por exemplo, `sample.mp4`) para testes.
 - Opcional: **Postman**, usando a collection do gateway:
   - `api-gateway/docs/Api-Gateway.postman_collection.json`
 
 ---
 
 ## Passo 1 – Subir toda a stack com Docker Compose
 
 Na raiz do projeto, onde está o arquivo `compose.yml`:
 
 ```bash
 docker compose up -d
 ```
 
 Isso deve subir pelo menos:
 
 - `api-gateway` (8080)
 - `auth-service` (8081)
 - `video-service` (8082)
 - `processing-service` (8083)
 - `rabbitmq` (5672 AMQP, 15672 console)
 - `postgres-video` (5434)
 - `mongodb` (27017)
 
 Os serviços `video-service` e `processing-service` compartilham o volume `./tmp/videos:/shared/videos`, para que o processamento leia os vídeos gravados pelo video-service.
 
 Verificações rápidas:
 
 ```bash
 curl http://localhost:8080/actuator/health         # API Gateway deve responder {"status":"UP"}
 curl http://localhost:8080/api/videos/health       # Health do video-service via gateway
 ```
 
 Também é útil acessar o console do RabbitMQ:
 
 - URL: `http://localhost:15672`
 - Usuário: `admin`
 - Senha: `admin123`
 
 (conforme definido em `compose.yml`).
 
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
 
 - Esperado: `200 OK` com resposta semelhante a:
 
 ```json
 {
   "token": "<JWT_AQUI>",
   "type": "Bearer",
   "expiresIn": 3600,
   "username": "postman_user"
 }
 ```
 
 - Guarde o campo `token` para usar nas chamadas protegidas:
   - Header: `Authorization: Bearer <JWT_AQUI>`
 
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
 
 - Esperado: `202 Accepted` sem body.
 - O `video-service` irá:
   - Armazenar o arquivo temporariamente no volume `./tmp/videos` (montado como `/shared/videos` no container).
   - Persistir os metadados do vídeo no PostgreSQL.
   - Publicar uma mensagem de **pedido de processamento** no RabbitMQ (exchange `video.processing.exchange`, routing key `video.processing.requested`), contendo `videoId`, `userId` e `videoPath` (caminho absoluto no container, ex.: `/shared/videos/<userId>/<uuid>_arquivo.mp4`).
 - O `processing-service` usa o **mesmo volume** (`./tmp/videos:/shared/videos` no `compose.yml`), portanto consegue ler o arquivo pelo mesmo `videoPath` recebido na mensagem.
 
 ### 3.2 Confirmar que o vídeo foi persistido
 
 ```bash
 curl "http://localhost:8080/api/videos?userId=<VIDEO_USER_ID>&page=0&size=10" \
   -H "Authorization: Bearer <JWT>"
 ```
 
 - Esperado: uma página contendo o novo vídeo, com:
   - Status inicial (por exemplo, `PENDING` ou `PROCESSING`, conforme o domínio).
   - Metadados (nome do arquivo, content-type, caminho, etc.).
 
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
 
 - **Produtor (`video-service`)**:
   - Exchange: `video.processing.exchange`
   - Routing key: `video.processing.requested`
   - Payload: `videoId`, `userId`, `videoPath` (caminho absoluto do vídeo no container, ex.: `/shared/videos/<userId>/<uuid>_arquivo.mp4`).
 - **Consumidor (`processing-service`)**:
   - Exchange: `video.processing.exchange`
   - Fila: `video.processing.queue`
   - Routing key do binding: `video.processing.requested` (alinhada ao produtor).
   - O processing-service aceita o campo `videoPath` da mensagem (mapeado para `inputLocation`) e usa `frameIntervalSeconds` opcional (default 1,0 s quando não enviado).
 
 Com a routing key e o contrato da mensagem alinhados, o fluxo é **automático**: assim que o video-service publica após o upload, o processing-service consome e processa o vídeo lendo-o da pasta compartilhada `/shared/videos`.
 
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
 
 ### Eventos de sucesso e falha
 
 Ao concluir, o `processing-service` publica eventos em outra exchange:
 
 - Exchange de eventos: `video.processing.events.exchange`
 - Em caso de sucesso:
   - Routing key: `video.processing.completed`
   - Fila: `video.processing.completed.processing-service`
 - Em caso de falha:
   - Routing key: `video.processing.failed`
   - Fila: `video.processing.failed.processing-service`
 
 E há uma Dead Letter Queue para mensagens que falham após tentativas:
 
 - Exchange DLQ: `video.processing.dlq.exchange`
 - Fila DLQ: `video.processing.dlq`
 
 Todas essas filas e exchanges estão descritas em `QUEUES.md`.
 
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
 
 - O status do vídeo deve ter mudado de `PENDING`/`PROCESSING` para:
   - `COMPLETED` (quando o processamento foi bem-sucedido), ou
   - `FAILED` (quando houve falha).
 - Em caso de sucesso, campos relacionados ao resultado (por exemplo, `zipPath`) devem estar preenchidos com a localização do ZIP.
 
 ### 6.2 Conferir diretamente no banco (opcional)
 
 Acesse o PostgreSQL do `video-service`:
 
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
 
 Para testar a robustez do fluxo e a DLQ:
 
 - Envie um vídeo inválido ou manipule o caminho de entrada de forma a causar erro no processamento.
 - Verifique, no console do RabbitMQ:
   - Mensagens em `video.processing.failed.processing-service`.
   - Mensagens redirecionadas para a fila DLQ `video.processing.dlq`.
 - Confirme via API (ou banco) que o `video-service` está refletindo o status de erro para o vídeo correspondente.
 
 ---
 
 ## Automação de testes end‑to‑end (opcional)
 
 A partir desse fluxo manual, você pode:
 
 - Transformar os cenários da collection do API Gateway em **testes automatizados de integração** usando, por exemplo, **Newman**:
   - Subir os serviços com `docker compose up -d`.
   - Rodar a collection do Postman (registro, login, upload, listagem).
 - Criar testes end‑to‑end que validem não só as respostas HTTP, mas também:
   - O estado das filas no RabbitMQ (quantidade de mensagens, DLQ).
   - O estado dos registros no PostgreSQL (`videos`).
 
 Isso consolida o fluxo completo passando por todos os serviços (gateway, auth-service, video-service, processing-service, RabbitMQ e PostgreSQL).
 
