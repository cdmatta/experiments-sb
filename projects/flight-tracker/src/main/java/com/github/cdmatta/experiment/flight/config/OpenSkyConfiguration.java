package com.github.cdmatta.experiment.flight.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(OpenSkyConfigProperties.class)
@RequiredArgsConstructor
public class OpenSkyConfiguration {
    private final OpenSkyConfigProperties openSkyConfigProperties;

    @Bean
    WebClient webClient(WebClient.Builder builder) {
        return builder
                .baseUrl(openSkyConfigProperties.getBaseUrl().toString())
                .build();
    }
}
