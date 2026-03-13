param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$ScriptPath = "load-tests/k6/video-processor.js",
    [string]$ReportsDir = "load-tests/reports",
    [int]$UploadVus = 1,
    [string]$UploadDuration = "30s",
    [int]$UploadSleepSeconds = 1,
    [string]$VideoFile,
    [string]$VideoFileName = "sample.mp4",
    [string]$VideoContentType = "video/mp4",
    [string]$FrameIntervalSeconds = "1"
)

$ErrorActionPreference = "Stop"

if (-not (Get-Command k6 -ErrorAction SilentlyContinue)) {
    throw "k6 nao encontrado no PATH. Instale o k6 e rode novamente."
}

if (-not $VideoFile) {
    throw "Informe -VideoFile com o caminho do video que sera enviado no POST /api/videos."
}

if (-not (Test-Path $VideoFile)) {
    throw "Arquivo de video nao encontrado: $VideoFile"
}

New-Item -ItemType Directory -Path $ReportsDir -Force | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$reportBase = Join-Path $ReportsDir "k6-$timestamp"
$summaryExport = "$reportBase-raw.json"

$env:K6_BASE_URL = $BaseUrl
$env:K6_UPLOAD_VUS = "$UploadVus"
$env:K6_UPLOAD_DURATION = $UploadDuration
$env:K6_UPLOAD_SLEEP_SECONDS = "$UploadSleepSeconds"
$env:K6_VIDEO_FILE = $VideoFile
$env:K6_VIDEO_FILE_NAME = $VideoFileName
$env:K6_VIDEO_CONTENT_TYPE = $VideoContentType
$env:K6_FRAME_INTERVAL_SECONDS = $FrameIntervalSeconds
$env:K6_REPORT_BASENAME = $reportBase

Write-Host "Executando k6 contra $BaseUrl"
Write-Host "Relatorios em $reportBase"

k6 run --summary-export $summaryExport $ScriptPath

Write-Host ""
Write-Host "Arquivos gerados:"
Write-Host " - $summaryExport"
Write-Host " - $reportBase-summary.json"
Write-Host " - $reportBase.html"
