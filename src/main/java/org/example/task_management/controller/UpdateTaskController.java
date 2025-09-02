package org.example.task_management.controller;

import lombok.RequiredArgsConstructor;
import org.example.task_management.model.TaskDTO;
import org.example.task_management.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class UpdateTaskController {

    private final TaskService taskService;

    @PutMapping()
    public ResponseEntity<TaskDTO> updateTask(@RequestBody TaskDTO taskDTO) {
        TaskDTO updated = taskService.updateTask(taskDTO);
        return ResponseEntity.ok(updated);
    }
}
