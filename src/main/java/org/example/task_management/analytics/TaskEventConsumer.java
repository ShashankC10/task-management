package org.example.task_management.analytics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.task_management.model.TaskEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "analytics.kafka.enabled", havingValue = "true")
@Slf4j
public class TaskEventConsumer {

    private final TaskEventProperties properties;

    @KafkaListener(topics = "#{@taskEventProperties.topic}", groupId = "${spring.kafka.consumer.group-id:task-events-consumer}")
    public void onEvent(TaskEvent event) {
        log.info("Kafka task event received: action={}, taskId={}, title={}, from={}, to={}, priority={}, dueDate={}, createdAt={}, updatedAt={}",
                event.action(), event.taskId(), event.title(), event.fromStatus(), event.toStatus(),
                event.priority(), event.dueDate(), event.createdAt(), event.updatedAt());
    }
}
