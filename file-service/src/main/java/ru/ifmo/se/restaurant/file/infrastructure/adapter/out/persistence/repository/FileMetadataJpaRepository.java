package ru.ifmo.se.restaurant.file.infrastructure.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.se.restaurant.file.domain.valueobject.FileCategory;
import ru.ifmo.se.restaurant.file.infrastructure.adapter.out.persistence.entity.FileMetadataJpaEntity;

import java.util.List;

@Repository
public interface FileMetadataJpaRepository extends JpaRepository<FileMetadataJpaEntity, String> {
    List<FileMetadataJpaEntity> findByCategory(FileCategory category);
    List<FileMetadataJpaEntity> findByEntityId(Long entityId);
}
