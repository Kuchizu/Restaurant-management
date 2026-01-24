package ru.ifmo.se.restaurant.file.infrastructure.adapter.out.storage;

import io.minio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ifmo.se.restaurant.file.domain.exception.FileUploadException;
import ru.ifmo.se.restaurant.file.infrastructure.config.MinioProperties;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioFileStorageAdapterTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private MinioProperties minioProperties;

    private MinioFileStorageAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new MinioFileStorageAdapter(minioClient, minioProperties);
    }

    @Test
    void store_ShouldStoreFileSuccessfully() throws Exception {
        when(minioProperties.getBucket()).thenReturn("test-bucket");
        when(minioProperties.getEndpoint()).thenReturn("http://minio:9000");
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());
        String result = adapter.store("test-file.jpg", inputStream, "image/jpeg", 12L);

        assertEquals("http://minio:9000/test-bucket/test-file.jpg", result);
        verify(minioClient).bucketExists(any(BucketExistsArgs.class));
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void store_ShouldCreateBucketIfNotExists() throws Exception {
        when(minioProperties.getBucket()).thenReturn("new-bucket");
        when(minioProperties.getEndpoint()).thenReturn("http://minio:9000");
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);
        doNothing().when(minioClient).makeBucket(any(MakeBucketArgs.class));
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        InputStream inputStream = new ByteArrayInputStream("test".getBytes());
        String result = adapter.store("test.jpg", inputStream, "image/jpeg", 4L);

        assertNotNull(result);
        verify(minioClient).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    void store_ShouldThrowException_OnError() throws Exception {
        when(minioProperties.getBucket()).thenReturn("bucket");
        when(minioClient.bucketExists(any(BucketExistsArgs.class)))
                .thenThrow(new RuntimeException("Connection failed"));

        InputStream inputStream = new ByteArrayInputStream("test".getBytes());
        assertThrows(FileUploadException.class,
                () -> adapter.store("test.jpg", inputStream, "image/jpeg", 4L));
    }

    @Test
    void retrieve_ShouldReturnInputStream() throws Exception {
        when(minioProperties.getBucket()).thenReturn("bucket");
        GetObjectResponse mockResponse = mock(GetObjectResponse.class);
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(mockResponse);

        InputStream result = adapter.retrieve("stored-name.jpg");

        assertNotNull(result);
        verify(minioClient).getObject(any(GetObjectArgs.class));
    }

    @Test
    void retrieve_ShouldThrowException_OnError() throws Exception {
        when(minioProperties.getBucket()).thenReturn("bucket");
        when(minioClient.getObject(any(GetObjectArgs.class)))
                .thenThrow(new RuntimeException("File not found"));

        assertThrows(FileUploadException.class,
                () -> adapter.retrieve("nonexistent.jpg"));
    }

    @Test
    void delete_ShouldDeleteFileSuccessfully() throws Exception {
        when(minioProperties.getBucket()).thenReturn("bucket");
        doNothing().when(minioClient).removeObject(any(RemoveObjectArgs.class));

        assertDoesNotThrow(() -> adapter.delete("file-to-delete.jpg"));
        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void delete_ShouldThrowException_OnError() throws Exception {
        when(minioProperties.getBucket()).thenReturn("bucket");
        doThrow(new RuntimeException("Delete failed")).when(minioClient).removeObject(any(RemoveObjectArgs.class));

        assertThrows(FileUploadException.class, () -> adapter.delete("file.jpg"));
    }

    @Test
    void getFileUrl_ShouldReturnCorrectUrl() {
        when(minioProperties.getEndpoint()).thenReturn("http://minio:9000");
        when(minioProperties.getBucket()).thenReturn("my-bucket");

        String url = adapter.getFileUrl("image.jpg");

        assertEquals("http://minio:9000/my-bucket/image.jpg", url);
    }

    @Test
    void getFileUrl_ShouldHandleSpecialCharacters() {
        when(minioProperties.getEndpoint()).thenReturn("http://localhost:9000");
        when(minioProperties.getBucket()).thenReturn("files");

        String url = adapter.getFileUrl("path/to/file.jpg");

        assertEquals("http://localhost:9000/files/path/to/file.jpg", url);
    }
}
