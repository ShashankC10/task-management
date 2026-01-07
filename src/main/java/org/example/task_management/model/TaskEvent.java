package org.example.task_management.model;

/**
 * Domain event for analytics around task lifecycle.
 */
public record TaskEvent(
        Long taskId,
        String action,
        String title,
        String description,
        Status fromStatus,
        Status toStatus,
        Priority priority,
        java.time.Instant dueDate,
        java.time.Instant createdAt,
        java.time.Instant updatedAt
) { }
