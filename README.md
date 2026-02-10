# Video Processor - FIAP X

Sistema de Processamento de Vídeos com Arquitetura de Microsserviços.

## Estrutura padrão (Arquitetura Hexagonal)

Cada microsserviço segue o **mesmo padrão de pastas** para facilitar o trabalho em equipe. A estrutura já está criada; cada desenvolvedor implementa as classes na sua pasta.

**Leia primeiro:** [ARQUITETURA-HEXAGONAL.md](./ARQUITETURA-HEXAGONAL.md) — explica o que vai em cada pasta e as regras do padrão.

## Microsserviços

| Serviço            | Pasta                  | Responsável |
|--------------------|------------------------|-------------|
| API Gateway        | `api-gateway/`         | —           |
| Auth Service       | `auth-service/`        | —           |
| Video Service      | `video-service/`       | —           |
| Processing Service | `processing-service/`  | —           |
| Notification Service | `notification-service/` | —         |

