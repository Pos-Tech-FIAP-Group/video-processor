package com.fiap.fiapx.processing.adapters.driven.infra.processing.runner;

import java.io.IOException;

/**
 * Abstração para execução de processos externos (ffmpeg, ffprobe, etc).
 * Permite simular saídas/erros em testes sem depender de binários instalados.
 */
public interface ProcessRunner {

    /**
     * Executa o comando e retorna o código de saída e o stdout combinado.
     *
     * @param command comando completo (binário + argumentos)
     * @return resultado da execução do processo
     * @throws IOException          erros de I/O ao iniciar ou ler o processo
     * @throws InterruptedException se a thread for interrompida enquanto aguarda o término do processo
     */
    ProcessResult run(String... command) throws IOException, InterruptedException;

    /**
     * Resultado simples de um processo externo.
     *
     * @param exitCode código de saída do processo
     * @param stdout   saída padrão combinada (stdout + stderr redirecionado, quando aplicável)
     */
    record ProcessResult(int exitCode, String stdout) {
    }
}

