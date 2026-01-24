package ru.ifmo.se.restaurant.file.application.port.out;

import ru.ifmo.se.restaurant.file.domain.entity.FileMetadata;
import ru.ifmo.se.restaurant.file.domain.valueobject.FileCategory;

import java.util.List;
import java.util.Optional;

public interface FileMetadataRepository {
    FileMetadata save(FileMetadata fileMetadata);
    Optional<FileMetadata> findById(String id);
    List<FileMetadata> findByCategory(FileCategory category);
    List<FileMetadata> findByEntityId(Long entityId);
    void deleteById(String id);
}
