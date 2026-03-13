# 🔑 Auth Service

> Microsserviço de **registro, login e validação de token** (JWT). Atende ao requisito de **sistema protegido por usuário e senha**; o notification-service também usa este serviço para obter o e-mail do usuário em caso de falha no processamento.

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-Database-47A248?logo=mongodb&logoColor=white)](https://www.mongodb.com/)

---

## Função

- **Registro** de usuários (username, e-mail, senha).
- **Login** com geração de JWT (campo `userUuid` na resposta — usado como `X-User-Id` no video-service).
- **Validação** de token (usada pelo API Gateway e outros consumidores).
- **Consulta** de usuário por ID (protegido) ou por **UUID** (rota interna para o notification-service).

## Endpoints (via gateway em `/api/auth/...`)

| Método | Rota | Acesso | Descrição |
|--------|------|--------|-----------|
| POST | `/api/auth/register` | Público | Criar usuário. |
| POST | `/api/auth/login` | Público | Obter token JWT (`token`, `userUuid`, `userId`, `expiresIn` em ms). |
| GET | `/api/auth/validate?token=...` | Público | Validar JWT. |
| GET | `/api/auth/users/{id}` | Protegido (Bearer) | Buscar usuário por ID (Mongo). |

> O cliente deve usar sempre o **API Gateway** (porta 8080) como entrada; o gateway roteia `/api/auth/**` para este serviço.

## Rota interna (service-to-service)

| Método | Rota | Acesso | Descrição |
|--------|------|--------|-----------|
| GET | `/internal/users/by-uuid/{uuid}` | Sem JWT (uso interno) | Buscar usuário por UUID. **Não é exposta no gateway**; chamada direta ao auth-service (porta 8081). Usada pelo **notification-service** para obter o e-mail em caso de falha no processamento. |

## Banco de dados

- **MongoDB** (banco `auth_db`). No Docker Compose: URI `mongodb://mongodb:27017/auth_db` (serviço `mongodb`).

## Configuração

- **Porta:** 8081.
- **Variáveis de ambiente:** `JWT_SECRET` (obrigatório em produção); MongoDB via `spring.data.mongodb.uri`. JWT expiration: 86400000 ms (24 h) no `application.yml`.

## Como rodar

Com a stack na raiz do projeto:

```bash
docker compose up -d
```

Ou build local dos serviços:

```bash
docker compose -f compose.yml -f compose.dev.yml up -d
```

Acesso direto ao serviço (para testes ou rota interna): **http://localhost:8081**

## Postman

| Collection | Uso |
|------------|-----|
| [docs/Auth-Service.postman_collection.json](docs/Auth-Service.postman_collection.json) | Testes diretos no auth-service (porta 8081). |
| [../api-gateway/docs/Api-Gateway.postman_collection.json](../api-gateway/docs/Api-Gateway.postman_collection.json) | Testar via gateway (recomendado). |
| [../resources/Fluxo-Completo.postman_collection.json](../resources/Fluxo-Completo.postman_collection.json) | Fluxo completo na raiz. |

## Documentação relacionada

- **README raiz** — visão do projeto e como rodar a stack: [../README.md](../README.md).
- **API Gateway** — rotas e validação JWT: [../api-gateway/README.md](../api-gateway/README.md).
- **Guia de testes** — fluxo completo: [../TESTES-FLUXO-COMPLETO.md](../TESTES-FLUXO-COMPLETO.md).
