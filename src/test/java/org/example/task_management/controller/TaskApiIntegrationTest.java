package org.example.task_management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.task_management.AbstractIntegrationTest;
import org.example.task_management.model.Priority;
import org.example.task_management.model.Status;
import org.example.task_management.model.TaskDTO;
import org.example.task_management.model.db.Task;
import org.example.task_management.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full-stack integration test using a real PostgreSQL Testcontainer.
 * This verifies that controllers, service, repository, mapper, and exception handler
 * all work together.
 */
class TaskApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void cleanDatabase() {
        taskRepository.deleteAll();
    }

    private Task buildTask(String title, String desc, Status status, Priority priority) {
        Task t = new Task();
        t.setTitle(title);
        t.setDescription(desc);
        t.setStatus(status);
        t.setPriority(priority);
        t.setCreatedAt(LocalDateTime.now());
        t.setUpdatedAt(LocalDateTime.now());
        t.setDueDate(LocalDateTime.ofInstant(Instant.parse("2030-01-01T00:00:00Z"), ZoneId.systemDefault()));
        return t;
    }

    @Test
    void createTask_persistsAndReturnsId() throws Exception {
        TaskDTO dto = new TaskDTO();
        dto.setTitle("Integration Task");
        dto.setDescription("Full integration test");
        dto.setPriority(Priority.HIGH);
        dto.setStatus(Status.PENDING);

        mvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(not(emptyString())));

        List<Task> all = taskRepository.findAll();
        org.assertj.core.api.Assertions.assertThat(all)
                .hasSize(1)
                .first()
                .extracting(Task::getTitle)
                .isEqualTo("Integration Task");
    }

    @Test
    void getAllTasks_returnsInsertedTasks() throws Exception {
        taskRepository.saveAll(List.of(
                buildTask("A", "DescA", Status.PENDING, Priority.LOW),
                buildTask("B", "DescB", Status.DONE, Priority.HIGH)
        ));

        mvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("A")))
                .andExpect(jsonPath("$[1].status", is("DONE")));
    }

    @Test
    void getTaskById_found_returns200() throws Exception {
        Task task = taskRepository.save(buildTask("FindMe", "Details", Status.PENDING, Priority.MEDIUM));

        mvc.perform(get("/api/tasks/{id}", task.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(task.getId().intValue())))
                .andExpect(jsonPath("$.title", is("FindMe")))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    void getTaskById_notFound_returns404() throws Exception {
        mvc.perform(get("/api/tasks/{id}", 999))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Task not found with id: 999")));
    }

    @Test
    void updateTask_invalidTransition_returns400_andEntityUnchanged() throws Exception {
        Task existing = taskRepository.save(
                buildTask("OldTitle", "OldDesc", Status.PENDING, Priority.LOW)
        );

        TaskDTO updateDto = new TaskDTO();
        updateDto.setId(existing.getId());
        updateDto.setTitle("NewTitle");         // should NOT be persisted
        updateDto.setDescription("Updated");    // should NOT be persisted
        updateDto.setStatus(Status.DONE);       // invalid from PENDING
        updateDto.setPriority(Priority.HIGH);   // should NOT be persisted
        updateDto.setDueDate(Instant.parse("2031-01-01T00:00:00Z"));

        mvc.perform(put("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid transition attempted: PENDING -> DONE")));

        Task reloaded = taskRepository.findById(existing.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(reloaded.getTitle()).isEqualTo("OldTitle");
        org.assertj.core.api.Assertions.assertThat(reloaded.getDescription()).isEqualTo("OldDesc");
        org.assertj.core.api.Assertions.assertThat(reloaded.getStatus()).isEqualTo(Status.PENDING);
        org.assertj.core.api.Assertions.assertThat(reloaded.getPriority()).isEqualTo(Priority.LOW);
    }

    @Test
    void deleteTask_removesRecord() throws Exception {
        Task t = taskRepository.save(buildTask("DeleteMe", "Desc", Status.PENDING, Priority.HIGH));

        mvc.perform(delete("/api/tasks/{id}", t.getId()))
                .andExpect(status().isNoContent());

        org.assertj.core.api.Assertions.assertThat(taskRepository.existsById(t.getId())).isFalse();
    }

    @Test
    void deleteTask_notFound_returns404() throws Exception {
        mvc.perform(delete("/api/tasks/{id}", 404))
                .andExpect(status().isNotFound());
    }
}
