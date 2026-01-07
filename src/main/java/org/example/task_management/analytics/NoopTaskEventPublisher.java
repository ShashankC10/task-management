package org.example.task_management.analytics;

import org.example.task_management.model.TaskEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(KafkaTaskEventPublisher.class)
public class NoopTaskEventPublisher implements TaskEventPublisher {
    @Override
    public void publish(TaskEvent event) {
        // intentionally no-op to keep application working when Kafka is disabled
    }
}
