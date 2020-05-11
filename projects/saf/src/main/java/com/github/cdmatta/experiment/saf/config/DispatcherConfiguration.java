package com.github.cdmatta.experiment.saf.config;

import com.github.cdmatta.experiment.saf.dispatch.EventDispatcher;
import com.github.cdmatta.experiment.saf.dispatch.dummy.ConsoleEventDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class DispatcherConfiguration {

    @Bean
    @ConditionalOnMissingBean(EventDispatcher.class)
    public EventDispatcher consoleEventDispatcher() {
        return new ConsoleEventDispatcher();
    }
}
