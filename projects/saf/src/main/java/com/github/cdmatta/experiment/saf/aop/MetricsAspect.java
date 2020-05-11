package com.github.cdmatta.experiment.saf.aop;

import com.github.cdmatta.experiment.saf.config.SafProperties;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.store.MessageStore;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import static com.github.cdmatta.experiment.saf.config.SafProperties.DATA_QUEUE_NAME;
import static com.github.cdmatta.experiment.saf.config.SafProperties.DLQ_NAME;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class MetricsAspect implements MeterBinder {

    private final BrokerService brokerService;
    private final SafProperties safProperties;

    private MeterRegistry registry;
    Timer successTimer;

    @Override
    public void bindTo(MeterRegistry registry) {
        this.registry = registry;
        successTimer = Timer.builder("saf.dispatch")
                .tag("outcome", "SUCCESS")
                .tag("exception", "None")
                .register(registry);
        try {
            initializeMetrics();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void initializeMetrics() throws Exception {
        createGaugesForQueue(DATA_QUEUE_NAME, "dataq", safProperties.getDataQueueMaxMessageCount());
        createGaugesForQueue(DLQ_NAME, "dlq", safProperties.getDlqMaxMessageCount());
    }

    private void createGaugesForQueue(String queueName, String queueMetricId, int maxMessageCount) throws Exception {
        MessageStore messageStore = getStoreForQueue(brokerService, queueName);
        String prefix = "saf." + queueMetricId;
        Gauge.builder(prefix + ".status", messageStore, store -> getStatus(store, maxMessageCount)).register(registry);
        Gauge.builder(prefix + ".max.message.count", messageStore, store -> maxMessageCount).register(registry);
        Gauge.builder(prefix + ".current.message.count", messageStore, store -> getMessageCount(store)).register(registry);
        Gauge.builder(prefix + ".current.message.size", messageStore, store -> getMessageSize(store)).register(registry);
    }

    @SneakyThrows
    private double getMessageSize(MessageStore store) {
        return store.getMessageSize();
    }

    @SneakyThrows
    private int getMessageCount(MessageStore store) {
        return store.getMessageCount();
    }

    @SneakyThrows
    private double getStatus(MessageStore store, int maxMessageCount) {
        int currentMessageCount = store.getMessageCount();
        if (currentMessageCount < maxMessageCount) {
            return 1;
        }
        return 0;
    }

    private MessageStore getStoreForQueue(BrokerService brokerService, String queueName) throws Exception {
        Destination destination = brokerService.getDestination(new ActiveMQQueue((queueName)));
        Assert.notNull(destination, "Queue " + queueName + " does not exist");

        MessageStore messageStore = destination.getMessageStore();
        Assert.notNull(messageStore, "Message store for " + queueName + " does not exist");
        return messageStore;
    }

    @Around("execution(* com.github.cdmatta.experiment.saf.dispatch.JmsEventListener.receive(javax.jms.TextMessage))")
    public Object recordMessageReceive(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            long end = System.currentTimeMillis();
            successTimer.record(end - start, MILLISECONDS);
            return result;
        } catch (Throwable t) {
            long end = System.currentTimeMillis();
            Timer.builder("saf.dispatch")
                    .tag("outcome", "FAILURE")
                    .tag("exception", t.getClass().getSimpleName())
                    .register(registry)
                    .record(end - start, MILLISECONDS);
            throw t;
        }
    }
}
