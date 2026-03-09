package com.fiap.fiapx.processing.core.application.ports;

import java.nio.file.Path;

/**
 * Port para upload do zip de frames para armazenamento externo (ex.: S3).
 * Retorna Optional vazio se o upload não estiver configurado (ex.: bucket não definido).
 */
public interface ZipStorageUploadPort {

    /**
     * Faz upload do zip e retorna a URL pública para download.
     * Se o storage não estiver configurado, retorna Optional.empty() e o caller usa o path local.
     *
     * @param localZipPath caminho local do arquivo zip
     * @param userUuid     UUID do usuário (string); pasta no bucket = userUuid)
     * @param videoId      ID do vídeo
     * @return URL pública do objeto ou empty se upload não configurado
     */
    java.util.Optional<String> uploadAndGetPublicUrl(Path localZipPath, String userUuid, String videoId);
}
