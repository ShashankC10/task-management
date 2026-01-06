package org.example.task_management.service;

import org.example.task_management.model.Status;
import org.example.task_management.model.db.Task;
import org.example.task_management.model.exception.InvalidTaskTransitionException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.ConsequenceException;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RulesServiceTest {

    @Mock
    KieContainer container;
    @Mock
    KieSession session;

    RulesService service;

    @BeforeAll
    static void allowSelfAttach() {
        System.setProperty("jdk.attach.allowAttachSelf", "true");
    }

    @BeforeEach
    void setup() {
        service = new RulesService(container);
        ReflectionTestUtils.setField(service, "kieSessionName", "taskSession");
        when(container.newKieSession("taskSession")).thenReturn(session);
    }

    @Test
    void fireRules_createsSession_insertsTask_andDisposes() {
        Task task = new Task();
        task.setId(99L);
        when(session.fireAllRules()).thenReturn(2);

        service.fireRules(task);

        verify(container).newKieSession("taskSession");
        verify(session).setGlobal(eq("logger"), any());
        verify(session).insert(task);
        verify(session).fireAllRules();
        verify(session).dispose();
    }

    @Test
    void fireRules_propagatesInvalidTransitionFromConsequenceException() {
        Task task = new Task();
        task.setId(1L);
        task.setStatus(Status.PENDING);
        task.setNewStatus(Status.DONE);
        InvalidTaskTransitionException ite =
                new InvalidTaskTransitionException(task.getId(), Status.PENDING, Status.DONE, "bad transition");
        ConsequenceException consequence = new ConsequenceException(ite, null, null);
        when(session.fireAllRules()).thenThrow(consequence);

        assertThrows(InvalidTaskTransitionException.class, () -> service.fireRules(task));

        verify(session).dispose();
    }

    @Test
    void fireRules_disposesSessionWhenUnexpectedExceptionOccurs() {
        Task task = new Task();
        RuntimeException runtime = new RuntimeException("boom");
        when(session.fireAllRules()).thenThrow(runtime);

        assertThrows(RuntimeException.class, () -> service.fireRules(task));
        verify(session).dispose();
    }
}
