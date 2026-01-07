package org.example.task_management.mapper;

import org.example.task_management.model.TaskDTO;
import org.example.task_management.model.db.Task;

public interface TaskMapper {
    TaskDTO toDTO(Task task);
    Task toEntity(TaskDTO taskDTO);
}
