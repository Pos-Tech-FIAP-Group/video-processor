# Video Service

Microsserviço responsável por gerenciar metadados e status do processamento de vídeos (upload, consulta de status, listagem por usuário e download do ZIP).

## Como rodar

### Pré-requisitos
- Java 21
- Maven
- PostgreSQL (local ou via docker-compose do monorepo)

### Executar
```bash
  mvn spring-boot:run
````

## Verificando o banco manualmente

```bash
  docker exec -it postgres-video psql -U video_user -d video_db
````
## Testes de integração (Testcontainers)

Este serviço possui teste de integração que sobe um PostgreSQL real via Docker (Testcontainers) e valida:
- aplicação das migrations do Flyway
- criação da tabela `videos`

### Executar
```bash
  mvn clean test
````