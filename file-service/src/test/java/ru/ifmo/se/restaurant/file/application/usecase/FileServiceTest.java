package ru.ifmo.se.restaurant.file.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ifmo.se.restaurant.file.application.dto.FileDownloadResult;
import ru.ifmo.se.restaurant.file.application.dto.FileResponse;
import ru.ifmo.se.restaurant.file.application.dto.FileUploadCommand;
import ru.ifmo.se.restaurant.file.application.port.out.FileEventPublisher;
import ru.ifmo.se.restaurant.file.application.port.out.FileMetadataRepository;
import ru.ifmo.se.restaurant.file.application.port.out.FileStoragePort;
import ru.ifmo.se.restaurant.file.domain.entity.FileMetadata;
import ru.ifmo.se.restaurant.file.domain.exception.FileNotFoundException;
import ru.ifmo.se.restaurant.file.domain.exception.InvalidFileException;
import ru.ifmo.se.restaurant.file.domain.valueobject.FileCategory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private FileStoragePort fileStorage;

    @Mock
    private FileMetadataRepository metadataRepository;

    @Mock
    private FileEventPublisher eventPublisher;

    @InjectMocks
    private FileService fileService;

    private FileMetadata testMetadata;
    private FileUploadCommand testCommand;

    @BeforeEach
    void setUp() {
        testMetadata = FileMetadata.builder()
                .id("test-file-id")
                .originalName("test.jpg")
                .storedName("test-file-id.jpg")
                .contentType("image/jpeg")
                .fileSize(1024L)
                .bucket("dish-images")
                .fileUrl("http://minio:9000/dish-images/test-file-id.jpg")
                .category(FileCategory.DISH_IMAGE)
                .entityId(1L)
                .uploadedAt(Instant.now())
                .uploadedBy("user1")
                .build();

        testCommand = FileUploadCommand.builder()
                .originalFileName("test.jpg")
                .contentType("image/jpeg")
                .fileSize(1024L)
                .inputStream(new ByteArrayInputStream(new byte[1024]))
                .category(FileCategory.DISH_IMAGE)
                .entityId(1L)
                .uploadedBy("user1")
                .build();
    }

    @Test
    void upload_ShouldUploadFileSuccessfully() {
        when(fileStorage.store(anyString(), any(InputStream.class), anyString(), anyLong()))
                .thenReturn("http://minio:9000/dish-images/test.jpg");
        when(metadataRepository.save(any(FileMetadata.class))).thenReturn(testMetadata);
        doNothing().when(eventPublisher).publishFileUploaded(any(FileMetadata.class));

        FileResponse response = fileService.upload(testCommand);

        assertNotNull(response);
        assertEquals("test-file-id", response.getId());
        assertEquals("test.jpg", response.getOriginalName());
        verify(fileStorage).store(anyString(), any(InputStream.class), eq("image/jpeg"), eq(1024L));
        verify(metadataRepository).save(any(FileMetadata.class));
        verify(eventPublisher).publishFileUploaded(any(FileMetadata.class));
    }

    @Test
    void upload_ShouldThrowException_WhenFileTooLarge() {
        FileUploadCommand largeFile = FileUploadCommand.builder()
                .originalFileName("large.jpg")
                .contentType("image/jpeg")
                .fileSize(11 * 1024 * 1024L) // 11MB
                .inputStream(new ByteArrayInputStream(new byte[0]))
                .category(FileCategory.DISH_IMAGE)
                .build();

        assertThrows(InvalidFileException.class, () -> fileService.upload(largeFile));
        verify(fileStorage, never()).store(anyString(), any(InputStream.class), anyString(), anyLong());
    }

    @Test
    void upload_ShouldThrowException_WhenInvalidImageType() {
        FileUploadCommand invalidType = FileUploadCommand.builder()
                .originalFileName("test.pdf")
                .contentType("application/pdf")
                .fileSize(1024L)
                .inputStream(new ByteArrayInputStream(new byte[1024]))
                .category(FileCategory.DISH_IMAGE)
                .build();

        assertThrows(InvalidFileException.class, () -> fileService.upload(invalidType));
        verify(fileStorage, never()).store(anyString(), any(InputStream.class), anyString(), anyLong());
    }

    @Test
    void upload_ShouldAllowPdf_ForReceiptCategory() {
        FileUploadCommand pdfReceipt = FileUploadCommand.builder()
                .originalFileName("receipt.pdf")
                .contentType("application/pdf")
                .fileSize(1024L)
                .inputStream(new ByteArrayInputStream(new byte[1024]))
                .category(FileCategory.RECEIPT)
                .build();

        when(fileStorage.store(anyString(), any(InputStream.class), anyString(), anyLong()))
                .thenReturn("http://minio:9000/documents/receipt.pdf");
        when(metadataRepository.save(any(FileMetadata.class))).thenReturn(testMetadata);
        doNothing().when(eventPublisher).publishFileUploaded(any(FileMetadata.class));

        FileResponse response = fileService.upload(pdfReceipt);

        assertNotNull(response);
        verify(fileStorage).store(anyString(), any(InputStream.class), eq("application/pdf"), eq(1024L));
    }

    @Test
    void download_ShouldReturnFileDownloadResult() {
        when(metadataRepository.findById("test-file-id")).thenReturn(Optional.of(testMetadata));
        when(fileStorage.retrieve("test-file-id.jpg")).thenReturn(new ByteArrayInputStream(new byte[1024]));

        FileDownloadResult result = fileService.download("test-file-id");

        assertNotNull(result);
        assertEquals("test.jpg", result.getFileName());
        assertEquals("image/jpeg", result.getContentType());
        assertEquals(1024L, result.getFileSize());
        assertNotNull(result.getInputStream());
    }

    @Test
    void download_ShouldThrowException_WhenFileNotFound() {
        when(metadataRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThrows(FileNotFoundException.class, () -> fileService.download("non-existent"));
    }

    @Test
    void getById_ShouldReturnFileResponse() {
        when(metadataRepository.findById("test-file-id")).thenReturn(Optional.of(testMetadata));

        FileResponse response = fileService.getById("test-file-id");

        assertNotNull(response);
        assertEquals("test-file-id", response.getId());
        assertEquals("test.jpg", response.getOriginalName());
    }

    @Test
    void getById_ShouldThrowException_WhenFileNotFound() {
        when(metadataRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThrows(FileNotFoundException.class, () -> fileService.getById("non-existent"));
    }

    @Test
    void getByCategory_ShouldReturnListOfFiles() {
        when(metadataRepository.findByCategory(FileCategory.DISH_IMAGE))
                .thenReturn(List.of(testMetadata));

        List<FileResponse> responses = fileService.getByCategory(FileCategory.DISH_IMAGE);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("test-file-id", responses.get(0).getId());
    }

    @Test
    void getByCategory_ShouldReturnEmptyList_WhenNoFilesFound() {
        when(metadataRepository.findByCategory(FileCategory.REPORT)).thenReturn(List.of());

        List<FileResponse> responses = fileService.getByCategory(FileCategory.REPORT);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void getByEntityId_ShouldReturnListOfFiles() {
        when(metadataRepository.findByEntityId(1L)).thenReturn(List.of(testMetadata));

        List<FileResponse> responses = fileService.getByEntityId(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).getEntityId());
    }

    @Test
    void delete_ShouldDeleteFileSuccessfully() {
        when(metadataRepository.findById("test-file-id")).thenReturn(Optional.of(testMetadata));
        doNothing().when(fileStorage).delete("test-file-id.jpg");
        doNothing().when(metadataRepository).deleteById("test-file-id");

        assertDoesNotThrow(() -> fileService.delete("test-file-id"));

        verify(fileStorage).delete("test-file-id.jpg");
        verify(metadataRepository).deleteById("test-file-id");
    }

    @Test
    void delete_ShouldThrowException_WhenFileNotFound() {
        when(metadataRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThrows(FileNotFoundException.class, () -> fileService.delete("non-existent"));
        verify(fileStorage, never()).delete(anyString());
    }

    @Test
    void upload_ShouldHandleFileWithoutExtension() {
        FileUploadCommand noExtension = FileUploadCommand.builder()
                .originalFileName("testfile")
                .contentType("application/octet-stream")
                .fileSize(1024L)
                .inputStream(new ByteArrayInputStream(new byte[1024]))
                .category(FileCategory.OTHER)
                .build();

        when(fileStorage.store(anyString(), any(InputStream.class), anyString(), anyLong()))
                .thenReturn("http://minio:9000/general/testfile");
        when(metadataRepository.save(any(FileMetadata.class))).thenReturn(testMetadata);
        doNothing().when(eventPublisher).publishFileUploaded(any(FileMetadata.class));

        FileResponse response = fileService.upload(noExtension);

        assertNotNull(response);
    }
}
