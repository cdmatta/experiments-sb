package com.github.cdmatta.experiment.flight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.springframework.boot.Banner.Mode.OFF;

@SpringBootApplication
public class FlightTracker {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(FlightTracker.class);
        app.setBannerMode(OFF);
        app.run(args);
    }
}
