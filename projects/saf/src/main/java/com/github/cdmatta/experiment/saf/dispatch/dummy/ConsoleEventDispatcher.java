package com.github.cdmatta.experiment.saf.dispatch.dummy;

import com.github.cdmatta.experiment.saf.dispatch.EventDispatcher;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;

import static org.apache.commons.lang3.StringUtils.trimToEmpty;

@Slf4j
public class ConsoleEventDispatcher implements EventDispatcher {

    @Override
    public void dispatch(@Nullable String routingKey, String eventString) {
        log.info("Dispatched routingKey={} event={}", trimToEmpty(routingKey), eventString);
    }
}