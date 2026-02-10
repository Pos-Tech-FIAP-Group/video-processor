# Padrão de Arquitetura Hexagonal - Video Processor

Este documento descreve a **estrutura de pastas padrão** que todos os microsserviços devem seguir. Cada desenvolvedor implementa seu serviço dentro dessa estrutura.

## Visão geral

- **Core**: regras de negócio e casos de uso (não dependem de framework nem de banco).
- **Adapters (driven)**: implementações que o core usa (banco, mensageria, e-mail, etc.).
- **Adapters (driver)**: pontos de entrada (API REST, consumers de fila, etc.).

O core **nunca** importa classes de `adapters`. As dependências são invertidas via **ports** (interfaces).

---

## Estrutura padrão por serviço

```
<nome-do-servico>/
├── src/main/java/com/fiap/fiapx/<servico>/
│   ├── core/                          # NÚCLEO (regras de negócio)
│   │   ├── domain/
│   │   │   ├── model/                 # Entidades de domínio
│   │   │   └── enums/                 # Enums do domínio
│   │   └── application/
│   │       ├── usecases/              # Casos de uso (orquestram o domínio)
│   │       └── ports/                 # Interfaces (contratos)
│   │
│   └── adapters/
│       ├── driven/                    # ADAPTADORES QUE O CORE USA
│       │   └── infra/
│       │       ├── persistence/       # Banco (adapter, entity, repository)
│       │       ├── messaging/        # RabbitMQ (adapter)
│       │       ├── storage/          # Arquivos (adapter)
│       │       ├── mail/             # E-mail (adapter)
│       │       ├── security/         # JWT, etc. (adapter)
│       │       └── processing/       # FFmpeg (adapter) – apenas processing-service
│       │
│       └── driver/                   # PONTOS DE ENTRADA
│           └── api/
│               ├── controller/       # REST controllers
│               ├── consumer/         # Listeners RabbitMQ (quando houver)
│               ├── config/          # Configurações (rotas, segurança, AMQP)
│               ├── dto/
│               │   ├── request/
│               │   └── response/
│               └── exceptionhandler/ # Tratamento global de exceções
│
├── src/main/resources/
│   └── application.yml
│
├── src/test/java/com/fiap/fiapx/<servico>/
├── pom.xml
└── Dockerfile
```

---

## O que colocar em cada camada

| Camada | Responsabilidade | Exemplos |
|--------|------------------|----------|
| **core/domain/model** | Entidades de domínio puras (sem JPA/Mongo anotações nos modelos do core, se possível; ou use uma única entidade por conceito). | `User`, `Video` |
| **core/domain/enums** | Enums do negócio. | `VideoStatus`, `UserRole` |
| **core/application/usecases** | Casos de uso: orquestram domínio e portas. | `RegisterUserUseCase`, `ProcessVideoUseCase` |
| **core/application/ports** | Interfaces que o core precisa (repositórios, envio de mensagem, notificação, etc.). | `UserRepositoryPort`, `MessagePublisherPort` |
| **adapters/driven/infra/** | Implementações concretas das portas (banco, fila, e-mail, storage). | `UserPersistenceAdapter`, `RabbitMQMessagePublisher` |
| **adapters/driver/api/controller** | Endpoints REST. | `AuthController`, `VideoController` |
| **adapters/driver/api/consumer** | Listeners de fila (RabbitMQ). | `VideoProcessingConsumer` |
| **adapters/driver/api/dto** | Objetos de entrada/saída da API. | `LoginRequest`, `VideoStatusResponse` |
| **adapters/driver/api/exceptionhandler** | `@ControllerAdvice` para exceções. | `GlobalExceptionHandler` |

---

## Regras importantes

1. **Core não depende de adapters**  
   O `core` só conhece as interfaces em `core/application/ports`. Quem implementa são as classes em `adapters/driven`.

2. **Driver chama o core**  
   Controllers e consumers obtêm um use case (por injeção) e chamam `useCase.execute(...)`. Não colocam lógica de negócio no controller.

3. **Um serviço pode não usar todas as pastas**  
   Ex.: API Gateway pode não ter `persistence`; Processing Service não tem `controller` REST (só consumer). Use apenas as pastas que fizerem sentido e deixe as outras vazias ou com `.gitkeep`.

4. **Pacote base**  
   Sempre `com.fiap.fiapx.<servico>` (gateway, auth, video, processing, notification).

---

## Serviços e suas pastas principais

| Serviço | Core | Driven (infra) | Driver (api) |
|---------|------|----------------|--------------|
| **api-gateway** | domain, ports (se necessário) | — | config (rotas, filtros) |
| **auth-service** | domain (User), usecases, ports | persistence (MongoDB), security (JWT) | controller, dto, exceptionhandler |
| **video-service** | domain (Video), usecases, ports | persistence (JPA), messaging, storage | controller, dto, exceptionhandler |
| **processing-service** | usecases, ports | messaging (consumer), processing (FFmpeg), storage | config (AMQP), consumer |
| **notification-service** | usecases, ports | messaging (consumer), mail | config (AMQP), consumer, exceptionhandler |

---

## Classe principal

Cada serviço deve ter uma classe com `@SpringBootApplication` no pacote base, por exemplo:

- `com.fiap.fiapx.gateway.GatewayApplication`
- `com.fiap.fiapx.auth.AuthServiceApplication`
- `com.fiap.fiapx.video.VideoServiceApplication`
- `com.fiap.fiapx.processing.ProcessingServiceApplication`
- `com.fiap.fiapx.notification.NotificationServiceApplication`

Ela pode ficar em `src/main/java/com/fiap/fiapx/<servico>/` (ao lado de `core` e `adapters`).

---


