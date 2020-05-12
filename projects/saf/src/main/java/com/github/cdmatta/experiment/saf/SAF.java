package com.github.cdmatta.experiment.saf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.springframework.boot.Banner.Mode.OFF;

@SpringBootApplication
public class SAF {

    public static void main(String[] args) {
        var app = new SpringApplication(SAF.class);
        app.setBannerMode(OFF);
        app.run(args);
    }
}