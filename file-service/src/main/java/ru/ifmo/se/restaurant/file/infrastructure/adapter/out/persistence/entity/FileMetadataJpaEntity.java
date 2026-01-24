package ru.ifmo.se.restaurant.file.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.ifmo.se.restaurant.file.domain.entity.FileMetadata;
import ru.ifmo.se.restaurant.file.domain.valueobject.FileCategory;

import java.time.Instant;

@Entity
@Table(name = "file_metadata")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadataJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "stored_name", nullable = false)
    private String storedName;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "bucket", nullable = false, length = 100)
    private String bucket;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private FileCategory category;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    @Column(name = "uploaded_by", length = 100)
    private String uploadedBy;

    public static FileMetadataJpaEntity fromDomain(FileMetadata domain) {
        return FileMetadataJpaEntity.builder()
                .id(domain.getId())
                .originalName(domain.getOriginalName())
                .storedName(domain.getStoredName())
                .contentType(domain.getContentType())
                .fileSize(domain.getFileSize())
                .bucket(domain.getBucket())
                .fileUrl(domain.getFileUrl())
                .category(domain.getCategory())
                .entityId(domain.getEntityId())
                .uploadedAt(domain.getUploadedAt())
                .uploadedBy(domain.getUploadedBy())
                .build();
    }

    public FileMetadata toDomain() {
        return FileMetadata.builder()
                .id(id)
                .originalName(originalName)
                .storedName(storedName)
                .contentType(contentType)
                .fileSize(fileSize)
                .bucket(bucket)
                .fileUrl(fileUrl)
                .category(category)
                .entityId(entityId)
                .uploadedAt(uploadedAt)
                .uploadedBy(uploadedBy)
                .build();
    }
}
