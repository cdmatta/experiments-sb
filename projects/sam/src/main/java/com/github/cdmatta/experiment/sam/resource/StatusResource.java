package com.github.cdmatta.experiment.sam.resource;

import com.github.cdmatta.experiment.sam.config.MonitoringConfiguration;
import com.github.cdmatta.experiment.sam.domain.HttpService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api")
public class StatusResource {

    private final List<HttpService> webpages;

    public StatusResource(MonitoringConfiguration monitoringConfiguration) {
        webpages = monitoringConfiguration.getWebPages();
    }

    @GetMapping(value = "status", produces = APPLICATION_JSON_VALUE)
    public List<HttpService> getStatus() {
        return webpages;
    }
}
