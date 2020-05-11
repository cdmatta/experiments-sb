package com.github.cdmatta.experiment.saf.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.Duration;
import java.util.Optional;

@Data
@Validated
@ConfigurationProperties(prefix = "saf")
public class SafProperties {

    public static final String DATA_QUEUE_NAME = "saf-data-queue";
    public static final String DLQ_NAME = "ActiveMq.DLQ";

    @NotBlank
    private String brokerUrl;

    private Credential user = new Credential();
    private Credential admin = new Credential();

    private boolean persistenceEnabled = true;
    @NotBlank
    private String kahaDbPath;

    private int listenerMaxConcurrency = 20;
    private int dataQueueMaxMessageCount = 10_000_000;
    private int dlqMaxMessageCount = 5_000;

    @Min(50)
    @Max(60)
    private int brokerMemoryPercentage = 50;

    private boolean advisorySupportEnabled = false;

    private Pool pool = new Pool();
    private RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();


    @Data
    public static class Credential {
        @NotBlank
        private String username;

        @NotBlank
        private String password;
    }


    // TODO ALERTING CONFIG private long diskThreshold = DataSize.ofGigabytes(10).toBytes();


    @Data
    public static class Pool {
        private int maxConnections = 10;
        private Duration idleTimeout = Duration.ofMinutes(1);
        private Duration expiryTimeout;

        public long getExpiryInMillisOrZero() {
            return Optional.ofNullable(expiryTimeout).map(Duration::toMillis).orElse(0L);
        }
    }


    @Data
    public static class RedeliveryPolicy {
        private Duration initialRedeliveryDelay = Duration.ofSeconds(1);
        private int maximumRedeliveries = 100;
        private boolean useExponentialBackoff = true;
        private double backOffMultiplier = 2;
        private Duration maxRedeliveryDelay = Duration.ofHours(1);
        private boolean nonBlockingRedeliveryEnabled = true;
        private boolean useCollisionAvoidance = true;
        private short collisionAvoidancePercent = 15;
    }
}
