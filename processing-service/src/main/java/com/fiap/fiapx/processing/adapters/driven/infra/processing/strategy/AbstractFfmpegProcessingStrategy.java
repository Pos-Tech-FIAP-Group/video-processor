package com.fiap.fiapx.processing.adapters.driven.infra.processing.strategy;

import com.fiap.fiapx.processing.adapters.driven.infra.processing.runner.ProcessRunner;
import com.fiap.fiapx.processing.core.application.ports.VideoProcessingStrategyPort;
import com.fiap.fiapx.processing.core.domain.enums.VideoFormat;
import com.fiap.fiapx.processing.core.domain.model.ProcessingResult;
import com.fiap.fiapx.processing.core.domain.model.VideoProcessingRequest;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Classe base abstrata para strategies de processamento usando FFmpeg.
 * Contém a lógica comum de extração de frames e criação de zip.
 * O zip é salvo em {zipsDirectory}/{videoId}.zip para persistência no volume compartilhado.
 */
public abstract class AbstractFfmpegProcessingStrategy implements VideoProcessingStrategyPort {

    private static final String FRAME_PATTERN = "frame_%04d.png";

    private final Path zipsDirectory;
    private final ProcessRunner processRunner;

    protected AbstractFfmpegProcessingStrategy(Path zipsDirectory, ProcessRunner processRunner) {
        this.zipsDirectory = zipsDirectory;
        this.processRunner = processRunner;
    }

    @Override
    public ProcessingResult processVideo(VideoProcessingRequest request) {
        Path videoPath = request.inputPath();

        if (!Files.isRegularFile(videoPath)) {
            throw new IllegalArgumentException("Video path must point to an existing file: " + videoPath);
        }

        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("video-frames-");

            extractFramesWithFfmpeg(videoPath, tempDir, request.frameIntervalSeconds());

            long frameCount = countFrames(tempDir);

            Files.createDirectories(zipsDirectory);
            Path zipPath = zipsDirectory.resolve(request.videoId() + ".zip");
            zipFrames(tempDir, zipPath);

            String resultLocation = generateResultLocation(request.videoId(), zipPath);

            return new ProcessingResult(zipPath, frameCount, resultLocation);
        } catch (IOException e) {
            throw new RuntimeException("Failed to process video: " + videoPath, e);
        } finally {
            if (tempDir != null) {
                deleteRecursively(tempDir);
            }
        }
    }
    
    /**
     * Extrai frames do vídeo usando FFmpeg com o intervalo especificado.
     */
    protected void extractFramesWithFfmpeg(Path videoPath, Path outputDir, double frameIntervalSeconds) throws IOException {
        Path outputPattern = outputDir.resolve(FRAME_PATTERN);
        double fps = 1.0 / frameIntervalSeconds;

        try {
            ProcessRunner.ProcessResult result = processRunner.run(
                    "ffmpeg",
                    "-i", videoPath.toAbsolutePath().toString(),
                    "-vf", String.format("fps=%.2f", fps),
                    "-y",
                    outputPattern.toAbsolutePath().toString()
            );

            if (result.exitCode() != 0) {
                throw new IOException("FFmpeg exited with code " + result.exitCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("FFmpeg was interrupted", e);
        }
    }
    
    /**
     * Conta o número de frames extraídos.
     */
    private long countFrames(Path framesDir) throws IOException {
        try (Stream<Path> files = Files.list(framesDir)) {
            return files.filter(p -> Files.isRegularFile(p) && p.toString().toLowerCase().endsWith(".png"))
                    .count();
        }
    }
    
    /**
     * Compacta os frames em um arquivo zip.
     */
    private void zipFrames(Path framesDir, Path zipPath) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            try (Stream<Path> files = Files.list(framesDir)) {
                files.filter(p -> Files.isRegularFile(p) && p.toString().toLowerCase().endsWith(".png"))
                        .sorted(Comparator.comparing(Path::getFileName))
                        .forEach(framePath -> {
                            try {
                                ZipEntry entry = new ZipEntry(framePath.getFileName().toString());
                                zos.putNextEntry(entry);
                                Files.copy(framePath, zos);
                                zos.closeEntry();
                            } catch (IOException e) {
                                throw new RuntimeException("Failed to add entry to zip: " + framePath, e);
                            }
                        });
            }
        }
    }
    
    /**
     * Gera a localização/URI do resultado.
     * Por padrão, retorna o path absoluto do zip.
     * Pode ser sobrescrito para retornar uma URI customizada.
     */
    protected String generateResultLocation(String videoId, Path zipPath) {
        return zipPath.toAbsolutePath().toString();
    }
    
    /**
     * Remove recursivamente um diretório ou arquivo.
     */
    private static void deleteRecursively(Path path) {
        try {
            if (Files.isDirectory(path)) {
                try (Stream<Path> entries = Files.walk(path).sorted(Comparator.reverseOrder())) {
                    entries.forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException ignored) {
                        }
                    });
                }
            } else {
                Files.deleteIfExists(path);
            }
        } catch (IOException ignored) {
        }
    }
    
    /**
     * Retorna o formato suportado por esta strategy.
     */
    protected abstract VideoFormat getSupportedFormat();
    
    @Override
    public boolean supports(VideoFormat format) {
        return format != null && format == getSupportedFormat();
    }
}
