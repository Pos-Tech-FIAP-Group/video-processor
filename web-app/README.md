# Video Processor – Web App

Frontend Angular do Video Processor. Login (usuários já cadastrados), listagem de vídeos, upload e detalhe com status do processamento. Comunica com o **API Gateway** em `http://localhost:8080`.

## Pré-requisitos

- Backend em execução (gateway, auth-service, video-service, etc.), por exemplo via `docker compose up` ou serviços rodando localmente.
- Usuário já cadastrado (registro é feito fora do frontend, ex.: Postman em `POST /api/auth/register`).

## Development server

```bash
npm start
# ou: npx ng serve
```

Acesse `http://localhost:4200/`. A API está configurada em `src/environments/environment.ts` (`apiUrl: 'http://localhost:8080'`).

## Fluxo para testar

1. **Login:** em `/login`, informe usuário e senha de um usuário já cadastrado.
2. **Vídeos:** após login, você é redirecionado para a listagem em `/videos`.
3. **Upload:** em "Upload" ou `/videos/upload`, selecione um arquivo de vídeo e envie.
4. **Detalhe:** na listagem, clique em "Ver" em um vídeo para ver status (PENDENTE, PROCESSANDO, CONCLUIDO, ERRO), frames e datas.
5. **Logout:** use "Sair" no topo para encerrar e voltar à tela de login.

Em 401 (token inválido ou expirado), o app faz logout e redireciona para `/login`.

## Code scaffolding

Angular CLI includes powerful code scaffolding tools. To generate a new component, run:

```bash
ng generate component component-name
```

For a complete list of available schematics (such as `components`, `directives`, or `pipes`), run:

```bash
ng generate --help
```

## Building

To build the project run:

```bash
ng build
```

This will compile your project and store the build artifacts in the `dist/` directory. By default, the production build optimizes your application for performance and speed.

## Running unit tests

To execute unit tests with the [Karma](https://karma-runner.github.io) test runner, use the following command:

```bash
ng test
```

## Running end-to-end tests

For end-to-end (e2e) testing, run:

```bash
ng e2e
```

Angular CLI does not come with an end-to-end testing framework by default. You can choose one that suits your needs.

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.
