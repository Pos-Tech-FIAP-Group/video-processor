# 🌐 Video Processor – Web App

> Frontend do Video Processor: interface em **Angular** para login, listagem de vídeos, upload e acompanhamento do status do processamento. Todas as chamadas HTTP passam pelo **API Gateway** (`http://localhost:8080`).

[![Angular](https://img.shields.io/badge/Angular-19-DD0031?logo=angular&logoColor=white)](https://angular.io/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.7-3178C6?logo=typescript&logoColor=white)](https://www.typescriptlang.org/)

---

## Stack

- **Angular** 19 (standalone components)
- **Angular Material** (UI)
- **RxJS** 7

## Pré-requisitos

- **Node.js** e **npm** (ou yarn)
- Backend em execução (gateway, auth, video-service, etc.), por exemplo via `docker compose up -d` na raiz do projeto
- Usuário já cadastrado (registro via Postman em `POST /api/auth/register` ou pela tela de registro, se disponível)

## Como rodar

### Desenvolvimento

```bash
npm install
npm start
```

Acesse **http://localhost:4200**. A API está configurada em `src/environments/environment.development.ts` (`apiUrl: 'http://localhost:8080'`).

### Build de produção

```bash
npm run build
```

A saída fica em `dist/`. No Docker Compose da raiz, o web-app é servido via Nginx (porta 4200 mapeada para 80 no container).

## Fluxo na aplicação

1. **Login** (`/login`) — informe usuário e senha de um usuário já cadastrado.
2. **Vídeos** — após login, redirecionamento para a listagem em `/videos`.
3. **Upload** (`/videos/upload`) — selecione um arquivo de vídeo e envie; o backend retorna 202 e o processamento é assíncrono.
4. **Detalhe** — na listagem, clique em um vídeo para ver status (PENDENTE, PROCESSANDO, CONCLUIDO, ERRO), quantidade de frames, datas e link para download do ZIP (quando concluído).
5. **Logout** — use "Sair" no topo para encerrar e voltar à tela de login.

> Em **401** (token inválido ou expirado), o app faz logout e redireciona para `/login`.

## Estrutura relevante

| Pasta | Conteúdo |
|-------|----------|
| `src/app/core/` | Guards (auth), interceptors (token), services (auth) |
| `src/app/features/auth/` | Login, registro |
| `src/app/features/videos/` | Listagem, upload, detalhe |
| `src/environments/` | `apiUrl` (gateway) |

## Documentação relacionada

- **README raiz** — como subir a stack completa com Docker e visão dos microsserviços: [../README.md](../README.md).
- **API Gateway** — rotas e Postman: [../api-gateway/README.md](../api-gateway/README.md).
- **Guia de testes** — fluxo completo (incl. uso do Web App): [../TESTES-FLUXO-COMPLETO.md](../TESTES-FLUXO-COMPLETO.md).
