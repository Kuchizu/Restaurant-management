package ru.ifmo.se.restaurant.file.infrastructure.adapter.out.storage;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.file.application.port.out.FileStoragePort;
import ru.ifmo.se.restaurant.file.domain.exception.FileUploadException;
import ru.ifmo.se.restaurant.file.infrastructure.config.MinioProperties;

import java.io.InputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioFileStorageAdapter implements FileStoragePort {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    @Override
    public String store(String fileName, InputStream inputStream, String contentType, long size) {
        try {
            ensureBucketExists();

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(fileName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build());

            log.debug("File stored in MinIO: {}", fileName);
            return getFileUrl(fileName);

        } catch (Exception e) {
            log.error("Failed to store file in MinIO: {}", fileName, e);
            throw new FileUploadException("Failed to store file: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream retrieve(String storedName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(storedName)
                    .build());
        } catch (Exception e) {
            log.error("Failed to retrieve file from MinIO: {}", storedName, e);
            throw new FileUploadException("Failed to retrieve file: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String storedName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(storedName)
                    .build());
            log.debug("File deleted from MinIO: {}", storedName);
        } catch (Exception e) {
            log.error("Failed to delete file from MinIO: {}", storedName, e);
            throw new FileUploadException("Failed to delete file: " + e.getMessage(), e);
        }
    }

    @Override
    public String getFileUrl(String storedName) {
        return String.format("%s/%s/%s",
                minioProperties.getEndpoint(),
                minioProperties.getBucket(),
                storedName);
    }

    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .build());

            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .build());
                log.info("Created MinIO bucket: {}", minioProperties.getBucket());
            }
        } catch (Exception e) {
            log.error("Failed to ensure bucket exists", e);
            throw new FileUploadException("Failed to ensure bucket exists: " + e.getMessage(), e);
        }
    }
}
