#!/bin/bash
set -e # Interrompe o script se ocorrer algum erro grave

# Variáveis padrão
BASE_URL="http://localhost:8080"
SCRIPT_PATH="load-tests/k6/video-processor.js"
REPORTS_DIR="load-tests/reports"
UPLOAD_VUS=1
UPLOAD_DURATION="30s"
UPLOAD_SLEEP_SECONDS=1
VIDEO_FILE=""
VIDEO_FILE_NAME="sample.mp4"
VIDEO_CONTENT_TYPE="video/mp4"
FRAME_INTERVAL_SECONDS="1"

# Lendo os parâmetros passados no terminal
while [[ "$#" -gt 0 ]]; do
    case $1 in
        -BaseUrl|--BaseUrl) BASE_URL="$2"; shift ;;
        -ScriptPath|--ScriptPath) SCRIPT_PATH="$2"; shift ;;
        -ReportsDir|--ReportsDir) REPORTS_DIR="$2"; shift ;;
        -UploadVus|--UploadVus) UPLOAD_VUS="$2"; shift ;;
        -UploadDuration|--UploadDuration) UPLOAD_DURATION="$2"; shift ;;
        -UploadSleepSeconds|--UploadSleepSeconds) UPLOAD_SLEEP_SECONDS="$2"; shift ;;
        -VideoFile|--VideoFile) VIDEO_FILE="$2"; shift ;;
        -VideoFileName|--VideoFileName) VIDEO_FILE_NAME="$2"; shift ;;
        -VideoContentType|--VideoContentType) VIDEO_CONTENT_TYPE="$2"; shift ;;
        -FrameIntervalSeconds|--FrameIntervalSeconds) FRAME_INTERVAL_SECONDS="$2"; shift ;;
        *) echo "Parâmetro desconhecido: $1"; exit 1 ;;
    esac
    shift
done

# Validação do k6
if ! command -v k6 &> /dev/null; then
    echo "Erro: k6 não encontrado no PATH. Instale o k6 e rode novamente."
    exit 1
fi

# Validação do arquivo de vídeo
if [ -z "$VIDEO_FILE" ]; then
    echo "Erro: Informe -VideoFile (ou --VideoFile) com o caminho do video que sera enviado no POST /api/videos."
    exit 1
fi

if [ ! -f "$VIDEO_FILE" ]; then
    echo "Erro: Arquivo de video nao encontrado: $VIDEO_FILE"
    exit 1
fi

# Criando diretório de relatórios
mkdir -p "$REPORTS_DIR"

# Timestamp e caminhos base
TIMESTAMP=$(date +"%Y%m%d-%H%M%S")
REPORT_BASE="$REPORTS_DIR/k6-$TIMESTAMP"
SUMMARY_EXPORT="${REPORT_BASE}-raw.json"

# Exportando as variáveis de ambiente para o k6 ler
export K6_BASE_URL="$BASE_URL"
export K6_UPLOAD_VUS="$UPLOAD_VUS"
export K6_UPLOAD_DURATION="$UPLOAD_DURATION"
export K6_UPLOAD_SLEEP_SECONDS="$UPLOAD_SLEEP_SECONDS"
export K6_VIDEO_FILE="$VIDEO_FILE"
export K6_VIDEO_FILE_NAME="$VIDEO_FILE_NAME"
export K6_VIDEO_CONTENT_TYPE="$VIDEO_CONTENT_TYPE"
export K6_FRAME_INTERVAL_SECONDS="$FRAME_INTERVAL_SECONDS"
export K6_REPORT_BASENAME="$REPORT_BASE"

echo "Executando k6 contra $BASE_URL"
echo "Relatorios em $REPORT_BASE"

# Execução principal
k6 run --summary-export "$SUMMARY_EXPORT" "$SCRIPT_PATH"

echo ""
echo "Arquivos gerados:"
echo " - $SUMMARY_EXPORT"
echo " - ${REPORT_BASE}-summary.json"
echo " - ${REPORT_BASE}.html"