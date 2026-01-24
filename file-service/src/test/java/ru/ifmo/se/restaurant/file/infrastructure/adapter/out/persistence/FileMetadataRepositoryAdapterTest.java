package ru.ifmo.se.restaurant.file.infrastructure.adapter.out.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ifmo.se.restaurant.file.domain.entity.FileMetadata;
import ru.ifmo.se.restaurant.file.domain.valueobject.FileCategory;
import ru.ifmo.se.restaurant.file.infrastructure.adapter.out.persistence.entity.FileMetadataJpaEntity;
import ru.ifmo.se.restaurant.file.infrastructure.adapter.out.persistence.repository.FileMetadataJpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileMetadataRepositoryAdapterTest {

    @Mock
    private FileMetadataJpaRepository jpaRepository;

    @InjectMocks
    private FileMetadataRepositoryAdapter adapter;

    private FileMetadata sampleMetadata;
    private FileMetadataJpaEntity sampleEntity;
    private Instant now;

    @BeforeEach
    void setUp() {
        now = Instant.now();

        sampleMetadata = FileMetadata.builder()
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

        sampleEntity = FileMetadataJpaEntity.builder()
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
    }

    @Test
    void save_ShouldSaveAndReturnFileMetadata() {
        when(jpaRepository.save(any(FileMetadataJpaEntity.class))).thenReturn(sampleEntity);

        FileMetadata result = adapter.save(sampleMetadata);

        assertNotNull(result);
        assertEquals("test-id", result.getId());
        assertEquals("test.jpg", result.getOriginalName());
        assertEquals(FileCategory.DISH_IMAGE, result.getCategory());
        verify(jpaRepository).save(any(FileMetadataJpaEntity.class));
    }

    @Test
    void findById_ShouldReturnFileMetadata_WhenFound() {
        when(jpaRepository.findById("test-id")).thenReturn(Optional.of(sampleEntity));

        Optional<FileMetadata> result = adapter.findById("test-id");

        assertTrue(result.isPresent());
        assertEquals("test-id", result.get().getId());
        assertEquals("test.jpg", result.get().getOriginalName());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotFound() {
        when(jpaRepository.findById("nonexistent")).thenReturn(Optional.empty());

        Optional<FileMetadata> result = adapter.findById("nonexistent");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByCategory_ShouldReturnListOfFiles() {
        List<FileMetadataJpaEntity> entities = List.of(sampleEntity);
        when(jpaRepository.findByCategory(FileCategory.DISH_IMAGE)).thenReturn(entities);

        List<FileMetadata> result = adapter.findByCategory(FileCategory.DISH_IMAGE);

        assertEquals(1, result.size());
        assertEquals("test-id", result.get(0).getId());
    }

    @Test
    void findByCategory_ShouldReturnEmptyList_WhenNoFiles() {
        when(jpaRepository.findByCategory(FileCategory.RECEIPT)).thenReturn(List.of());

        List<FileMetadata> result = adapter.findByCategory(FileCategory.RECEIPT);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByEntityId_ShouldReturnListOfFiles() {
        List<FileMetadataJpaEntity> entities = List.of(sampleEntity);
        when(jpaRepository.findByEntityId(1L)).thenReturn(entities);

        List<FileMetadata> result = adapter.findByEntityId(1L);

        assertEquals(1, result.size());
        assertEquals("test-id", result.get(0).getId());
        assertEquals(1L, result.get(0).getEntityId());
    }

    @Test
    void findByEntityId_ShouldReturnEmptyList_WhenNoFiles() {
        when(jpaRepository.findByEntityId(999L)).thenReturn(List.of());

        List<FileMetadata> result = adapter.findByEntityId(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void deleteById_ShouldCallRepositoryDelete() {
        doNothing().when(jpaRepository).deleteById("test-id");

        adapter.deleteById("test-id");

        verify(jpaRepository).deleteById("test-id");
    }

    @Test
    void findByCategory_WithMultipleFiles_ShouldReturnAll() {
        FileMetadataJpaEntity entity2 = FileMetadataJpaEntity.builder()
                .id("test-id-2")
                .originalName("test2.jpg")
                .storedName("test-id-2.jpg")
                .contentType("image/jpeg")
                .fileSize(2048L)
                .bucket("dish-images")
                .fileUrl("http://minio:9000/dish-images/test-id-2.jpg")
                .category(FileCategory.DISH_IMAGE)
                .entityId(2L)
                .uploadedAt(now)
                .uploadedBy("user2")
                .build();

        List<FileMetadataJpaEntity> entities = List.of(sampleEntity, entity2);
        when(jpaRepository.findByCategory(FileCategory.DISH_IMAGE)).thenReturn(entities);

        List<FileMetadata> result = adapter.findByCategory(FileCategory.DISH_IMAGE);

        assertEquals(2, result.size());
    }
}
