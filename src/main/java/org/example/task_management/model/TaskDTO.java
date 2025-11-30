package org.example.task_management.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class TaskDTO {
    private Long id;
    @NotBlank(message = "title is required")
    private String title;
    private String description;
    @NotNull(message = "status is required")
    private Status status;
    private Priority priority;
    private Instant dueDate;
    private Instant createdAt;
    private Instant updatedAt;
}
