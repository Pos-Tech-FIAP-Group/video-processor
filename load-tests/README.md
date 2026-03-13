# Testes de carga com k6

Os artefatos de carga ficam em `load-tests/k6/`. Para subir a stack antes de rodar os testes, veja o [README na raiz](../README.md) ou [TESTES-FLUXO-COMPLETO.md](../TESTES-FLUXO-COMPLETO.md).

## Cenario incluido

- O `setup()` registra um usuario e faz login.
- A carga exercita somente o endpoint `POST /api/videos`.
- Cada requisicao envia o header `Authorization`, o `X-User-Id`, o arquivo multipart e valida a resposta `202` com body JSON.

## Executar

### Windows (PowerShell)

Com a stack do projeto de pe:

```powershell
./load-tests/k6/run-k6.ps1 `
  -VideoFile "C:\caminho\para\sample.mp4"
```

Exemplo com mais carga:

```powershell
./load-tests/k6/run-k6.ps1 `
  -VideoFile "C:\caminho\para\sample.mp4" `
  -UploadVus 5 `
  -UploadDuration "1m"
```

### Linux / Ubuntu (bash)

Primeira vez, torne o script executavel:

```bash
chmod +x load-tests/k6/run-k6.sh
```

Com a stack do projeto de pe:

```bash
./load-tests/k6/run-k6.sh \
  -VideoFile "/caminho/para/sample.mp4"
```

Exemplo com mais carga:

```bash
./load-tests/k6/run-k6.sh \
  -VideoFile "/caminho/para/sample.mp4" \
  -UploadVus 5 \
  -UploadDuration "1m"
```

## Parametros principais

Os mesmos parametros sao aceitos tanto pelo script PowerShell (`run-k6.ps1`) quanto pelo script bash (`run-k6.sh`):

- `-BaseUrl`: gateway alvo. Padrao `http://localhost:8080`.
- `-VideoFile`: caminho do video real a ser enviado. Obrigatorio.
- `-UploadVus`: quantidade de VUs do cenario de upload.
- `-UploadDuration`: duracao total do teste. Padrao `30s`.
- `-UploadSleepSeconds`: pausa entre uploads por VU. Padrao `1`.
- `-FrameIntervalSeconds`: valor enviado na query string do upload. Padrao `1`.

## Relatorios

Cada execucao gera arquivos em `load-tests/reports/`:

- `k6-<timestamp>-raw.json`: export bruto do `k6 run --summary-export`.
- `k6-<timestamp>-summary.json`: resumo completo produzido pelo `handleSummary`.
- `k6-<timestamp>.html`: relatorio HTML simples para abrir no navegador.

## Observacoes

- O script usa o API Gateway como entrada principal.
- Para exercitar processamento real, use um arquivo de video valido (`.mp4`, `.mov`, `.avi` ou `.mkv`).
