package org.example.task_management.config;

import lombok.extern.slf4j.Slf4j;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DroolsConfig {
    private static final Logger logger = LoggerFactory.getLogger(DroolsConfig.class);

    @Bean
    public KieContainer kieContainer() {
        logger.info("Loading KieContainer from classpath...");
        var ks = KieServices.Factory.get();
        var kc = ks.getKieClasspathContainer();
        var messages = kc.verify().getMessages();
        if (!messages.isEmpty()) {
            logger.error("Drools verification failed with {} issues:", messages.size());
            messages.forEach(m -> logger.error(" - {} [Line: {}]", m.getText(), m.getLine()));
            throw new IllegalStateException("Drools verification failed. Check DRL files and kmodule.xml.");
        }
        logger.info("Drools verification passed with no issues.");
        return kc;
    }
}