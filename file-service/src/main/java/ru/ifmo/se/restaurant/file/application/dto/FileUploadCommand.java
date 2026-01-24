package ru.ifmo.se.restaurant.file.application.dto;

import lombok.Builder;
import lombok.Value;
import ru.ifmo.se.restaurant.file.domain.valueobject.FileCategory;

import java.io.InputStream;

@Value
@Builder
public class FileUploadCommand {
    String originalFileName;
    String contentType;
    Long fileSize;
    InputStream inputStream;
    FileCategory category;
    Long entityId;
    String uploadedBy;
}
