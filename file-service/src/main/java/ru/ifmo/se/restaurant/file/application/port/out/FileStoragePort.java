package ru.ifmo.se.restaurant.file.application.port.out;

import java.io.InputStream;

public interface FileStoragePort {
    String store(String fileName, InputStream inputStream, String contentType, long size);
    InputStream retrieve(String storedName);
    void delete(String storedName);
    String getFileUrl(String storedName);
}
