package ru.ifmo.se.restaurant.file.infrastructure.adapter.in.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import ru.ifmo.se.restaurant.file.domain.exception.FileNotFoundException;
import ru.ifmo.se.restaurant.file.domain.exception.FileUploadException;
import ru.ifmo.se.restaurant.file.domain.exception.InvalidFileException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleFileNotFound_ShouldReturnNotFoundStatus() {
        FileNotFoundException ex = new FileNotFoundException("test-file-id");

        ResponseEntity<Map<String, Object>> response = handler.handleFileNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().get("status"));
        assertEquals("Not Found", response.getBody().get("error"));
        assertEquals("File not found with id: test-file-id", response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void handleInvalidFile_ShouldReturnBadRequestStatus() {
        InvalidFileException ex = new InvalidFileException("Invalid file type");

        ResponseEntity<Map<String, Object>> response = handler.handleInvalidFile(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Bad Request", response.getBody().get("error"));
        assertEquals("Invalid file type", response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void handleFileUpload_ShouldReturnInternalServerErrorStatus() {
        FileUploadException ex = new FileUploadException("Upload failed");

        ResponseEntity<Map<String, Object>> response = handler.handleFileUpload(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().get("status"));
        assertEquals("Internal Server Error", response.getBody().get("error"));
        assertEquals("Failed to process file", response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void handleFileUpload_WithCause_ShouldReturnInternalServerErrorStatus() {
        FileUploadException ex = new FileUploadException("Upload failed", new RuntimeException("IO Error"));

        ResponseEntity<Map<String, Object>> response = handler.handleFileUpload(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to process file", response.getBody().get("message"));
    }

    @Test
    void handleMaxSizeExceeded_ShouldReturnPayloadTooLargeStatus() {
        MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(10 * 1024 * 1024L);

        ResponseEntity<Map<String, Object>> response = handler.handleMaxSizeExceeded(ex);

        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(413, response.getBody().get("status"));
        assertEquals("Payload Too Large", response.getBody().get("error"));
        assertEquals("File size exceeds maximum allowed size", response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void handleGeneral_ShouldReturnInternalServerErrorStatus() {
        Exception ex = new RuntimeException("Unexpected error");

        ResponseEntity<Map<String, Object>> response = handler.handleGeneral(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().get("status"));
        assertEquals("Internal Server Error", response.getBody().get("error"));
        assertEquals("An unexpected error occurred", response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void handleGeneral_WithNullPointerException_ShouldReturnInternalServerError() {
        NullPointerException ex = new NullPointerException("Null reference");

        ResponseEntity<Map<String, Object>> response = handler.handleGeneral(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().get("message"));
    }

    @Test
    void errorResponse_ShouldContainAllRequiredFields() {
        FileNotFoundException ex = new FileNotFoundException("test");

        ResponseEntity<Map<String, Object>> response = handler.handleFileNotFound(ex);

        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("timestamp"));
        assertTrue(response.getBody().containsKey("status"));
        assertTrue(response.getBody().containsKey("error"));
        assertTrue(response.getBody().containsKey("message"));
        assertEquals(4, response.getBody().size());
    }
}
