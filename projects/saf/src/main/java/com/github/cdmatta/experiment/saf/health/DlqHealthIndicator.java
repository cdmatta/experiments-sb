package com.github.cdmatta.experiment.saf.health;

import com.github.cdmatta.experiment.saf.config.SafProperties;
import org.apache.activemq.broker.BrokerService;
import org.springframework.stereotype.Component;

import static com.github.cdmatta.experiment.saf.config.SafProperties.DLQ_NAME;

@Component
public class DlqHealthIndicator extends AbstractQueueHealthIndicator {

    public DlqHealthIndicator(BrokerService brokerService, SafProperties safProperties) throws Exception {
        super(brokerService, DLQ_NAME, safProperties.getDlqMaxMessageCount());
    }
}
