package ru.ifmo.se.restaurant.file.application.port.in;

import ru.ifmo.se.restaurant.file.application.dto.FileDownloadResult;

public interface DownloadFileUseCase {
    FileDownloadResult download(String fileId);
}
