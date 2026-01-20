package ru.ifmo.se.restaurant.file.domain.exception;

public class FileNotFoundException extends RuntimeException {
    public FileNotFoundException(String fileId) {
        super("File not found with id: " + fileId);
    }
}
