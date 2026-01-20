package ru.ifmo.se.restaurant.file.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import ru.ifmo.se.restaurant.file.domain.valueobject.FileCategory;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class FileMetadata {
    private final String id;
    private final String originalName;
    private final String storedName;
    private final String contentType;
    private final Long fileSize;
    private final String bucket;
    private final String fileUrl;
    private final FileCategory category;
    private final Long entityId;
    private final Instant uploadedAt;
    private final String uploadedBy;

    public boolean isImage() {
        return contentType != null && contentType.startsWith("image/");
    }

    public String getFileExtension() {
        if (originalName == null || !originalName.contains(".")) {
            return "";
        }
        return originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
    }
}
