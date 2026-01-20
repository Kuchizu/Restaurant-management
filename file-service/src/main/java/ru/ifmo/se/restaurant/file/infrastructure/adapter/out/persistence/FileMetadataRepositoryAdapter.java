package ru.ifmo.se.restaurant.file.infrastructure.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.file.application.port.out.FileMetadataRepository;
import ru.ifmo.se.restaurant.file.domain.entity.FileMetadata;
import ru.ifmo.se.restaurant.file.domain.valueobject.FileCategory;
import ru.ifmo.se.restaurant.file.infrastructure.adapter.out.persistence.entity.FileMetadataJpaEntity;
import ru.ifmo.se.restaurant.file.infrastructure.adapter.out.persistence.repository.FileMetadataJpaRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FileMetadataRepositoryAdapter implements FileMetadataRepository {

    private final FileMetadataJpaRepository jpaRepository;

    @Override
    public FileMetadata save(FileMetadata fileMetadata) {
        FileMetadataJpaEntity entity = FileMetadataJpaEntity.fromDomain(fileMetadata);
        FileMetadataJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<FileMetadata> findById(String id) {
        return jpaRepository.findById(id)
                .map(FileMetadataJpaEntity::toDomain);
    }

    @Override
    public List<FileMetadata> findByCategory(FileCategory category) {
        return jpaRepository.findByCategory(category).stream()
                .map(FileMetadataJpaEntity::toDomain)
                .toList();
    }

    @Override
    public List<FileMetadata> findByEntityId(Long entityId) {
        return jpaRepository.findByEntityId(entityId).stream()
                .map(FileMetadataJpaEntity::toDomain)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }
}
