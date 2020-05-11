package com.github.cdmatta.experiment.saf.health;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.store.MessageStore;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.util.Assert;

@Slf4j
public abstract class AbstractQueueHealthIndicator extends AbstractHealthIndicator {
    private final MessageStore messageStore;
    private final int maxMessageCount;
    private String queueName;

    protected AbstractQueueHealthIndicator(BrokerService brokerService, String queueName, int maxMessageCount) throws Exception {
        this.queueName = queueName;
        this.maxMessageCount = maxMessageCount;

        Destination destination = brokerService.getDestination(new ActiveMQQueue((queueName)));
        Assert.notNull(destination, "Queue " + queueName + " does not exist");

        this.messageStore = destination.getMessageStore();
        Assert.notNull(messageStore, "Message store for " + queueName + " does not exist");
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        int currentMessageCount = messageStore.getMessageCount();
        builder.withDetail("current_message_count", currentMessageCount)
                .withDetail("maximum_message_count", maxMessageCount)
                .withDetail("current_message_size", messageStore.getMessageSize())
                .withDetail("physical_name", messageStore.getDestination().getPhysicalName());
        if (currentMessageCount <= maxMessageCount) {
            builder.up();
        } else {
            log.error("{} has exceeded max message limit. current_message_count={} > max_message_count={}", queueName, currentMessageCount, maxMessageCount);
            builder.outOfService();
        }
    }
}
