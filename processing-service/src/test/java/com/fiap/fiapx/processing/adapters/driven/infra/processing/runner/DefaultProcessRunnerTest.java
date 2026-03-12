package com.fiap.fiapx.processing.adapters.driven.infra.processing.runner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultProcessRunnerTest {

    private final DefaultProcessRunner runner = new DefaultProcessRunner();

    @Test
    void shouldRunSimpleCommandAndCaptureOutput() throws Exception {
        ProcessRunner.ProcessResult result = runner.run("echo", "hello-world");

        assertEquals(0, result.exitCode());
        assertTrue(result.stdout().contains("hello-world"));
    }
}

