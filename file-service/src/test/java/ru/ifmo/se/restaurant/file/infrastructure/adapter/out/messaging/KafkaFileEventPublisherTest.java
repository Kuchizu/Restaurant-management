package ru.ifmo.se.restaurant.file.infrastructure.adapter.out.messaging;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import ru.ifmo.se.restaurant.common.event.DomainEvent;
import ru.ifmo.se.restaurant.common.event.KafkaTopics;
import ru.ifmo.se.restaurant.file.domain.entity.FileMetadata;
import ru.ifmo.se.restaurant.file.domain.valueobject.FileCategory;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaFileEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private KafkaFileEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new KafkaFileEventPublisher(kafkaTemplate);
    }

    @Test
    void publishFileUploaded_ShouldSendEventToKafka() {
        FileMetadata metadata = createTestMetadata();
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        SendResult<String, Object> sendResult = createMockSendResult();
        future.complete(sendResult);

        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        publisher.publishFileUploaded(metadata);

        verify(kafkaTemplate).send(eq(KafkaTopics.FILES_UPLOADED), eq("test-id"), any(DomainEvent.class));
    }

    @Test
    void publishFileUploaded_ShouldHandleFailure() {
        FileMetadata metadata = createTestMetadata();
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka error"));

        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        assertDoesNotThrow(() -> publisher.publishFileUploaded(metadata));
        verify(kafkaTemplate).send(anyString(), anyString(), any());
    }

    @Test
    void publishFileUploaded_ShouldMapAllMetadataFields() {
        FileMetadata metadata = createTestMetadata();
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        SendResult<String, Object> sendResult = createMockSendResult();
        future.complete(sendResult);

        ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
        when(kafkaTemplate.send(anyString(), anyString(), eventCaptor.capture())).thenReturn(future);

        publisher.publishFileUploaded(metadata);

        DomainEvent capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent);
        assertEquals("FILE_UPLOADED", capturedEvent.getEventType());
    }

    @Test
    void publishFileUploaded_ShouldUseCorrectTopic() {
        FileMetadata metadata = createTestMetadata();
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.complete(createMockSendResult());

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        when(kafkaTemplate.send(topicCaptor.capture(), anyString(), any())).thenReturn(future);

        publisher.publishFileUploaded(metadata);

        assertEquals(KafkaTopics.FILES_UPLOADED, topicCaptor.getValue());
    }

    @Test
    void publishFileUploaded_ShouldUseFileIdAsKey() {
        FileMetadata metadata = createTestMetadata();
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.complete(createMockSendResult());

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        when(kafkaTemplate.send(anyString(), keyCaptor.capture(), any())).thenReturn(future);

        publisher.publishFileUploaded(metadata);

        assertEquals("test-id", keyCaptor.getValue());
    }

    @Test
    void publishFileUploaded_WithNullEntityId_ShouldStillWork() {
        FileMetadata metadata = FileMetadata.builder()
                .id("test-id")
                .originalName("test.jpg")
                .storedName("test-id.jpg")
                .contentType("image/jpeg")
                .fileSize(1024L)
                .bucket("bucket")
                .fileUrl("http://url")
                .category(FileCategory.DISH_IMAGE)
                .entityId(null)
                .uploadedAt(Instant.now())
                .uploadedBy(null)
                .build();

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.complete(createMockSendResult());

        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        assertDoesNotThrow(() -> publisher.publishFileUploaded(metadata));
    }

    private FileMetadata createTestMetadata() {
        return FileMetadata.builder()
                .id("test-id")
                .originalName("test.jpg")
                .storedName("test-id.jpg")
                .contentType("image/jpeg")
                .fileSize(1024L)
                .bucket("dish-images")
                .fileUrl("http://minio:9000/dish-images/test-id.jpg")
                .category(FileCategory.DISH_IMAGE)
                .entityId(1L)
                .uploadedAt(Instant.now())
                .uploadedBy("user1")
                .build();
    }

    @SuppressWarnings("unchecked")
    private SendResult<String, Object> createMockSendResult() {
        SendResult<String, Object> sendResult = mock(SendResult.class);
        RecordMetadata recordMetadata = new RecordMetadata(
                new TopicPartition(KafkaTopics.FILES_UPLOADED, 0),
                0L, 0, 0L, 0, 0
        );
        when(sendResult.getRecordMetadata()).thenReturn(recordMetadata);
        return sendResult;
    }
}
