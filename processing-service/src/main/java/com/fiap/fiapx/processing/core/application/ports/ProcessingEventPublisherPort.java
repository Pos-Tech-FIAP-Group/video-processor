package com.fiap.fiapx.processing.core.application.ports;

/**
 * Port para publicar eventos de processamento de vídeo.
 */
public interface ProcessingEventPublisherPort {
    
    /**
     * Publica evento de processamento concluído com sucesso.
     * 
     * @param videoId ID do vídeo processado
     * @param resultLocation localização/URI do arquivo zip gerado
     * @param frameIntervalSeconds intervalo entre frames usado
     */
    void publishProcessingCompleted(String videoId, String resultLocation, double frameIntervalSeconds);
    
    /**
     * Publica evento de falha no processamento.
     * 
     * @param videoId ID do vídeo que falhou
     * @param errorMessage mensagem de erro
     */
    void publishProcessingFailed(String videoId, String errorMessage);
}
