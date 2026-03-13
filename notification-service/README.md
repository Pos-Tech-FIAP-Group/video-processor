# 📬 Notification Service

> Microsserviço que **notifica o usuário por e-mail em caso de erro** no processamento de vídeo (requisito essencial). **Não expõe API REST** — é acionado apenas por eventos na fila RabbitMQ. Em eventos de **sucesso**, ignora a mensagem; em **falha**, obtém o e-mail no auth-service e envia via SMTP. Em dev os e-mails chegam no **Mailtrap** (caixa do serviço), não no e-mail real do usuário.

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Messaging-FF6600?logo=rabbitmq&logoColor=white)](https://www.rabbitmq.com/)

---

## Requisito atendido

- **Notificação (e-mail) em caso de erro:** quando o `processing-service` publica evento com `success=false`, o notification-service consome a mensagem, obtém o e-mail do usuário no auth-service (rota interna **`GET /internal/users/by-uuid/{uuid}`**) e envia e-mail com os dados da falha. Os e-mails são recebidos pelo serviço de captura (**Mailtrap** em dev).

## Arquitetura

- **Arquitetura hexagonal** (core + adapters); ver [ARQUITETURA-DO-PROJETO.md](../ARQUITETURA-DO-PROJETO.md).
- **Entrada:** apenas RabbitMQ (fila `video.processing.notification-service.queue`).
- **Integração:** auth-service (HTTP) para obter e-mail; envio via SMTP (JavaMail).

## Fluxo

1. O **processing-service** publica eventos de conclusão na exchange `video.processing.events.exchange` (routing key `video.processing.completed`), com payload contendo `videoId`, **`success`**, `userId`, `errorMessage`, etc.
2. O **notification-service** consome da fila `video.processing.notification-service.queue`.
3. Para cada mensagem:
   - Se **`success === true`:** não faz nada.
   - Se **`success === false`:** chama o auth-service em `GET /internal/users/by-uuid/{uuid}` para obter o e-mail e envia o e-mail informando a falha.

Detalhes das filas em [QUEUES.md](../QUEUES.md).

## Variáveis de ambiente

| Variável | Descrição | Exemplo |
|----------|-----------|---------|
| `AUTH_SERVICE_URL` | URL base do auth-service | `http://auth-service:8081` |
| `SPRING_RABBITMQ_HOST` / `PORT` / `USERNAME` / `PASSWORD` | RabbitMQ | `rabbitmq`, `5672`, `admin`, `admin123` |
| `MAIL_HOST` / `MAIL_PORT` / `MAIL_USERNAME` / `MAIL_PASSWORD` | SMTP | Mailtrap: `sandbox.smtp.mailtrap.io`, `2525` |

No `compose.yml` da raiz o serviço está configurado com **Mailtrap** (sandbox). Os e-mails enviados **chegam na caixa do Mailtrap** ([mailtrap.io](https://mailtrap.io)).

## Porta

- **8084** — uso interno; não exposto no gateway.

## Como rodar

**Com Docker Compose (raiz do projeto):**

```bash
docker compose up -d
```

Ou build local:

```bash
docker compose -f compose.yml -f compose.dev.yml up -d
```

**Local:** RabbitMQ e auth-service acessíveis; configurar `AUTH_SERVICE_URL`, `SPRING_RABBITMQ_*` e `MAIL_*` no ambiente ou em `application.yml`.

## Sem API REST / Postman

Não há endpoints HTTP. O fluxo é testado provocando uma falha no processamento e verificando o e-mail no Mailtrap, ou via testes de integração/BDD do próprio serviço.

## Documentação relacionada

- **README raiz** — visão do projeto e como rodar a stack: [../README.md](../README.md).
- **QUEUES** — fila e bindings: [../QUEUES.md](../QUEUES.md).
- **Auth Service** — rota interna `/internal/users/by-uuid/{uuid}`: [../auth-service/README.md](../auth-service/README.md).
- **Guia de testes** — como validar a notificação em erro: [../TESTES-FLUXO-COMPLETO.md](../TESTES-FLUXO-COMPLETO.md).
