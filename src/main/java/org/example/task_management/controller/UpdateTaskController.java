package org.example.task_management.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.example.task_management.model.TaskDTO;
import org.example.task_management.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Slf4j
public class UpdateTaskController {

    private final TaskService taskService;

    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable Long id, @Valid @RequestBody TaskDTO taskDTO) {
        taskDTO.setId(id); // path ID is the source of truth
        log.debug("Update task called: {}", taskDTO);
        TaskDTO updated = taskService.updateTask(taskDTO);
        return ResponseEntity.ok(updated);
    }
}
