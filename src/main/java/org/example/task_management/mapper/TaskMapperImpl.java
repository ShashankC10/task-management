package org.example.task_management.mapper;

import org.example.task_management.model.TaskDTO;
import org.example.task_management.model.db.Task;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class TaskMapperImpl implements TaskMapper {
    @Override
    public TaskDTO toDTO(Task task) {
        if (task == null) return null;
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setDueDate(task.getDueDate() != null ? task.getDueDate().atZone(ZoneId.systemDefault()).toInstant() : null);
        dto.setCreatedAt(task.getCreatedAt() != null ? task.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant() : null);
        dto.setUpdatedAt(task.getUpdatedAt() != null ? task.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant() : null);
        return dto;
    }

    @Override
    public Task toEntity(TaskDTO taskDTO) {
        if (taskDTO == null) return null;
        Task task = new Task();
        task.setId(taskDTO.getId());
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setStatus(taskDTO.getStatus());
        task.setPriority(taskDTO.getPriority());
        if (taskDTO.getDueDate() != null) {
            task.setDueDate(LocalDateTime.ofInstant(taskDTO.getDueDate(), ZoneId.systemDefault()));
        }
        if (taskDTO.getCreatedAt() != null) {
            task.setCreatedAt(LocalDateTime.ofInstant(taskDTO.getCreatedAt(), ZoneId.systemDefault()));
        }
        if (taskDTO.getUpdatedAt() != null) {
            task.setUpdatedAt(LocalDateTime.ofInstant(taskDTO.getUpdatedAt(), ZoneId.systemDefault()));
        }
        return task;
    }
}
