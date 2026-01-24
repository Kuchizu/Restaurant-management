package ru.ifmo.se.restaurant.file.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    @Test
    void fileNotFoundException_ShouldContainFileId() {
        FileNotFoundException ex = new FileNotFoundException("abc-123");

        assertEquals("File not found with id: abc-123", ex.getMessage());
        assertTrue(ex instanceof RuntimeException);
    }

    @Test
    void invalidFileException_ShouldContainMessage() {
        InvalidFileException ex = new InvalidFileException("Invalid file type");

        assertEquals("Invalid file type", ex.getMessage());
        assertTrue(ex instanceof RuntimeException);
    }

    @Test
    void fileUploadException_WithMessage_ShouldContainMessage() {
        FileUploadException ex = new FileUploadException("Upload failed");

        assertEquals("Upload failed", ex.getMessage());
        assertNull(ex.getCause());
        assertTrue(ex instanceof RuntimeException);
    }

    @Test
    void fileUploadException_WithCause_ShouldContainCause() {
        RuntimeException cause = new RuntimeException("IO Error");
        FileUploadException ex = new FileUploadException("Upload failed", cause);

        assertEquals("Upload failed", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void fileNotFoundException_ShouldBeThrowable() {
        assertThrows(FileNotFoundException.class, () -> {
            throw new FileNotFoundException("test-id");
        });
    }

    @Test
    void invalidFileException_ShouldBeThrowable() {
        assertThrows(InvalidFileException.class, () -> {
            throw new InvalidFileException("Invalid");
        });
    }

    @Test
    void fileUploadException_ShouldBeThrowable() {
        assertThrows(FileUploadException.class, () -> {
            throw new FileUploadException("Error");
        });
    }
}
