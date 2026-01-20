package ru.ifmo.se.restaurant.file.application.port.in;

import ru.ifmo.se.restaurant.file.application.dto.FileResponse;
import ru.ifmo.se.restaurant.file.domain.valueobject.FileCategory;

import java.util.List;

public interface GetFileMetadataUseCase {
    FileResponse getById(String fileId);
    List<FileResponse> getByCategory(FileCategory category);
    List<FileResponse> getByEntityId(Long entityId);
}
