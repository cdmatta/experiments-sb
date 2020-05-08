package com.github.cdmatta.experiment.dpkg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.springframework.boot.Banner.Mode.OFF;

@SpringBootApplication
public class Dpkg {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Dpkg.class);
        app.setBannerMode(OFF);
        app.run(args);
    }
}