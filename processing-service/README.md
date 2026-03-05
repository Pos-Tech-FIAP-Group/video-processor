# Processing Service

Microsserviço responsável por **processar vídeos** (extração de frames) no sistema Video Processor FIAP X. Não persiste dados: consome mensagens da fila, processa com FFmpeg e publica o resultado na fila `video.processing.completed.processing-service` para o **video-service** consumir.

## Arquitetura

- **Arquitetura hexagonal** (core + adapters), conforme [ARQUITETURA-HEXAGONAL.md](../ARQUITETURA-HEXAGONAL.md) do projeto.
- **Sem persistência**: o serviço é stateless em relação a banco de dados; apenas processa e envia eventos na fila.
- **Strategy pattern** para múltiplos formatos de vídeo (MP4, AVI, MOV, etc.) via FFmpeg.
- **Paralelismo**: consumidores RabbitMQ configurados (5–20 concorrentes) para processar vários vídeos ao mesmo tempo.

## Fluxo

1. **Entrada**: o processing-service consome mensagens da fila de processamento (exchange `video.processing.exchange`, routing key `video.processing.request`, queue `video.processing.queue`).
2. **Processamento**:
   - Valida `frameIntervalSeconds` contra a duração do vídeo (FFprobe).
   - Detecta o formato do vídeo (extensão ou FFprobe).
   - Escolhe a strategy por formato e executa extração de frames (FFmpeg) e geração do ZIP.
3. **Saída**:
   - **Sucesso**: publica mensagem na fila **`video.processing.completed.processing-service`** (exchange `video.processing.events.exchange`, routing key `video.processing.completed`) com `videoId`, `resultLocation`, `frameIntervalSeconds`, `processedAt`. O **video-service** deve consumir essa fila para atualizar status e `zipPath` do vídeo.
   - **Falha**: publica mensagem na fila **`video.processing.failed.processing-service`** (routing key `video.processing.failed`) com `videoId`, `errorMessage`, `failedAt` para notificação/retry.

## Filas RabbitMQ

| Fila / Exchange | Uso |
|-----------------|-----|
| `video.processing.exchange` + `video.processing.queue` | Entrada: requisições de processamento (consumidas por este serviço). |
| `video.processing.events.exchange` + **`video.processing.completed.processing-service`** | Saída: processamento concluído (consumida pelo **video-service**). |
| `video.processing.events.exchange` + `video.processing.failed.processing-service` | Saída: processamento com erro (consumida pelo **notification-service** ou video-service). |
| `video.processing.dlq` | Dead letter para mensagens que falharam após retries. |

## Formato da mensagem de conclusão (video.processing.completed.processing-service)

O payload publicado na fila de conclusão segue o formato esperado pelo video-service (JSON), por exemplo:

```json
{
  "videoId": "uuid-do-video",
  "status": "COMPLETED",
  "resultLocation": "/caminho/ou/uri/do/zip",
  "frameIntervalSeconds": 1.0,
  "processedAt": "2025-02-19T12:00:00"
}
```

O **video-service** deve consumir `video.processing.completed.processing-service`, atualizar o vídeo correspondente (ex.: status CONCLUIDO, `zipPath` = `resultLocation`) e permitir download do ZIP.

## Pré-requisitos

- **Java 21**
- **FFmpeg** (e FFprobe) instalado e no `PATH` (no container, a imagem do Docker já inclui FFmpeg).
- **RabbitMQ** acessível (host/port/usuário/senha configurados).

## Configuração

Variáveis de ambiente principais:

| Variável | Descrição | Default |
|----------|-----------|---------|
| `SPRING_RABBITMQ_HOST` | Host do RabbitMQ | `localhost` |
| `SPRING_RABBITMQ_PORT` | Porta AMQP | `5672` |
| `SPRING_RABBITMQ_USERNAME` | Usuário | `admin` |
| `SPRING_RABBITMQ_PASSWORD` | Senha | `admin123` |
| `PROCESSING_STORAGE_BASE_PATH` | Diretório base para vídeos (paths da API são relativos a ele) | `/data` |

