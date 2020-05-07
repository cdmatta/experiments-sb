package com.github.cdmatta.experiment.sam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.springframework.boot.Banner.Mode.OFF;

@SpringBootApplication
public class SAM {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SAM.class);
        app.setBannerMode(OFF);
        app.run(args);
    }
}
