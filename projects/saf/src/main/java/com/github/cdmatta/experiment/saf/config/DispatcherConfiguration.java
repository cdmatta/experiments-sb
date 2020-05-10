package com.github.cdmatta.experiment.saf.config;

import com.github.cdmatta.experiment.saf.dispatch.EventDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;

@Configuration
@ConditionalOnMissingBean(EventDispatcher.class)
@Slf4j
public class DispatcherConfiguration {

    @Bean
    public EventDispatcher consoleEventDispatcher() {
        return (routingKey, eventString) -> {
            log.info("Dispatched routingKey={} event={}", trimToEmpty(routingKey), eventString);
        };
    }
}
