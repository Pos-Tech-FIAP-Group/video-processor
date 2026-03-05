### Apanhado das filas

#### Tecnologias

- **Mensageria**: RabbitMQ (Spring AMQP)

---

### Filas, serviços e parâmetros

#### 1. `video.processing.queue` (fila principal de processamento)

- **Tecnologia**: RabbitMQ  
- **Serviço(s)**:
  - **`processing-service`**: consumidor
- **Parâmetros**:
  - **Exchange**: `video.processing.exchange` (Topic)  
  - **Fila**: `video.processing.queue`  
  - **Routing key (binding)**: `video.processing.request`  
  - **DLQ**:
    - `x-dead-letter-exchange = video.processing.dlq.exchange`
    - `x-dead-letter-routing-key = video.processing.dlq`
  - **Listener (container)**:
    - `minConcurrentConsumers = 5`, `maxConcurrentConsumers = 20`
    - `prefetchCount = 10`
    - `defaultRequeueRejected = false`

---

#### 2. Publicação de pedido de processamento (`video.processing.requested`)

- **Tecnologia**: RabbitMQ  
- **Serviço(s)**:
  - **`video-service`**: produtor
- **Parâmetros**:
  - **Exchange**: `video.processing.exchange`  
  - **Routing key (produtor)**: `video.processing.requested`  
- **Observação**:
  - O `processing-service` escuta `video.processing.request`, então hoje há **divergência de routing key** (`request` vs `requested`).

---

#### 3. `video.processing.completed.video-service` (conclusão para o `video-service`)

- **Tecnologia**: RabbitMQ  
- **Serviço(s)**:
  - **`video-service`**: consumidor
- **Parâmetros**:
  - **Exchange**: `video.processing.exchange` (Topic)  
  - **Fila**: `video.processing.completed.video-service`  
  - **Routing key**: `video.processing.completed`

---

#### 4. `video.processing.completed.processing-service` (eventos de sucesso – genérica)

- **Tecnologia**: RabbitMQ  
- **Serviço(s)**:
  - **`processing-service`**: produtor
- **Parâmetros**:
  - **Exchange**: `video.processing.events.exchange` (Topic)  
  - **Fila**: `video.processing.completed.processing-service`  
  - **Routing key**: `video.processing.completed`
- **Consumidores**:
  - Nenhum consumidor implementado no repositório.

---

#### 5. `video.processing.failed.processing-service` (eventos de falha)

- **Tecnologia**: RabbitMQ  
- **Serviço(s)**:
  - **`processing-service`**: produtor
- **Parâmetros**:
  - **Exchange**: `video.processing.events.exchange`  
  - **Fila**: `video.processing.failed.processing-service`  
  - **Routing key**: `video.processing.failed`
- **Consumidores**:
  - Nenhum consumidor implementado no repositório.

---

#### 6. `video.processing.dlq` (Dead Letter Queue)

- **Tecnologia**: RabbitMQ  
- **Serviço(s)**:
  - **`processing-service`**: define e usa como DLQ da fila principal
- **Parâmetros**:
  - **Exchange**: `video.processing.dlq.exchange` (Direct)  
  - **Fila**: `video.processing.dlq`  
  - **Routing key**: `video.processing.dlq`
- **Uso**:
  - Recebe mensagens redirecionadas da `video.processing.queue` via `x-dead-letter-*`.
  - Não há consumidores específicos implementados.

---

### Intenções futuras

- **`notification-service`**:
  - Estrutura preparada para AMQP (config + adapter de consumer), mas **sem filas/exchanges/routing keys definidas ainda**.

---

### Gráfico Mermaid das ligações

```mermaid
flowchart LR
  subgraph videoService [video-service]
    vs_pub["Publica pedido (video.processing.requested)"]
    vs_consume["Consome conclusão (video.processing.completed.video-service)"]
  end

  subgraph processingService [processing-service]
    ps_consume["Consome pedidos (video.processing.queue)"]
    ps_pub_ok["Publica sucesso (video.processing.completed.processing-service)"]
    ps_pub_fail["Publica falha (video.processing.failed.processing-service)"]
  end

  vs_pub -->|"video.processing.exchange / video.processing.requested"| ex_main["video.processing.exchange"]
  ex_main -->|"binding esperado: video.processing.request"| ps_consume

  ps_consume -->|"DLQ em erro"| dlq["video.processing.dlq"]

  ps_pub_ok -->|"video.processing.events.exchange / video.processing.completed"| ex_events["video.processing.events.exchange"]
  ps_pub_fail -->|"video.processing.events.exchange / video.processing.failed"| ex_events

  ex_events -->|"video.processing.completed"| q_completed["video.processing.completed.processing-service"]
  ex_events -->|"video.processing.failed"| q_failed["video.processing.failed.processing-service"]

  ex_main -->|"video.processing.completed"| q_vs_completed["video.processing.completed.video-service"]
  q_vs_completed --> vs_consume
```

