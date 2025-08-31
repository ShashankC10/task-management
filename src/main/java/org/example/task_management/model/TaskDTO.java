package org.example.task_management.model;

import lombok.*;

import java.time.Instant;

@Data
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private Status status;
    private Priority priority;
    private Instant dueDate;
    private Instant createdAt;
    private Instant updatedAt;
}
