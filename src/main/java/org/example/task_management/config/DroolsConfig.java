package org.example.task_management.config;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DroolsConfig {

    private static final Logger logger = LoggerFactory.getLogger(DroolsConfig.class);

    @Value("${drools.kieBaseName:taskKBase}")
    private String kieBaseName;

    @Value("${drools.kieSessionName:taskSession}")
    private String kieSessionName;

    @Bean
    public KieContainer kieContainer() {
        logger.info("Loading KieContainer from classpath...");
        KieServices kieServices = KieServices.Factory.get();
        KieContainer kieContainer = kieServices.getKieClasspathContainer();
        var messages = kieContainer.verify().getMessages();
        //verification of drl file
        if (!messages.isEmpty()) {
            logger.error("Drools verification failed with {} issues:", messages.size());
            messages.forEach(m -> logger.error(" - {} [Line: {}]", m.getText(), m.getLine()));
            throw new IllegalStateException("Drools verification failed. Check DRL files and kmodule.xml.");
        } else {
            logger.info("Drools verification passed with no issues.");
        }
        return kieContainer;
    }

    @Bean
    public KieSession kieSession(KieContainer kieContainer) {
        KieSession kieSession = kieContainer.newKieSession(kieSessionName);
        kieSession.setGlobal("logger", logger);
        logger.info("KieSession '{}' created successfully.", kieSessionName);
        return kieSession;
    }
}