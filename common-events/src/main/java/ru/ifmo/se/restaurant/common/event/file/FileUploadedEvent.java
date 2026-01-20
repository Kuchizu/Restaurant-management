package ru.ifmo.se.restaurant.common.event.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadedEvent {
    private String fileId;
    private String fileName;
    private String fileUrl;
    private String contentType;
    private Long fileSize;
    private String category;
    private Long entityId;
    private Instant uploadedAt;
}
