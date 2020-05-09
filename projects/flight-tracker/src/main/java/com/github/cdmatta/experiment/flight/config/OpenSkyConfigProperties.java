package com.github.cdmatta.experiment.flight.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.net.URL;

@Data
@Validated
@ConfigurationProperties(prefix = "opensky")
public class OpenSkyConfigProperties {

    @NotNull
    URL baseUrl;

    @NotBlank
    String allStates;
}
