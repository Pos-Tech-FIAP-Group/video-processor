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