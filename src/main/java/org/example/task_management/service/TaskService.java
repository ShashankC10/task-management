package org.example.task_management.service;

import lombok.RequiredArgsConstructor;
import org.example.task_management.mapper.TaskMapper;
import org.example.task_management.model.TaskDTO;
import org.example.task_management.model.db.Task;
import org.example.task_management.repository.TaskRepository;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper = Mappers.getMapper(TaskMapper.class);

    @Transactional
    public Long createTask(TaskDTO taskDTO) {
        Task task = taskMapper.toEntity(taskDTO);
        Instant now = Instant.now();
        task.setCreatedAt(LocalDateTime.ofInstant(now, ZoneId.systemDefault()));
        task.setUpdatedAt(LocalDateTime.ofInstant(now, ZoneId.systemDefault()));
        taskRepository.save(task);
        return task.getId();
    }

    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(taskMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<TaskDTO> getTaskById(Long id) {
        return taskRepository.findById(id)
                .map(taskMapper::toDTO);
    }

    @Transactional
    public TaskDTO updateTask(TaskDTO taskDTO) {
        Long id = taskDTO.getId();
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task not found with id: " + id));

        // Map updated fields from DTO to entity
        existingTask.setTitle(taskDTO.getTitle());
        existingTask.setDescription(taskDTO.getDescription());
        existingTask.setStatus(taskDTO.getStatus());
        existingTask.setPriority(taskDTO.getPriority());
        existingTask.setDueDate(LocalDateTime.ofInstant(taskDTO.getDueDate(), ZoneId.systemDefault()));
        existingTask.setUpdatedAt(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));

        Task updatedTask = taskRepository.save(existingTask);
        return taskMapper.toDTO(updatedTask);
    }

    // Delete task
    @Transactional
    public boolean deleteTask(Long id) {
        return taskRepository.findById(id)
                .map(task -> {
                    taskRepository.delete(task);
                    return true;
                })
                .orElse(false);
    }
}
