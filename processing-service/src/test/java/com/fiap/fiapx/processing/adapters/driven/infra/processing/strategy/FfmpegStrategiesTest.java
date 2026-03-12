package com.fiap.fiapx.processing.adapters.driven.infra.processing.strategy;

import com.fiap.fiapx.processing.adapters.driven.infra.processing.runner.ProcessRunner;
import com.fiap.fiapx.processing.core.domain.enums.VideoFormat;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FfmpegStrategiesTest {

    private static class NoopProcessRunner implements ProcessRunner {
        @Override
        public ProcessResult run(String... command) {
            return new ProcessResult(0, "");
        }
    }

    private final Path dummyPath = Paths.get("/tmp/zips");
    private final ProcessRunner noopRunner = new NoopProcessRunner();

    @Test
    void mp4StrategyShouldSupportOnlyMp4() {
        FfmpegMp4ProcessingStrategy strategy = new FfmpegMp4ProcessingStrategy(dummyPath, noopRunner);

        assertTrue(strategy.supports(VideoFormat.MP4));
        assertFalse(strategy.supports(VideoFormat.AVI));
        assertFalse(strategy.supports(VideoFormat.MOV));
    }

    @Test
    void aviStrategyShouldSupportOnlyAvi() {
        FfmpegAviProcessingStrategy strategy = new FfmpegAviProcessingStrategy(dummyPath, noopRunner);

        assertTrue(strategy.supports(VideoFormat.AVI));
        assertFalse(strategy.supports(VideoFormat.MP4));
        assertFalse(strategy.supports(VideoFormat.MOV));
    }

    @Test
    void movStrategyShouldSupportOnlyMov() {
        FfmpegMovProcessingStrategy strategy = new FfmpegMovProcessingStrategy(dummyPath, noopRunner);

        assertTrue(strategy.supports(VideoFormat.MOV));
        assertFalse(strategy.supports(VideoFormat.MP4));
        assertFalse(strategy.supports(VideoFormat.AVI));
    }
}

