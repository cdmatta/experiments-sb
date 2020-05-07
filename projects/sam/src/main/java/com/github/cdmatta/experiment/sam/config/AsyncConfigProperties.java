package com.github.cdmatta.experiment.sam.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = AsyncConfigProperties.PREFIX)
@Data
public class AsyncConfigProperties {
    static final String PREFIX = "async";

    private int corePoolSize = 10;
    private int maxPoolSize = 500;
    private int queueCapacity = 100;
}
