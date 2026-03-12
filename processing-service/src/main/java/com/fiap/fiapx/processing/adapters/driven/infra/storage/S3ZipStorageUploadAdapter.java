package com.fiap.fiapx.processing.adapters.driven.infra.storage;

import com.fiap.fiapx.processing.core.application.ports.ZipStorageUploadPort;
import com.fiap.fiapx.processing.core.domain.exception.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Adapter que faz upload do zip para o bucket S3 e retorna a URL pública.
 * A chave no S3 é organizada por user UUID: {userUuid}/{videoId}.zip.
 * Se o bucket não estiver configurado (bucket vazio), retorna empty e o caller usa o path local.
 */
@Component
public class S3ZipStorageUploadAdapter implements ZipStorageUploadPort {

    private static final Logger logger = LoggerFactory.getLogger(S3ZipStorageUploadAdapter.class);

    private final String bucket;
    private final String region;
    private S3Client s3Client;

    public S3ZipStorageUploadAdapter(
            @Value("${processing.storage.s3.bucket:}") String bucket,
            @Value("${processing.storage.s3.region:}") String region) {
        this.bucket = bucket != null ? bucket.trim() : "";
        this.region = region != null ? region.trim() : "";
    }

    private S3Client getS3Client() {
        if (s3Client == null) {
            Region r = region.isBlank() ? Region.US_EAST_1 : Region.of(region);
            s3Client = S3Client.builder().region(r).build();
        }
        return s3Client;
    }

    @Override
    public Optional<String> uploadAndGetPublicUrl(Path localZipPath, String userUuid, String videoId) {
        if (bucket.isBlank()) {
            logger.debug("S3 bucket not configured, skipping upload");
            return Optional.empty();
        }

        String key = key(userUuid, videoId);
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType("application/zip")
                    .build();

            getS3Client().putObject(request, RequestBody.fromFile(localZipPath.toFile()));
            String publicUrl = publicUrl(key);
            logger.info("Uploaded zip to S3: {} -> {}", key, publicUrl);
            return Optional.of(publicUrl);
        } catch (Exception e) {
            logger.error("Failed to upload zip to S3: key={}", key, e);
            throw new StorageException("S3 upload failed: " + e.getMessage(), e);
        }
    }

    /** Chave no bucket: pasta = user UUID, arquivo = {videoId}.zip */
    private static String key(String userUuid, String videoId) {
        return userUuid + "/" + videoId + ".zip";
    }

    private String publicUrl(String key) {
        // https://{bucket}.s3.{region}.amazonaws.com/{key}
        String regionPart = region.isBlank() ? "" : "." + region;
        return "https://" + bucket + ".s3" + regionPart + ".amazonaws.com/" + key;
    }
}
