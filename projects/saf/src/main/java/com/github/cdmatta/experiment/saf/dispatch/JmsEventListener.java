package com.github.cdmatta.experiment.saf.dispatch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.support.JmsUtils;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.Optional;

import static com.github.cdmatta.experiment.saf.config.SafProperties.DATA_QUEUE_NAME;

@Service
@Slf4j
@RequiredArgsConstructor
public class JmsEventListener {

    private final EventDispatcher eventDispatcher;

    @JmsListener(destination = DATA_QUEUE_NAME)
    public void receive(TextMessage event) throws JMSException {
        log.debug("Received event={}", event);

        String eventString;
        try {
            eventString = event.getText();
        } catch (JMSException e) {
            log.error("Failed to read incoming event " + event + " cannot be forwarded", e);
            throw JmsUtils.convertJmsAccessException(e);
        }

        String route = Optional.ofNullable(event.getStringProperty("ROUTING_KEY")).orElse(null);
        eventDispatcher.dispatch(route, eventString);
    }
}
