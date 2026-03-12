package com.fiap.fiapx.processing.adapters.driven.infra.storage;

import com.fiap.fiapx.processing.core.domain.exception.StorageException;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class S3ZipStorageUploadAdapterTest {

    @Test
    void shouldReturnEmptyWhenBucketIsNotConfigured() throws IOException {
        Path tempZip = Files.createTempFile("zip-", ".zip");

        try {
            S3ZipStorageUploadAdapter adapter = new S3ZipStorageUploadAdapter("", "");

            Optional<String> result = adapter.uploadAndGetPublicUrl(tempZip, "user-123", "video-456");

            assertTrue(result.isEmpty());
        } finally {
            Files.deleteIfExists(tempZip);
        }
    }

    @Test
    void shouldWrapExceptionFromS3ClientInStorageException() throws Exception {
        Path tempZip = Files.createTempFile("zip-", ".zip");

        try {
            S3ZipStorageUploadAdapter adapter = new S3ZipStorageUploadAdapter("my-bucket", "sa-east-1");

            S3Client s3Client = mock(S3Client.class);
            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenThrow(new RuntimeException("S3 is down"));

            Field clientField = S3ZipStorageUploadAdapter.class.getDeclaredField("s3Client");
            clientField.setAccessible(true);
            clientField.set(adapter, s3Client);

            StorageException ex = assertThrows(StorageException.class,
                    () -> adapter.uploadAndGetPublicUrl(tempZip, "user-123", "video-456"));

            assertTrue(ex.getMessage().startsWith("S3 upload failed: "));
            verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        } finally {
            Files.deleteIfExists(tempZip);
        }
    }
}

