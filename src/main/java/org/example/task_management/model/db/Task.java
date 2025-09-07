package org.example.task_management.model.db;

import jakarta.persistence.*;
import lombok.*;
import org.example.task_management.model.Priority;
import org.example.task_management.model.Status;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Data
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Status status;  // Current persisted status

    // This field is not stored in DB, only used for rule evaluation
    @Transient
    private Status newStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Priority priority;

    private LocalDateTime dueDate;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
