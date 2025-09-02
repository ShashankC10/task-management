package org.example.task_management.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.task_management.model.TaskDTO;
import org.example.task_management.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class CreateTaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<Long> createTask(@RequestBody TaskDTO taskDTO) {
        log.debug("Tasks dto {}",taskDTO);
        Long taskId = taskService.createTask(taskDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(taskId);
    }
}