Demais opções (exchanges, queues, concorrência) estão em `src/main/resources/application.yml`.

## Como rodar

### Local (com RabbitMQ na máquina ou em container)

```bash
# Na raiz do repositório
mvn -pl processing-service spring-boot:run
```

### Com Docker Compose (projeto principal)

Na raiz do repositório:

```bash
docker compose up -d rabbitmq
docker compose up --build processing-service
```

O `compose.yml` do projeto já define RabbitMQ e processing-service; o processing-service depende apenas do RabbitMQ (sem PostgreSQL/Redis).

### Testes

```bash
mvn -pl processing-service test
```

Testes de integração usam **Testcontainers** para subir um RabbitMQ efêmero.

## API REST – Endpoint e como testar

| Método | Path | Descrição |
|--------|------|-----------|
| `POST` | `/api/process/extract-frames` | Extrai frames do vídeo em um intervalo configurável e gera um ZIP no mesmo diretório. |

**Request (JSON):**
- `videoPath` (obrigatório): path **relativo ao diretório base** (ex.: `/data`). Aceita `video.mp4`, `video1/video.mp4`, com ou sem barra no início — o serviço resolve sempre sob o base path.
- `frameIntervalSeconds` (opcional): intervalo em segundos entre um frame e outro. Default `1.0`. Se informado, deve ser > 0.

```json
{"videoPath": "video.mp4"}
{"videoPath": "video1/video.mp4", "frameIntervalSeconds": 2.0}
```

**Response 200:** `{"zipPath": "/data/video_frames.zip"}`

**Exemplo curl** (base path `/data`, volume `./storage:/data`):
```bash
curl -X POST http://localhost:8082/api/process/extract-frames \
  -H "Content-Type: application/json" \
  -d '{"videoPath": "video.mp4", "frameIntervalSeconds": 2.0}'
```

## Integração com o Video Service

- O **video-service** persiste os vídeos (ex.: PostgreSQL), controla status (PENDENTE, PROCESSANDO, CONCLUIDO, ERRO) e expõe API para listar e fazer download.
- Para disparar o processamento, o video-service (ou outro serviço) deve **publicar** uma mensagem na exchange/fila de entrada do processing-service (ex.: `video.processing.exchange` + routing key `video.processing.request`), com payload contendo pelo menos: `videoId`, `inputLocation`, `frameIntervalSeconds`, `userId`, e opcionalmente `format`.
- Ao concluir, o processing-service publica na **`video.processing.completed.processing-service`**. O video-service deve ter um **consumer** dessa fila para atualizar o vídeo (status CONCLUIDO, `zipPath` = `resultLocation`) e, se aplicável, mover/copiar o ZIP para o storage definitivo.

## Estrutura do serviço (resumo)

```
processing-service/
├── core/                    # Regras de negócio
│   ├── domain/              # VideoFormat, VideoProcessingRequest, VideoDuration, ProcessingResult
│   └── application/         # ProcessVideoUseCase, ports (VideoProcessingStrategyPort, VideoFormatDetectorPort, VideoMetadataPort, ProcessingEventPublisherPort)
├── adapters/
│   ├── driven/infra/        # FFmpeg strategies, detector, metadata, RabbitMqProcessingEventPublisher
│   └── driver/api/          # RabbitMqConfig, VideoProcessingConsumer, DTOs, exception handler
├── src/main/resources/
│   └── application.yml
├── Dockerfile
├── pom.xml
└── README.md (este arquivo)
```

## Observação sobre persistência

Este serviço **não utiliza banco de dados**. Todo estado do vídeo (status, zipPath, etc.) fica no **video-service**. Se no futuro for necessário manter algum estado local no processador (ex.: fila de reprocessamento, métricas por worker), pode-se avaliar a inclusão de persistência (por exemplo com **MongoDB**) apenas para esse fim, mantendo a regra de que “quem persiste o resultado do processamento é o video-service”.
