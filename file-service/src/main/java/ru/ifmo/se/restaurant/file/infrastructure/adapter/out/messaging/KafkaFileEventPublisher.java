package ru.ifmo.se.restaurant.file.infrastructure.adapter.out.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.common.event.DomainEvent;
import ru.ifmo.se.restaurant.common.event.KafkaTopics;
import ru.ifmo.se.restaurant.common.event.file.FileUploadedEvent;
import ru.ifmo.se.restaurant.file.application.port.out.FileEventPublisher;
import ru.ifmo.se.restaurant.file.domain.entity.FileMetadata;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaFileEventPublisher implements FileEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishFileUploaded(FileMetadata fileMetadata) {
        FileUploadedEvent payload = FileUploadedEvent.builder()
                .fileId(fileMetadata.getId())
                .fileName(fileMetadata.getOriginalName())
                .fileUrl(fileMetadata.getFileUrl())
                .contentType(fileMetadata.getContentType())
                .fileSize(fileMetadata.getFileSize())
                .category(fileMetadata.getCategory().name())
                .entityId(fileMetadata.getEntityId())
                .uploadedAt(fileMetadata.getUploadedAt())
                .build();

        DomainEvent<FileUploadedEvent> event = DomainEvent.create("FILE_UPLOADED", payload);

        kafkaTemplate.send(KafkaTopics.FILES_UPLOADED, fileMetadata.getId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish FILE_UPLOADED event for file: {}", fileMetadata.getId(), ex);
                    } else {
                        log.info("Published FILE_UPLOADED event for file: {} to partition: {}",
                                fileMetadata.getId(),
                                result.getRecordMetadata().partition());
                    }
                });
    }
}
