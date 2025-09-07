package org.example.task_management.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.task_management.mapper.TaskMapper;
import org.example.task_management.model.Status;
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
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final RulesService rulesService;
    private final TaskMapper taskMapper = Mappers.getMapper(TaskMapper.class);

    /**
     * Create a new task and fire Drools rules.
     */
    @Transactional
    public Long createTask(TaskDTO taskDTO) {
        Task task = taskMapper.toEntity(taskDTO);
        task.setStatus(Status.PENDING); // new task will have pending status
        Instant now = Instant.now();
        task.setCreatedAt(LocalDateTime.ofInstant(now, ZoneId.systemDefault()));
        task.setUpdatedAt(LocalDateTime.ofInstant(now, ZoneId.systemDefault()));

        taskRepository.save(task);
        log.info("Created task with ID: {}", task.getId());
        return task.getId();
    }

    /**
     * Fetch all tasks.
     */
    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(taskMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Fetch task by ID.
     */
    public Optional<TaskDTO> getTaskById(Long id) {
        return taskRepository.findById(id)
                .map(taskMapper::toDTO);
    }

    /**
     * Update an existing task and apply Drools rules.
     */
    @Transactional
    public TaskDTO updateTask(TaskDTO taskDTO) {
        Long id = taskDTO.getId();
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task not found with id: " + id));

        // Map updated fields
        existingTask.setTitle(taskDTO.getTitle());
        existingTask.setDescription(taskDTO.getDescription());
        existingTask.setNewStatus(taskDTO.getStatus()); // state to update to
        existingTask.setPriority(taskDTO.getPriority());
        if (taskDTO.getDueDate() != null) {
            existingTask.setDueDate(LocalDateTime.ofInstant(taskDTO.getDueDate(), ZoneId.systemDefault()));
        }
        existingTask.setUpdatedAt(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()));

        // Fire Drools rules before saving
        rulesService.fireRules(existingTask);

        Task updatedTask = taskRepository.save(existingTask);
        log.info("Updated task with ID: {}", updatedTask.getId());
        return taskMapper.toDTO(updatedTask);
    }

    /**
     * Delete a task.
     */
    @Transactional
    public boolean deleteTask(Long id) {
        return taskRepository.findById(id)
                .map(task -> {
                    taskRepository.delete(task);
                    log.info("Deleted task with ID: {}", task.getId());
                    return true;
                })
                .orElse(false);
    }
}
