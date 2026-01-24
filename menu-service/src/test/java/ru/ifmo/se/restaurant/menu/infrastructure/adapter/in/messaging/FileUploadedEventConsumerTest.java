package ru.ifmo.se.restaurant.menu.infrastructure.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ifmo.se.restaurant.common.event.DomainEvent;
import ru.ifmo.se.restaurant.common.event.file.FileUploadedEvent;
import ru.ifmo.se.restaurant.menu.application.port.out.DishRepository;

import java.time.Instant;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileUploadedEventConsumerTest {

    @Mock
    private DishRepository dishRepository;

    private ObjectMapper objectMapper;
    private FileUploadedEventConsumer consumer;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        consumer = new FileUploadedEventConsumer(dishRepository, objectMapper);
    }

    @Test
    void handleFileUploaded_WithDishImageCategory_ShouldUpdateDish() throws Exception {
        FileUploadedEvent payload = FileUploadedEvent.builder()
                .fileId("file-123")
                .fileName("pizza.jpg")
                .fileUrl("http://storage.com/images/pizza.jpg")
                .contentType("image/jpeg")
                .fileSize(1024L)
                .category("DISH_IMAGE")
                .entityId(100L)
                .uploadedAt(Instant.now())
                .build();

        DomainEvent<FileUploadedEvent> event = DomainEvent.create("FILE_UPLOADED", payload);
        String message = objectMapper.writeValueAsString(event);

        consumer.handleFileUploaded(message);

        verify(dishRepository).updateImageUrl(100L, "http://storage.com/images/pizza.jpg");
    }

    @Test
    void handleFileUploaded_WithNonDishImageCategory_ShouldNotUpdate() throws Exception {
        FileUploadedEvent payload = FileUploadedEvent.builder()
                .fileId("file-123")
                .fileName("avatar.jpg")
                .fileUrl("http://storage.com/images/avatar.jpg")
                .contentType("image/jpeg")
                .fileSize(1024L)
                .category("USER_AVATAR")
                .entityId(100L)
                .uploadedAt(Instant.now())
                .build();

        DomainEvent<FileUploadedEvent> event = DomainEvent.create("FILE_UPLOADED", payload);
        String message = objectMapper.writeValueAsString(event);

        consumer.handleFileUploaded(message);

        verify(dishRepository, never()).updateImageUrl(anyLong(), anyString());
    }

    @Test
    void handleFileUploaded_WithNullEntityId_ShouldNotUpdate() throws Exception {
        FileUploadedEvent payload = FileUploadedEvent.builder()
                .fileId("file-123")
                .fileName("pizza.jpg")
                .fileUrl("http://storage.com/images/pizza.jpg")
                .contentType("image/jpeg")
                .fileSize(1024L)
                .category("DISH_IMAGE")
                .entityId(null)
                .uploadedAt(Instant.now())
                .build();

        DomainEvent<FileUploadedEvent> event = DomainEvent.create("FILE_UPLOADED", payload);
        String message = objectMapper.writeValueAsString(event);

        consumer.handleFileUploaded(message);

        verify(dishRepository, never()).updateImageUrl(anyLong(), anyString());
    }

    @Test
    void handleFileUploaded_WithInvalidJson_ShouldNotThrowException() {
        String invalidMessage = "{ invalid json }";

        consumer.handleFileUploaded(invalidMessage);

        verify(dishRepository, never()).updateImageUrl(anyLong(), anyString());
    }

    @Test
    void handleFileUploaded_WithNullCategory_ShouldNotUpdate() throws Exception {
        FileUploadedEvent payload = FileUploadedEvent.builder()
                .fileId("file-123")
                .fileName("image.jpg")
                .fileUrl("http://storage.com/images/image.jpg")
                .contentType("image/jpeg")
                .fileSize(1024L)
                .category(null)
                .entityId(100L)
                .uploadedAt(Instant.now())
                .build();

        DomainEvent<FileUploadedEvent> event = DomainEvent.create("FILE_UPLOADED", payload);
        String message = objectMapper.writeValueAsString(event);

        consumer.handleFileUploaded(message);

        verify(dishRepository, never()).updateImageUrl(anyLong(), anyString());
    }
}
