package ru.ifmo.se.restaurant.file.infrastructure.adapter.out.persistence.entity;

import org.junit.jupiter.api.Test;
import ru.ifmo.se.restaurant.file.domain.entity.FileMetadata;
import ru.ifmo.se.restaurant.file.domain.valueobject.FileCategory;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class FileMetadataJpaEntityTest {

    @Test
    void fromDomain_ShouldMapAllFields() {
        Instant now = Instant.now();
        FileMetadata domain = FileMetadata.builder()
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

        FileMetadataJpaEntity entity = FileMetadataJpaEntity.fromDomain(domain);

        assertEquals("test-id", entity.getId());
        assertEquals("test.jpg", entity.getOriginalName());
        assertEquals("test-id.jpg", entity.getStoredName());
        assertEquals("image/jpeg", entity.getContentType());
        assertEquals(1024L, entity.getFileSize());
        assertEquals("dish-images", entity.getBucket());
        assertEquals("http://minio:9000/dish-images/test-id.jpg", entity.getFileUrl());
        assertEquals(FileCategory.DISH_IMAGE, entity.getCategory());
        assertEquals(1L, entity.getEntityId());
        assertEquals(now, entity.getUploadedAt());
        assertEquals("user1", entity.getUploadedBy());
    }

    @Test
    void toDomain_ShouldMapAllFields() {
        Instant now = Instant.now();
        FileMetadataJpaEntity entity = FileMetadataJpaEntity.builder()
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

        FileMetadata domain = entity.toDomain();

        assertEquals("test-id", domain.getId());
        assertEquals("test.jpg", domain.getOriginalName());
        assertEquals("test-id.jpg", domain.getStoredName());
        assertEquals("image/jpeg", domain.getContentType());
        assertEquals(1024L, domain.getFileSize());
        assertEquals("dish-images", domain.getBucket());
        assertEquals("http://minio:9000/dish-images/test-id.jpg", domain.getFileUrl());
        assertEquals(FileCategory.DISH_IMAGE, domain.getCategory());
        assertEquals(1L, domain.getEntityId());
        assertEquals(now, domain.getUploadedAt());
        assertEquals("user1", domain.getUploadedBy());
    }

    @Test
    void fromDomain_AndToDomain_ShouldBeSymmetric() {
        Instant now = Instant.now();
        FileMetadata original = FileMetadata.builder()
                .id("test-id")
                .originalName("document.pdf")
                .storedName("test-id.pdf")
                .contentType("application/pdf")
                .fileSize(5000L)
                .bucket("receipts")
                .fileUrl("http://minio:9000/receipts/test-id.pdf")
                .category(FileCategory.RECEIPT)
                .entityId(100L)
                .uploadedAt(now)
                .uploadedBy("admin")
                .build();

        FileMetadataJpaEntity entity = FileMetadataJpaEntity.fromDomain(original);
        FileMetadata converted = entity.toDomain();

        assertEquals(original.getId(), converted.getId());
        assertEquals(original.getOriginalName(), converted.getOriginalName());
        assertEquals(original.getStoredName(), converted.getStoredName());
        assertEquals(original.getContentType(), converted.getContentType());
        assertEquals(original.getFileSize(), converted.getFileSize());
        assertEquals(original.getBucket(), converted.getBucket());
        assertEquals(original.getFileUrl(), converted.getFileUrl());
        assertEquals(original.getCategory(), converted.getCategory());
        assertEquals(original.getEntityId(), converted.getEntityId());
        assertEquals(original.getUploadedAt(), converted.getUploadedAt());
        assertEquals(original.getUploadedBy(), converted.getUploadedBy());
    }

    @Test
    void noArgsConstructor_ShouldCreateEmptyEntity() {
        FileMetadataJpaEntity entity = new FileMetadataJpaEntity();
        assertNull(entity.getId());
        assertNull(entity.getOriginalName());
    }

    @Test
    void setters_ShouldUpdateFields() {
        FileMetadataJpaEntity entity = new FileMetadataJpaEntity();
        Instant now = Instant.now();

        entity.setId("new-id");
        entity.setOriginalName("new.jpg");
        entity.setStoredName("new-id.jpg");
        entity.setContentType("image/png");
        entity.setFileSize(2048L);
        entity.setBucket("new-bucket");
        entity.setFileUrl("http://url");
        entity.setCategory(FileCategory.CATEGORY_IMAGE);
        entity.setEntityId(5L);
        entity.setUploadedAt(now);
        entity.setUploadedBy("user");

        assertEquals("new-id", entity.getId());
        assertEquals("new.jpg", entity.getOriginalName());
        assertEquals("new-id.jpg", entity.getStoredName());
        assertEquals("image/png", entity.getContentType());
        assertEquals(2048L, entity.getFileSize());
        assertEquals("new-bucket", entity.getBucket());
        assertEquals("http://url", entity.getFileUrl());
        assertEquals(FileCategory.CATEGORY_IMAGE, entity.getCategory());
        assertEquals(5L, entity.getEntityId());
        assertEquals(now, entity.getUploadedAt());
        assertEquals("user", entity.getUploadedBy());
    }

    @Test
    void allArgsConstructor_ShouldSetAllFields() {
        Instant now = Instant.now();
        FileMetadataJpaEntity entity = new FileMetadataJpaEntity(
                "id", "name.jpg", "stored.jpg", "image/jpeg",
                1024L, "bucket", "http://url", FileCategory.OTHER,
                10L, now, "uploader"
        );

        assertEquals("id", entity.getId());
        assertEquals("name.jpg", entity.getOriginalName());
        assertEquals("stored.jpg", entity.getStoredName());
        assertEquals("image/jpeg", entity.getContentType());
        assertEquals(1024L, entity.getFileSize());
        assertEquals("bucket", entity.getBucket());
        assertEquals("http://url", entity.getFileUrl());
        assertEquals(FileCategory.OTHER, entity.getCategory());
        assertEquals(10L, entity.getEntityId());
        assertEquals(now, entity.getUploadedAt());
        assertEquals("uploader", entity.getUploadedBy());
    }

    @Test
    void fromDomain_WithNullEntityId_ShouldMapCorrectly() {
        FileMetadata domain = FileMetadata.builder()
                .id("test-id")
                .originalName("test.jpg")
                .storedName("test-id.jpg")
                .contentType("image/jpeg")
                .fileSize(1024L)
                .bucket("dish-images")
                .fileUrl("http://url")
                .category(FileCategory.DISH_IMAGE)
                .entityId(null)
                .uploadedAt(Instant.now())
                .uploadedBy(null)
                .build();

        FileMetadataJpaEntity entity = FileMetadataJpaEntity.fromDomain(domain);

        assertNull(entity.getEntityId());
        assertNull(entity.getUploadedBy());
    }
}
