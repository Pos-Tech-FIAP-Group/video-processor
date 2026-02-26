package com.fiap.fiapx.processing.core.domain.model;

/**
 * Representa a duração de um vídeo em segundos.
 */
public record VideoDuration(double seconds) {
    
    public VideoDuration {
        if (seconds < 0) {
            throw new IllegalArgumentException("Duration cannot be negative: " + seconds);
        }
    }
    
    /**
     * Retorna a duração em minutos.
     */
    public double toMinutes() {
        return seconds / 60.0;
    }
    
    /**
     * Retorna a duração em horas.
     */
    public double toHours() {
        return seconds / 3600.0;
    }
    
    /**
     * Verifica se a duração é válida (maior que zero).
     */
    public boolean isValid() {
        return seconds > 0;
    }
}
