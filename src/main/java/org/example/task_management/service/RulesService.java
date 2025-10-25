package org.example.task_management.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.task_management.model.db.Task;
import org.example.task_management.model.exception.InvalidTaskTransitionException;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.ConsequenceException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RulesService {
    private final KieSession kieSession;

    //method to insert a task into Drools session and evaluate all rules.
    public void fireRules(Task task) {
        try {
            System.out.println("Rules are being fired");
            kieSession.insert(task);
            int rulesFired = kieSession.fireAllRules();
            log.info("Rules fired: {}, Task ID: {}, status: {}, newStatus: {}",
                    rulesFired, task.getId(), task.getStatus(), task.getNewStatus());
        }catch (Exception e){
            if(e instanceof ConsequenceException){
                if(e.getCause() instanceof InvalidTaskTransitionException ite){
                    log.error("Invalid transition {}: {}", task.getId(), e.getMessage(), e);
                    throw ite;
                }
            }
            log.error("Error applying rules for task ID {}: {}", task.getId(), e.getMessage(), e);
            throw e;
        }
    }
}
