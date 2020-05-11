package com.github.cdmatta.experiment.saf.health;

import com.github.cdmatta.experiment.saf.config.SafProperties;
import org.apache.activemq.broker.BrokerService;
import org.springframework.stereotype.Component;

import static com.github.cdmatta.experiment.saf.config.SafProperties.DATA_QUEUE_NAME;

@Component
public class DataQueueHealthIndicator extends AbstractQueueHealthIndicator {

    public DataQueueHealthIndicator(BrokerService brokerService, SafProperties safProperties) throws Exception {
        super(brokerService, DATA_QUEUE_NAME, safProperties.getDataQueueMaxMessageCount());
    }
}
