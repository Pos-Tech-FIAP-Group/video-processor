package com.fiap.fiapx.processing.adapters.driven.infra.processing.runner;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Implementação padrão de {@link ProcessRunner} que usa {@link ProcessBuilder}.
 */
@Component
public class DefaultProcessRunner implements ProcessRunner {

    @Override
    public ProcessResult run(String... command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        String output;
        try (InputStream inputStream = process.getInputStream()) {
            byte[] bytes = inputStream.readAllBytes();
            output = new String(bytes, StandardCharsets.UTF_8);
        }

        int exitCode = process.waitFor();
        return new ProcessResult(exitCode, output);
    }
}

