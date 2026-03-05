package com.fiap.fiapx.processing.adapters.driven.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuração do diretório onde os zips de frames são persistidos.
 * Usa {base-path}/zips (ex.: /shared/videos/zips) para que video-service e processing-service
 * compartilhem o mesmo volume.
 */
@Configuration
public class ProcessingStorageConfig {

    @Bean
    public Path zipsDirectoryPath(
            @Value("${processing.storage.base-path:/data}") String basePath) {
        return Paths.get(basePath, "zips");
    }
}
