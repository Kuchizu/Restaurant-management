package ru.ifmo.se.restaurant.file.application.dto;

import lombok.Builder;
import lombok.Value;
import ru.ifmo.se.restaurant.file.domain.entity.FileMetadata;
import ru.ifmo.se.restaurant.file.domain.valueobject.FileCategory;

import java.time.Instant;

@Value
@Builder
public class FileResponse {
    String id;
    String originalName;
    String contentType;
    Long fileSize;
    String fileUrl;
    FileCategory category;
    Long entityId;
    Instant uploadedAt;
    String uploadedBy;

    public static FileResponse fromDomain(FileMetadata metadata) {
        return FileResponse.builder()
                .id(metadata.getId())
                .originalName(metadata.getOriginalName())
                .contentType(metadata.getContentType())
                .fileSize(metadata.getFileSize())
                .fileUrl(metadata.getFileUrl())
                .category(metadata.getCategory())
                .entityId(metadata.getEntityId())
                .uploadedAt(metadata.getUploadedAt())
                .uploadedBy(metadata.getUploadedBy())
                .build();
    }
}
