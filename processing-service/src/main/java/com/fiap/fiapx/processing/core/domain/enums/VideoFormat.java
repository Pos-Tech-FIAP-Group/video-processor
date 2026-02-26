package com.fiap.fiapx.processing.core.domain.enums;

/**
 * Formatos de vídeo suportados pelo sistema de processamento.
 */
public enum VideoFormat {
    MP4("mp4", "video/mp4"),
    AVI("avi", "video/x-msvideo"),
    MOV("mov", "video/quicktime"),
    MKV("mkv", "video/x-matroska"),
    WEBM("webm", "video/webm"),
    FLV("flv", "video/x-flv"),
    WMV("wmv", "video/x-ms-wmv");

    private final String extension;
    private final String mimeType;

    VideoFormat(String extension, String mimeType) {
        this.extension = extension;
        this.mimeType = mimeType;
    }

    public String getExtension() {
        return extension;
    }

    public String getMimeType() {
        return mimeType;
    }

    /**
     * Detecta o formato baseado na extensão do arquivo.
     */
    public static VideoFormat fromExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        
        String lowerFilename = filename.toLowerCase();
        int lastDot = lowerFilename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == lowerFilename.length() - 1) {
            return null;
        }
        
        String ext = lowerFilename.substring(lastDot + 1);
        for (VideoFormat format : values()) {
            if (format.extension.equals(ext)) {
                return format;
            }
        }
        return null;
    }
}
