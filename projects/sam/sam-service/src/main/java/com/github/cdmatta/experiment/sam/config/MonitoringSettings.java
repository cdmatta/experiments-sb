package com.github.cdmatta.experiment.sam.config;

import lombok.Data;

@Data
public class MonitoringSettings {

    /**
     * Cron expression specifying when to start polling for services.
     *
     * @see org.springframework.scheduling.support.CronSequenceGenerator
     */
    private String pollingFrequencyCronExpression;

    /**
     * Max time the monitoring application will wait for the service to respond. If the service fails
     * to respond within this period, it is assumed down.
     */
    private int timeOutForPollRequestSeconds;
}
