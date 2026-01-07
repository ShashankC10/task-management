package org.example.task_management.analytics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.task_management.model.TaskEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "analytics.kafka.enabled", havingValue = "true")
@Slf4j
public class KafkaTaskEventPublisher implements TaskEventPublisher {

    private final KafkaTemplate<String, TaskEvent> kafkaTemplate;
    private final TaskEventProperties properties;

    @Override
    public void publish(TaskEvent event) {
        try {
            kafkaTemplate.send(properties.getTopic(), event.taskId() != null ? event.taskId().toString() : "", event);
        } catch (Exception e) {
            log.warn("Failed to publish task event to Kafka topic {}: {}", properties.getTopic(), e.getMessage());
        }
    }
}
