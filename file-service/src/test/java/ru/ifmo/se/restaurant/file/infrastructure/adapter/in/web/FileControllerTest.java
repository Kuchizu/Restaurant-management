package ru.ifmo.se.restaurant.file.infrastructure.adapter.in.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import ru.ifmo.se.restaurant.file.application.dto.FileDownloadResult;
import ru.ifmo.se.restaurant.file.application.dto.FileResponse;
import ru.ifmo.se.restaurant.file.application.dto.FileUploadCommand;
import ru.ifmo.se.restaurant.file.application.port.in.DeleteFileUseCase;
import ru.ifmo.se.restaurant.file.application.port.in.DownloadFileUseCase;
import ru.ifmo.se.restaurant.file.application.port.in.GetFileMetadataUseCase;
import ru.ifmo.se.restaurant.file.application.port.in.UploadFileUseCase;
import ru.ifmo.se.restaurant.file.domain.valueobject.FileCategory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private UploadFileUseCase uploadFileUseCase;

    @Mock
    private DownloadFileUseCase downloadFileUseCase;

    @Mock
    private GetFileMetadataUseCase getFileMetadataUseCase;

    @Mock
    private DeleteFileUseCase deleteFileUseCase;

    @InjectMocks
    private FileController fileController;

    private FileResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = FileResponse.builder()
                .id("test-id")
                .originalName("test.jpg")
                .contentType("image/jpeg")
                .fileSize(1024L)
                .fileUrl("http://minio:9000/dish-images/test-id.jpg")
                .category(FileCategory.DISH_IMAGE)
                .entityId(1L)
                .uploadedAt(Instant.now())
                .uploadedBy("user1")
                .build();
    }

    @Test
    void upload_ShouldReturnFileResponse() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "test content".getBytes()
        );
        when(uploadFileUseCase.upload(any(FileUploadCommand.class))).thenReturn(sampleResponse);

        ResponseEntity<FileResponse> response = fileController.upload(
                file, FileCategory.DISH_IMAGE, 1L, "user1"
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test-id", response.getBody().getId());
        verify(uploadFileUseCase).upload(any(FileUploadCommand.class));
    }

    @Test
    void upload_WithoutEntityId_ShouldReturnFileResponse() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "test content".getBytes()
        );
        when(uploadFileUseCase.upload(any(FileUploadCommand.class))).thenReturn(sampleResponse);

        ResponseEntity<FileResponse> response = fileController.upload(
                file, FileCategory.DISH_IMAGE, null, null
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void download_ShouldReturnFileAsResource() {
        byte[] content = "file content".getBytes();
        FileDownloadResult downloadResult = FileDownloadResult.builder()
                .fileName("test.jpg")
                .contentType("image/jpeg")
                .fileSize((long) content.length)
                .inputStream(new ByteArrayInputStream(content))
                .build();
        when(downloadFileUseCase.download("test-id")).thenReturn(downloadResult);

        ResponseEntity<InputStreamResource> response = fileController.download("test-id");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getHeaders().getContentDisposition().toString().contains("test.jpg"));
        assertEquals("image/jpeg", response.getHeaders().getContentType().toString());
        assertEquals(content.length, response.getHeaders().getContentLength());
    }

    @Test
    void getMetadata_ShouldReturnFileResponse() {
        when(getFileMetadataUseCase.getById("test-id")).thenReturn(sampleResponse);

        ResponseEntity<FileResponse> response = fileController.getMetadata("test-id");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test-id", response.getBody().getId());
        assertEquals("test.jpg", response.getBody().getOriginalName());
    }

    @Test
    void getByCategory_ShouldReturnListOfFiles() {
        List<FileResponse> files = List.of(sampleResponse);
        when(getFileMetadataUseCase.getByCategory(FileCategory.DISH_IMAGE)).thenReturn(files);

        ResponseEntity<List<FileResponse>> response = fileController.getByCategory(FileCategory.DISH_IMAGE);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("test-id", response.getBody().get(0).getId());
    }

    @Test
    void getByCategory_ShouldReturnEmptyList_WhenNoFiles() {
        when(getFileMetadataUseCase.getByCategory(FileCategory.RECEIPT)).thenReturn(List.of());

        ResponseEntity<List<FileResponse>> response = fileController.getByCategory(FileCategory.RECEIPT);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getByEntityId_ShouldReturnListOfFiles() {
        List<FileResponse> files = List.of(sampleResponse);
        when(getFileMetadataUseCase.getByEntityId(1L)).thenReturn(files);

        ResponseEntity<List<FileResponse>> response = fileController.getByEntityId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getByEntityId_ShouldReturnEmptyList_WhenNoFiles() {
        when(getFileMetadataUseCase.getByEntityId(999L)).thenReturn(List.of());

        ResponseEntity<List<FileResponse>> response = fileController.getByEntityId(999L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void delete_ShouldReturnNoContent() {
        doNothing().when(deleteFileUseCase).delete("test-id");

        ResponseEntity<Void> response = fileController.delete("test-id");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(deleteFileUseCase).delete("test-id");
    }

    @Test
    void upload_WithDifferentCategories_ShouldWork() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "report.pdf", "application/pdf", "pdf content".getBytes()
        );
        FileResponse pdfResponse = FileResponse.builder()
                .id("pdf-id")
                .originalName("report.pdf")
                .contentType("application/pdf")
                .fileSize(100L)
                .category(FileCategory.REPORT)
                .build();
        when(uploadFileUseCase.upload(any(FileUploadCommand.class))).thenReturn(pdfResponse);

        ResponseEntity<FileResponse> response = fileController.upload(
                file, FileCategory.REPORT, null, "admin"
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(FileCategory.REPORT, response.getBody().getCategory());
    }
}
