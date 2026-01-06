package org.example.task_management.mapper;

import org.example.task_management.model.Priority;
import org.example.task_management.model.Status;
import org.example.task_management.model.TaskDTO;
import org.example.task_management.model.db.Task;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TaskMapperTest {

    private final TaskMapper mapper = Mappers.getMapper(TaskMapper.class);

    @Test
    void toDTO_convertsTemporalFieldsAndCopiesValues() {
        Task entity = new Task();
        entity.setId(1L);
        entity.setTitle("Sample");
        entity.setDescription("Desc");
        entity.setStatus(Status.IN_PROGRESS);
        entity.setPriority(Priority.HIGH);
        entity.setDueDate(LocalDateTime.of(2030, 1, 1, 12, 0));
        entity.setCreatedAt(LocalDateTime.of(2029, 12, 31, 23, 0));
        entity.setUpdatedAt(LocalDateTime.of(2029, 12, 31, 23, 30));

        TaskDTO dto = mapper.toDTO(entity);

        assertEquals(1L, dto.getId());
        assertEquals("Sample", dto.getTitle());
        assertEquals(Status.IN_PROGRESS, dto.getStatus());
        assertEquals(Priority.HIGH, dto.getPriority());
        assertEquals(entity.getDueDate().atZone(ZoneId.systemDefault()).toInstant(), dto.getDueDate());
        assertEquals(entity.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant(), dto.getCreatedAt());
        assertEquals(entity.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant(), dto.getUpdatedAt());
    }

    @Test
    void toEntity_convertsInstantsAndHandlesNulls() {
        TaskDTO dto = new TaskDTO();
        dto.setId(5L);
        dto.setTitle("Backwards");
        dto.setStatus(Status.CANCELLED);
        dto.setPriority(Priority.LOW);
        dto.setDueDate(Instant.parse("2031-06-01T10:15:30Z"));
        dto.setCreatedAt(null);
        dto.setUpdatedAt(null);

        Task entity = mapper.toEntity(dto);

        assertEquals(5L, entity.getId());
        assertEquals("Backwards", entity.getTitle());
        assertEquals(Status.CANCELLED, entity.getStatus());
        assertEquals(Priority.LOW, entity.getPriority());
        assertEquals(LocalDateTime.ofInstant(dto.getDueDate(), ZoneId.systemDefault()), entity.getDueDate());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
    }
}
