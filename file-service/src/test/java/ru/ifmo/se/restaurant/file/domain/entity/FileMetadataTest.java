package ru.ifmo.se.restaurant.file.domain.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.ifmo.se.restaurant.file.domain.valueobject.FileCategory;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class FileMetadataTest {

    @Test
    void builder_ShouldCreateFileMetadataWithAllFields() {
        Instant now = Instant.now();

        FileMetadata metadata = FileMetadata.builder()
                .id("test-id")
                .originalName("test.jpg")
                .storedName("test-id.jpg")
                .contentType("image/jpeg")
                .fileSize(1024L)
                .bucket("dish-images")
                .fileUrl("http://minio:9000/dish-images/test-id.jpg")
                .category(FileCategory.DISH_IMAGE)
                .entityId(1L)
                .uploadedAt(now)
                .uploadedBy("user1")
                .build();

        assertEquals("test-id", metadata.getId());
        assertEquals("test.jpg", metadata.getOriginalName());
        assertEquals("test-id.jpg", metadata.getStoredName());
        assertEquals("image/jpeg", metadata.getContentType());
        assertEquals(1024L, metadata.getFileSize());
        assertEquals("dish-images", metadata.getBucket());
        assertEquals("http://minio:9000/dish-images/test-id.jpg", metadata.getFileUrl());
        assertEquals(FileCategory.DISH_IMAGE, metadata.getCategory());
        assertEquals(1L, metadata.getEntityId());
        assertEquals(now, metadata.getUploadedAt());
        assertEquals("user1", metadata.getUploadedBy());
    }

    @ParameterizedTest
    @ValueSource(strings = {"image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml"})
    void isImage_ShouldReturnTrue_WhenContentTypeStartsWithImage(String contentType) {
        FileMetadata metadata = FileMetadata.builder()
                .contentType(contentType)
                .build();

        assertTrue(metadata.isImage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"application/pdf", "text/plain", "video/mp4", "audio/mp3"})
    void isImage_ShouldReturnFalse_WhenContentTypeDoesNotStartWithImage(String contentType) {
        FileMetadata metadata = FileMetadata.builder()
                .contentType(contentType)
                .build();

        assertFalse(metadata.isImage());
    }

    @Test
    void isImage_ShouldReturnFalse_WhenContentTypeIsNull() {
        FileMetadata metadata = FileMetadata.builder()
                .contentType(null)
                .build();

        assertFalse(metadata.isImage());
    }

    @ParameterizedTest
    @CsvSource({
            "test.jpg, jpg",
            "document.PDF, pdf",
            "image.PNG, png",
            "file.tar.gz, gz",
            "UPPERCASE.JPEG, jpeg"
    })
    void getFileExtension_ShouldReturnLowercaseExtension(String fileName, String expectedExtension) {
        FileMetadata metadata = FileMetadata.builder()
                .originalName(fileName)
                .build();

        assertEquals(expectedExtension, metadata.getFileExtension());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"filename", "noextension"})
    void getFileExtension_ShouldReturnEmptyString_WhenNoExtension(String fileName) {
        FileMetadata metadata = FileMetadata.builder()
                .originalName(fileName)
                .build();

        assertEquals("", metadata.getFileExtension());
    }

    @Test
    void getFileExtension_ShouldReturnEmptyString_WhenDotAtEnd() {
        FileMetadata metadata = FileMetadata.builder()
                .originalName("filename.")
                .build();

        assertEquals("", metadata.getFileExtension());
    }

    @Test
    void builder_ShouldAllowNullValues() {
        FileMetadata metadata = FileMetadata.builder()
                .id(null)
                .originalName(null)
                .storedName(null)
                .contentType(null)
                .fileSize(null)
                .bucket(null)
                .fileUrl(null)
                .category(null)
                .entityId(null)
                .uploadedAt(null)
                .uploadedBy(null)
                .build();

        assertNull(metadata.getId());
        assertNull(metadata.getOriginalName());
        assertNull(metadata.getContentType());
        assertNull(metadata.getCategory());
    }

    @Test
    void constructor_ShouldCreateFileMetadataWithAllArgsConstructor() {
        Instant now = Instant.now();

        FileMetadata metadata = new FileMetadata(
                "id", "original.jpg", "stored.jpg", "image/jpeg",
                1024L, "bucket", "http://url", FileCategory.DISH_IMAGE,
                1L, now, "user"
        );

        assertEquals("id", metadata.getId());
        assertEquals("original.jpg", metadata.getOriginalName());
        assertEquals("stored.jpg", metadata.getStoredName());
        assertEquals("image/jpeg", metadata.getContentType());
        assertEquals(1024L, metadata.getFileSize());
        assertEquals("bucket", metadata.getBucket());
        assertEquals("http://url", metadata.getFileUrl());
        assertEquals(FileCategory.DISH_IMAGE, metadata.getCategory());
        assertEquals(1L, metadata.getEntityId());
        assertEquals(now, metadata.getUploadedAt());
        assertEquals("user", metadata.getUploadedBy());
    }
}
