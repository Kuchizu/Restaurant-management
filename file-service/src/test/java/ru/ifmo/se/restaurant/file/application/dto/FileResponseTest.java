package ru.ifmo.se.restaurant.file.application.dto;

import org.junit.jupiter.api.Test;
import ru.ifmo.se.restaurant.file.domain.entity.FileMetadata;
import ru.ifmo.se.restaurant.file.domain.valueobject.FileCategory;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class FileResponseTest {

    @Test
    void fromDomain_ShouldMapAllFields() {
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

        FileResponse response = FileResponse.fromDomain(metadata);

        assertEquals("test-id", response.getId());
        assertEquals("test.jpg", response.getOriginalName());
        assertEquals("image/jpeg", response.getContentType());
        assertEquals(1024L, response.getFileSize());
        assertEquals("http://minio:9000/dish-images/test-id.jpg", response.getFileUrl());
        assertEquals(FileCategory.DISH_IMAGE, response.getCategory());
        assertEquals(1L, response.getEntityId());
        assertEquals(now, response.getUploadedAt());
        assertEquals("user1", response.getUploadedBy());
    }

    @Test
    void builder_ShouldCreateFileResponse() {
        Instant now = Instant.now();
        FileResponse response = FileResponse.builder()
                .id("id")
                .originalName("name.png")
                .contentType("image/png")
                .fileSize(2048L)
                .fileUrl("http://url")
                .category(FileCategory.CATEGORY_IMAGE)
                .entityId(5L)
                .uploadedAt(now)
                .uploadedBy("admin")
                .build();

        assertEquals("id", response.getId());
        assertEquals("name.png", response.getOriginalName());
        assertEquals("image/png", response.getContentType());
        assertEquals(2048L, response.getFileSize());
        assertEquals("http://url", response.getFileUrl());
        assertEquals(FileCategory.CATEGORY_IMAGE, response.getCategory());
        assertEquals(5L, response.getEntityId());
        assertEquals(now, response.getUploadedAt());
        assertEquals("admin", response.getUploadedBy());
    }

    @Test
    void fromDomain_WithNullOptionalFields_ShouldMapCorrectly() {
        FileMetadata metadata = FileMetadata.builder()
                .id("test-id")
                .originalName("test.jpg")
                .contentType("image/jpeg")
                .fileSize(1024L)
                .fileUrl("http://url")
                .category(FileCategory.DISH_IMAGE)
                .entityId(null)
                .uploadedAt(Instant.now())
                .uploadedBy(null)
                .build();

        FileResponse response = FileResponse.fromDomain(metadata);

        assertNull(response.getEntityId());
        assertNull(response.getUploadedBy());
    }

    @Test
    void equals_ShouldWorkCorrectly() {
        Instant now = Instant.now();
        FileResponse response1 = FileResponse.builder()
                .id("id")
                .originalName("name.jpg")
                .contentType("image/jpeg")
                .fileSize(1024L)
                .fileUrl("http://url")
                .category(FileCategory.DISH_IMAGE)
                .entityId(1L)
                .uploadedAt(now)
                .uploadedBy("user")
                .build();

        FileResponse response2 = FileResponse.builder()
                .id("id")
                .originalName("name.jpg")
                .contentType("image/jpeg")
                .fileSize(1024L)
                .fileUrl("http://url")
                .category(FileCategory.DISH_IMAGE)
                .entityId(1L)
                .uploadedAt(now)
                .uploadedBy("user")
                .build();

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void toString_ShouldReturnStringRepresentation() {
        FileResponse response = FileResponse.builder()
                .id("test-id")
                .originalName("test.jpg")
                .build();

        String str = response.toString();
        assertTrue(str.contains("test-id"));
        assertTrue(str.contains("test.jpg"));
    }
}
