## Monitoramento - Video Processor

- **Stack de métricas**: Prometheus + Grafana.
- **Escopo atual**:
  - Métricas e healthchecks expostos pelo `video-service`.
  - Coleta de métricas via Prometheus.
  - Visualização via Grafana.

### Evoluções futuras sugeridas

- **Alertas (Prometheus + Alertmanager)**:
  - Regras para falta de processamento (nenhum vídeo concluído em X minutos).
  - Regras para taxa de erro alta (`video_processing_failure_total` crescendo rápido).
  - Regras para latência média de processamento acima de um limite.

- **Métricas de infraestrutura**:
  - Adicionar `node-exporter` para métricas do host.
  - Adicionar `cAdvisor` para métricas detalhadas de containers.
  - Dashboards de CPU, memória, I/O para serviços críticos (especialmente `video-service` e `processing-service`).

- **Centralização de logs**:
  - Introduzir Loki + Promtail ou ELK em um `docker-compose` separado dedicado a observabilidade.
  - Manter o stack de monitoramento desacoplado do stack de aplicação.

