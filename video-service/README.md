# 🎞️ Video Service

> Microsserviço que gerencia **metadados e status** do processamento de vídeos: upload, listagem por usuário, detalhes e referência para download do ZIP. Atende aos requisitos de **listagem de status por usuário** e **não perder requisição em picos** (processamento enfileirado no RabbitMQ).

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-336791?logo=postgresql&logoColor=white)](https://www.postgresql.org/)

---

## Função

- **Upload** de vídeo (multipart); grava no volume compartilhado e publica pedido de processamento na fila RabbitMQ.
- **Listagem** por usuário (paginação) e **detalhes** por ID.
- **Resposta** com `zipPath` (caminho local ou URL S3) para download do ZIP quando o processamento conclui.
- **Consumo** da fila de eventos de conclusão (`video.processing.completed.video-service`) para atualizar status e `zipPath`.

Todas as chamadas passam pelo **API Gateway** com token e, quando aplicável, header **`X-User-Id`** ou query param **`userId`**.

## Endpoints (via gateway em `/api/videos/...`)

| Método | Rota | Acesso | Descrição |
|--------|------|--------|-----------|
| POST | `/api/videos` | Protegido + `X-User-Id` | Upload (multipart). Opcional: `frameIntervalSeconds`. Resposta 202 + body (id, status, createdAt). |
| GET | `/api/videos?userId=...&page=0&size=10` | Protegido | Listagem paginada por usuário. |
| GET | `/api/videos/{id}` | Protegido | Detalhes do vídeo (status, zipPath, etc.). |
| GET | `/api/videos/health` | Público | Health check (via gateway). |

## Swagger / OpenAPI

Com o serviço rodando, a documentação interativa está em:

- **Swagger UI:** **http://localhost:8082/swagger-ui.html** (acesso direto ao serviço).
- Via gateway: use a base **http://localhost:8080** e os paths `/api/videos/**`.

## Banco de dados e storage

- **PostgreSQL** (banco `video_db`). Migrations com **Flyway**. No Compose: `postgres-video`, porta 5434 no host.
- **Vídeos:** volume compartilhado `video_shared` (path `/shared/videos` no container), compartilhado com o processing-service.

## Configuração

- **Porta:** 8082.
- **Variáveis de ambiente:** `SPRING_DATASOURCE_*`, `SPRING_RABBITMQ_*`, `APP_STORAGE_TEMP_DIR` (no Compose: `/shared/videos`).

## Como rodar

Com a stack na raiz:

```bash
docker compose up -d
```

Ou build local:

```bash
docker compose -f compose.yml -f compose.dev.yml up -d
```

**Local (Maven):** exige PostgreSQL e RabbitMQ acessíveis.

```bash
mvn spring-boot:run
```

**Verificar banco (opcional):**

```bash
docker exec -it postgres-video psql -U video_user -d video_db
```

## Documentação relacionada

- **README raiz** — visão do projeto e como rodar a stack: [../README.md](../README.md).
- **QUEUES** — filas de processamento e eventos: [../QUEUES.md](../QUEUES.md).
- **Processing Service** — quem consome a fila e publica eventos: [../processing-service/README.md](../processing-service/README.md).
- **Guia de testes** — fluxo completo: [../TESTES-FLUXO-COMPLETO.md](../TESTES-FLUXO-COMPLETO.md).
