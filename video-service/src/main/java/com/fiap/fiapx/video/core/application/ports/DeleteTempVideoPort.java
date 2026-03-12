package com.fiap.fiapx.video.core.application.ports;

public interface DeleteTempVideoPort {
    void deleteIfExists(String videoPath);
}
