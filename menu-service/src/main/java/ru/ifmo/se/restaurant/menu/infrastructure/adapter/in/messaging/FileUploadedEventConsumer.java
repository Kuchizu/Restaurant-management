package ru.ifmo.se.restaurant.menu.infrastructure.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.ifmo.se.restaurant.common.event.DomainEvent;
import ru.ifmo.se.restaurant.common.event.KafkaTopics;
import ru.ifmo.se.restaurant.common.event.file.FileUploadedEvent;
import ru.ifmo.se.restaurant.menu.application.port.out.DishRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileUploadedEventConsumer {

    private final DishRepository dishRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopics.FILES_UPLOADED,
            groupId = "${spring.kafka.consumer.group-id:menu-service-group}"
    )
    public void handleFileUploaded(String message) {
        try {
            @SuppressWarnings("unchecked")
            DomainEvent<FileUploadedEvent> event = objectMapper.readValue(message,
                    objectMapper.getTypeFactory().constructParametricType(DomainEvent.class, FileUploadedEvent.class));

            FileUploadedEvent payload = event.getPayload();
            log.info("Received FILE_UPLOADED event: fileId={}, category={}, entityId={}",
                    payload.getFileId(), payload.getCategory(), payload.getEntityId());

            // Only process DISH_IMAGE category
            if ("DISH_IMAGE".equals(payload.getCategory()) && payload.getEntityId() != null) {
                dishRepository.updateImageUrl(payload.getEntityId(), payload.getFileUrl());
                log.info("Updated dish {} with image URL: {}", payload.getEntityId(), payload.getFileUrl());
            }

        } catch (Exception e) {
            log.error("Error processing FILE_UPLOADED event: {}", e.getMessage(), e);
        }
    }
}
