package com.github.cdmatta.experiment.sam.config;

import com.github.cdmatta.experiment.sam.domain.HttpService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties
@Getter
@Setter
public class MonitoringConfiguration {

    private MonitoringSettings monitoringSettings;

    private List<HttpService> webPages;
}
