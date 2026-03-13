# ⚙️ Processing Service

> Worker responsável por **processar vídeos** (extração de frames com FFmpeg) no Video Processor. Atende aos requisitos de **processar mais de um vídeo ao mesmo tempo** (múltiplos consumers) e **não perder requisição em picos** (fila durável). **Não expõe API REST** — entrada só via fila RabbitMQ; publica eventos de conclusão para o video-service e notification-service.

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Messaging-FF6600?logo=rabbitmq&logoColor=white)](https://www.rabbitmq.com/)

---

## Arquitetura

- **Arquitetura hexagonal** (core + adapters); ver seção "Padrão interno: Arquitetura Hexagonal" em [ARQUITETURA-DO-PROJETO.md](../ARQUITETURA-DO-PROJETO.md).
- **Sem persistência:** stateless; apenas consome, processa e publica eventos.
- **Strategy pattern** para formatos de vídeo (MP4, AVI, MOV, etc.) via FFmpeg.
- **Paralelismo:** 5–20 consumers RabbitMQ (configurável no `application.yml`).

## Fluxo

1. **Entrada:** consome da fila `video.processing.queue` (exchange `video.processing.exchange`, routing key `video.processing.requested`). Lê o vídeo do volume compartilhado `/shared/videos`.
2. **Processamento:** valida `frameIntervalSeconds`, detecta formato (FFprobe), extrai frames (FFmpeg) e gera ZIP.
3. **Saída:** publica na exchange `video.processing.events.exchange`:
   - **Sucesso:** routing key `video.processing.completed`, payload com `success: true`, `videoId`, `frameCount`, `zipPath` (local ou URL S3). O **video-service** consome da fila `video.processing.completed.video-service` e atualiza status e `zipPath`.
   - **Falha:** routing key `video.processing.completed` com `success: false` (ou `video.processing.failed`, conforme config). O **notification-service** consome e envia e-mail quando `success === false`.

> Detalhes das filas e bindings em [QUEUES.md](../QUEUES.md).

## Filas RabbitMQ

| Fila / Exchange | Uso |
|-----------------|-----|
| `video.processing.exchange` + `video.processing.queue` | Entrada: requisições de processamento (consumidas por este serviço). |
| `video.processing.events.exchange` | Saída: eventos de conclusão (sucesso/falha). Video-service e notification-service têm filas próprias com binding nessa exchange. |
| `video.processing.dlq` | Dead letter para mensagens que falharam após retries. |

## Formato da mensagem de conclusão

Exemplo de payload publicado (JSON):

```json
{
  "videoId": "uuid-do-video",
  "success": true,
  "frameCount": 10,
  "zipPath": "https://bucket.s3.region.amazonaws.com/{userUuid}/{videoId}.zip"
}
```

Sem S3, `zipPath` é o caminho local do ZIP. O **video-service** atualiza o vídeo (status CONCLUIDO, `zip_path`) e a UI usa `zipPath` para download.

## Armazenamento S3 (opcional)

Com variáveis de ambiente de S3 definidas (`PROCESSING_STORAGE_S3_BUCKET`, `AWS_REGION`, credenciais), o ZIP é enviado ao bucket e a **URL pública** é publicada na fila. Chave no S3: `{userUuid}/{videoId}.zip`.

| Variável | Descrição |
|----------|-----------|
| `PROCESSING_STORAGE_S3_BUCKET` | Nome do bucket. |
| `AWS_REGION` | Região (ex.: `sa-east-1`). |
| `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` | Credenciais IAM com permissão de escrita. |

Sem S3, o serviço publica o caminho local; o video-service/UI usam o endpoint de download do backend. Configuração do bucket (leitura pública, IAM) em detalhe no [README raiz](../README.md#-variáveis-de-ambiente-docker-compose).

## Configuração

| Variável | Descrição | Default |
|----------|-----------|---------|
| `SPRING_RABBITMQ_HOST` / `PORT` / `USERNAME` / `PASSWORD` | RabbitMQ | `localhost`, `5672`, `admin`, `admin123` |
| `PROCESSING_STORAGE_BASE_PATH` | Diretório base para vídeos e ZIPs | `/data` (no Compose: `/shared/videos`) |

Exchanges, queues e concorrência em `src/main/resources/application.yml`.

## Como rodar

**Com Docker Compose (raiz do projeto):**

```bash
docker compose up -d
```

Ou build local dos serviços:

```bash
docker compose -f compose.yml -f compose.dev.yml up -d
```

**Local (Maven):** RabbitMQ e FFmpeg no PATH.

```bash
mvn -pl processing-service spring-boot:run
```

**Testes:**

```bash
mvn -pl processing-service test
```

(Testcontainers para RabbitMQ.)

## Sem API REST / Postman

Este serviço não expõe endpoints HTTP. Não há collection Postman; o fluxo é testado via upload no gateway/web-app e verificação de eventos e status no video-service.

## Documentação relacionada

- **README raiz** — visão do projeto e variáveis S3: [../README.md](../README.md).
- **QUEUES** — filas, exchanges e bindings: [../QUEUES.md](../QUEUES.md).
- **Video Service** — quem publica o pedido e consome a conclusão: [../video-service/README.md](../video-service/README.md).
- **Notification Service** — consome eventos de falha: [../notification-service/README.md](../notification-service/README.md).
