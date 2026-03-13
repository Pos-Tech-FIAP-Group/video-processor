# 🚪 API Gateway

> Ponto de entrada único do sistema Video Processor. Roteia as requisições para auth-service e video-service e valida JWT nas rotas protegidas — atende ao requisito de **sistema protegido por usuário e senha**.

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Cloud Gateway](https://img.shields.io/badge/Spring_Cloud-Gateway-6DB33F?logo=spring&logoColor=white)](https://spring.io/projects/spring-cloud-gateway)

---

## Função

- **Porta 8080** — recebe todas as chamadas do cliente (Web App ou Postman).
- **Roteamento** — `/api/auth/**` → auth-service (8081); `/api/videos/**` → video-service (8082).
- **Validação JWT** — nas rotas protegidas exige o header `Authorization: Bearer <token>`; rejeita com **401** se ausente ou inválido (validação via chamada ao auth-service).
- **CORS** — configurado para origem `http://localhost:4200` (Web App); headers permitidos incluem `Authorization`, `Content-Type`, `X-User-Id`.
- **Upload de vídeo** — limite de request de **5000MB** para `/api/videos/**`.

## Rotas

| Tipo | Rotas | Destino |
|------|-------|---------|
| Públicas | `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/validate`, `GET /api/videos/health`, `/actuator/**` | auth-service / video-service (sem token) |
| Protegidas | `GET /api/auth/users/{id}`, `/api/videos/**` (upload, listagem, detalhes) | Exigem `Authorization: Bearer <token>` |

> **Rotas de vídeo:** o upload exige o header **`X-User-Id`** (UUID do usuário). A listagem usa o query param **`userId`**. O Web App e a collection Postman preenchem esses valores a partir do login (campo `userUuid`).

## Configuração

- **application.yml** — rotas fixas para `http://auth-service:8081` e `http://video-service:8082`. A URL usada para **validar o JWT** vem da variável **`AUTH_SERVICE_URL`**, definida no **entrypoint** do container a partir de **`AUTH_SERVICE_HOST`** (default: `auth-service`).
- **Uso apenas via Docker** — não há suporte a execução local standalone; o gateway é pensado para rodar no Compose da raiz.

## Como rodar

O gateway sobe junto com a stack na raiz do projeto:

```bash
docker compose up -d
```

Ou, com build local dos serviços:

```bash
docker compose -f compose.yml -f compose.dev.yml up -d
```

Base URL: **http://localhost:8080**

## Postman

| Collection | Uso |
|------------|-----|
| [docs/Api-Gateway.postman_collection.json](docs/Api-Gateway.postman_collection.json) | Registro, login, validação de token, rotas protegidas e upload de vídeo via gateway. |
| [../resources/Fluxo-Completo.postman_collection.json](../resources/Fluxo-Completo.postman_collection.json) | Fluxo completo (auth + vídeos) na raiz do projeto. |

## Documentação relacionada

- **README raiz** — visão do projeto, como rodar a stack e lista de serviços: [../README.md](../README.md).
- **Auth Service** — endpoints e rota interna: [../auth-service/README.md](../auth-service/README.md).
- **Guia de testes** — fluxo completo passo a passo: [../TESTES-FLUXO-COMPLETO.md](../TESTES-FLUXO-COMPLETO.md).
