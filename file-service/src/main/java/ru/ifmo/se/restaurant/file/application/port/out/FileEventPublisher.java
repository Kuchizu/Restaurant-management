package ru.ifmo.se.restaurant.file.application.port.out;

import ru.ifmo.se.restaurant.file.domain.entity.FileMetadata;

public interface FileEventPublisher {
    void publishFileUploaded(FileMetadata fileMetadata);
}
