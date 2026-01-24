package ru.ifmo.se.restaurant.file.application.port.in;

import ru.ifmo.se.restaurant.file.application.dto.FileUploadCommand;
import ru.ifmo.se.restaurant.file.application.dto.FileResponse;

public interface UploadFileUseCase {
    FileResponse upload(FileUploadCommand command);
}
