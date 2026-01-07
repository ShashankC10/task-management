package org.example.task_management.analytics;

import org.example.task_management.model.TaskEvent;

public interface TaskEventPublisher {
    void publish(TaskEvent event);
}
