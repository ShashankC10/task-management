package org.example.task_management.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.task_management.model.db.Task;
import org.example.task_management.model.exception.InvalidTaskTransitionException;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.ConsequenceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RulesService {

    @Value("${drools.kieSessionName:taskSession}")
    private String kieSessionName;

    private final KieContainer kieContainer;
    //method to insert a task into Drools session and evaluate all rules.
    public void fireRules(Task task) {
        KieSession kSession = kieContainer.newKieSession(kieSessionName);
        try {
            kSession.setGlobal("logger",log);
            System.out.println("Rules are being fired");
            kSession.insert(task);
            int rulesFired = kSession.fireAllRules();
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
        }finally {
            kSession.dispose();
        }
    }
}
