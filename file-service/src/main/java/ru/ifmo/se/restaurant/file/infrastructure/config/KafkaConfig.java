package ru.ifmo.se.restaurant.file.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import ru.ifmo.se.restaurant.common.event.KafkaTopics;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic filesUploadedTopic() {
        return TopicBuilder.name(KafkaTopics.FILES_UPLOADED)
                .partitions(3)
                .replicas(2)
                .build();
    }
}
