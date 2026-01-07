package org.example.task_management.analytics;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "analytics.kafka")
@Data
public class TaskEventProperties {
    private String topic = "task-events";
}
