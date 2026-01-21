package ru.ifmo.se.restaurant.file.application.dto;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class FileDownloadResultTest {

    @Test
    void builder_ShouldCreateResult() {
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());

        FileDownloadResult result = FileDownloadResult.builder()
                .fileName("test.jpg")
                .contentType("image/jpeg")
                .fileSize(1024L)
                .inputStream(inputStream)
                .build();

        assertEquals("test.jpg", result.getFileName());
        assertEquals("image/jpeg", result.getContentType());
        assertEquals(1024L, result.getFileSize());
        assertNotNull(result.getInputStream());
    }

    @Test
    void getters_ShouldReturnCorrectValues() {
        InputStream inputStream = new ByteArrayInputStream("content".getBytes());
        FileDownloadResult result = FileDownloadResult.builder()
                .fileName("document.pdf")
                .contentType("application/pdf")
                .fileSize(5000L)
                .inputStream(inputStream)
                .build();

        assertEquals("document.pdf", result.getFileName());
        assertEquals("application/pdf", result.getContentType());
        assertEquals(5000L, result.getFileSize());
        assertEquals(inputStream, result.getInputStream());
    }

    @Test
    void equals_ShouldWorkCorrectly() {
        InputStream inputStream = new ByteArrayInputStream("test".getBytes());
        FileDownloadResult result1 = FileDownloadResult.builder()
                .fileName("test.jpg")
                .contentType("image/jpeg")
                .fileSize(1024L)
                .inputStream(inputStream)
                .build();

        FileDownloadResult result2 = FileDownloadResult.builder()
                .fileName("test.jpg")
                .contentType("image/jpeg")
                .fileSize(1024L)
                .inputStream(inputStream)
                .build();

        assertEquals(result1, result2);
    }

    @Test
    void toString_ShouldContainFields() {
        FileDownloadResult result = FileDownloadResult.builder()
                .fileName("report.pdf")
                .contentType("application/pdf")
                .fileSize(2048L)
                .build();

        String str = result.toString();
        assertTrue(str.contains("report.pdf"));
        assertTrue(str.contains("application/pdf"));
    }
}
