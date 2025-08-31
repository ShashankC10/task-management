package org.example.task_management.controller;

import org.example.task_management.model.TaskDTO;
import org.example.task_management.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class GetTaskController {

    private final TaskService taskService;

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTask(@PathVariable Long id) {
        TaskDTO task = taskService.getTaskById(id)
                .orElseThrow(() -> new NoSuchElementException("Task not found with id: " + id));
        return ResponseEntity.ok(task);
    }
}
