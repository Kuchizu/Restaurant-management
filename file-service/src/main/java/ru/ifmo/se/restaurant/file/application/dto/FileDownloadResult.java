package ru.ifmo.se.restaurant.file.application.dto;

import lombok.Builder;
import lombok.Value;

import java.io.InputStream;

@Value
@Builder
public class FileDownloadResult {
    String fileName;
    String contentType;
    Long fileSize;
    InputStream inputStream;
}
