package com.fiap.fiapx.processing.adapters.driven.infra.processing.adapter;

import com.fiap.fiapx.processing.core.application.ports.VideoFrameExtractorPort;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Extracts one frame per second from a video using FFmpeg and packages the images into a zip file
 * in the same directory as the source video.
 * <p>
 * Requires FFmpeg to be installed and available on the system PATH.
 * </p>
 */
@Component
public class FfmpegVideoFrameExtractorAdapter implements VideoFrameExtractorPort {

    private static final String FRAME_PATTERN = "frame_%04d.png";
    private static final String ZIP_SUFFIX = "_frames.zip";

    @Override
    public Path extractFramesPerSecondToZip(Path videoPath) {
        if (videoPath == null || !Files.isRegularFile(videoPath)) {
            throw new IllegalArgumentException("Video path must point to an existing file: " + videoPath);
        }

        Path outputDir = videoPath.getParent();
        if (outputDir == null) {
            outputDir = Paths.get(".");
        }

        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("video-frames-");
            Path framesOutput = tempDir.resolve("frame_%04d.png");

            extractFramesWithFfmpeg(videoPath, tempDir);

            String baseName = getBaseName(videoPath);
            Path zipPath = outputDir.resolve(baseName + ZIP_SUFFIX);
            zipFrames(tempDir, zipPath);

            return zipPath;
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract frames from video: " + videoPath, e);
        } finally {
            if (tempDir != null) {
                deleteRecursively(tempDir);
            }
        }
    }

    private void extractFramesWithFfmpeg(Path videoPath, Path outputDir) throws IOException {
        Path outputPattern = outputDir.resolve(FRAME_PATTERN);
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-i", videoPath.toAbsolutePath().toString(),
                "-vf", "fps=1",
                "-y",
                outputPattern.toAbsolutePath().toString()
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (InputStream out = process.getInputStream()) {
            out.readAllBytes(); // consume output so process doesn't block
        }

        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("FFmpeg exited with code " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
            throw new IOException("FFmpeg was interrupted", e);
        }
    }

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

    private static String getBaseName(Path path) {
        String fileName = path.getFileName().toString();
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(0, lastDot) : fileName;
    }

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
}
