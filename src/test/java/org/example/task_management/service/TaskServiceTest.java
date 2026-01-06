package org.example.task_management.service;

import org.example.task_management.model.Priority;
import org.example.task_management.model.Status;
import org.example.task_management.model.TaskDTO;
import org.example.task_management.model.db.Task;
import org.example.task_management.repository.TaskRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
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

    TaskService service;

    @BeforeAll
    static void allowSelfAttach() {
        System.setProperty("jdk.attach.allowAttachSelf", "true");
    }

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
        Instant due = Instant.parse("2030-01-01T12:00:00Z");
        dto.setDueDate(due);

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
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
        assertEquals(due.truncatedTo(ChronoUnit.SECONDS),
                entity.getDueDate().atZone(ZoneId.systemDefault()).toInstant().truncatedTo(ChronoUnit.SECONDS));
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
        assertNull(entity.getDueDate());
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
        existing.setStatus(Status.PENDING);
        existing.setDueDate(LocalDateTime.of(2030, 1, 1, 10, 0));
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
        assertEquals(dto.getDueDate().atZone(ZoneId.systemDefault()).toLocalDateTime(), existing.getDueDate());
        assertEquals(Status.PENDING, result.getStatus());
    }

    @Test
    void updateTask_throwsWhenMissing() {
        TaskDTO dto = new TaskDTO(); dto.setId(999L);
        when(repo.findById(999L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.updateTask(dto));
    }

    @Test
    void updateTask_whenDueDateNull_keepsExistingDueDate() {
        TaskDTO dto = new TaskDTO();
        dto.setId(5L);
        dto.setTitle("Updated title");
        dto.setDescription("Updated desc");
        dto.setStatus(Status.IN_PROGRESS);
        dto.setPriority(Priority.MEDIUM);
        dto.setDueDate(null);

        Task existing = new Task();
        existing.setId(5L);
        existing.setStatus(Status.PENDING);
        existing.setDueDate(LocalDateTime.of(2029, 12, 31, 23, 59));

        when(repo.findById(5L)).thenReturn(Optional.of(existing));
        when(repo.save(existing)).thenReturn(existing);

        service.updateTask(dto);

        assertEquals(LocalDateTime.of(2029, 12, 31, 23, 59), existing.getDueDate());
    }

    @Test
    void getAllTasks_mapsEntities() {
        Task first = new Task();
        first.setId(1L);
        first.setTitle("First");
        first.setDescription("Desc1");
        first.setStatus(Status.PENDING);
        first.setPriority(Priority.LOW);
        first.setCreatedAt(LocalDateTime.of(2023, 1, 1, 0, 0));
        first.setUpdatedAt(LocalDateTime.of(2023, 1, 2, 0, 0));

        Task second = new Task();
        second.setId(2L);
        second.setTitle("Second");
        second.setDescription("Desc2");
        second.setStatus(Status.DONE);
        second.setPriority(Priority.HIGH);
        second.setCreatedAt(LocalDateTime.of(2024, 2, 2, 0, 0));
        second.setUpdatedAt(LocalDateTime.of(2024, 2, 3, 0, 0));

        when(repo.findAll()).thenReturn(List.of(first, second));

        List<TaskDTO> result = service.getAllTasks();

        assertEquals(2, result.size());
        TaskDTO dto1 = result.get(0);
        assertEquals(1L, dto1.getId());
        assertEquals("First", dto1.getTitle());
        assertEquals(Status.PENDING, dto1.getStatus());
        assertEquals(Instant.from(first.getCreatedAt().atZone(ZoneId.systemDefault())), dto1.getCreatedAt());

        TaskDTO dto2 = result.get(1);
        assertEquals(2L, dto2.getId());
        assertEquals(Status.DONE, dto2.getStatus());
        assertEquals(Priority.HIGH, dto2.getPriority());
    }

    @Test
    void getTaskById_mapsEntity_whenPresent() {
        Task entity = new Task();
        entity.setId(10L);
        entity.setTitle("Single");
        entity.setStatus(Status.CANCELLED);
        entity.setPriority(Priority.MEDIUM);
        entity.setCreatedAt(LocalDateTime.of(2022, 5, 5, 5, 5));
        when(repo.findById(10L)).thenReturn(Optional.of(entity));

        Optional<TaskDTO> result = service.getTaskById(10L);

        assertTrue(result.isPresent());
        assertEquals("Single", result.get().getTitle());
        assertEquals(Status.CANCELLED, result.get().getStatus());
        assertEquals(Priority.MEDIUM, result.get().getPriority());
    }

    @Test
    void getTaskById_returnsEmpty_whenMissing() {
        when(repo.findById(123L)).thenReturn(Optional.empty());
        assertTrue(service.getTaskById(123L).isEmpty());
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
