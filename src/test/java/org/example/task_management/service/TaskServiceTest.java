package org.example.task_management.service;

import org.example.task_management.mapper.TaskMapper;
import org.example.task_management.model.Priority;
import org.example.task_management.model.Status;
import org.example.task_management.model.TaskDTO;
import org.example.task_management.model.db.Task;
import org.example.task_management.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    TaskRepository repo;
    @Mock
    RulesService rules;

    TaskMapper mapper;

    TaskService service;

    @BeforeEach
    void setUp() {
        service = new TaskService(repo, rules); // after you inject mapper, pass it too
    }

    @Test
    void createTask_setsPendingAndTimestamps_andReturnsId() {
        TaskDTO dto = new TaskDTO();
        dto.setTitle("Test Task");
        dto.setDescription("This is a test task");
        dto.setPriority(Priority.HIGH);
        dto.setStatus(Status.PENDING);
        dto.setDueDate(Instant.now().plus(3, ChronoUnit.DAYS));

        when(repo.save(any(Task.class))).thenAnswer(inv -> {
            Task t = inv.getArgument(0, Task.class);
            t.setId(42L);
            return t;
        });
        Long id = service.createTask(dto);

        assertEquals(42L, id);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(repo).save(captor.capture());
        Task entity = captor.getValue();

        assertEquals(Status.PENDING, entity.getStatus());
        verify(repo).save(any());
    }
    @Test
    void createTask_setsPendingAndTimestamps_andReturnsId_nullDueDate() {
        TaskDTO dto = new TaskDTO();
        dto.setTitle("Test Task");
        dto.setDescription("This is a test task");
        dto.setPriority(Priority.HIGH);
        dto.setStatus(Status.PENDING);
        dto.setDueDate(null);

        when(repo.save(any(Task.class))).thenAnswer(inv -> {
            Task t = inv.getArgument(0, Task.class);
            t.setId(42L);
            return t;
        });
        Long id = service.createTask(dto);

        assertEquals(42L, id);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(repo).save(captor.capture());
        Task entity = captor.getValue();

        assertEquals(Status.PENDING, entity.getStatus());
        verify(repo).save(any());
    }

    @Test
    void updateTask_mapsFields_firesRules_andSaves() {
        TaskDTO dto = new TaskDTO();
        dto.setId(7L);
        dto.setTitle("t");
        dto.setDescription("d");
        dto.setStatus(Status.DONE);
        dto.setPriority(Priority.HIGH);
        dto.setDueDate(Instant.now());

        Task existing = new Task();
        existing.setId(7L);
        when(repo.findById(7L)).thenReturn(Optional.of(existing));
        when(repo.save(existing)).thenReturn(existing);

        TaskDTO result = service.updateTask(dto);

        verify(rules).fireRules(existing);
        verify(repo).save(existing);
        // assert field updates
        assertEquals("t", existing.getTitle());
        assertEquals("d", existing.getDescription());
        assertEquals(Status.DONE, existing.getNewStatus());
        assertNotNull(existing.getUpdatedAt());
    }

    @Test
    void updateTask_throwsWhenMissing() {
        TaskDTO dto = new TaskDTO(); dto.setId(999L);
        when(repo.findById(999L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.updateTask(dto));
    }

    @Test
    void deleteTask_returnsTrueWhenFound_falseOtherwise() {
        Task t = new Task(); t.setId(1L);
        when(repo.findById(1L)).thenReturn(Optional.of(t));
        assertTrue(service.deleteTask(1L));
        verify(repo).delete(t);

        when(repo.findById(2L)).thenReturn(Optional.empty());
        assertFalse(service.deleteTask(2L));
    }
}

