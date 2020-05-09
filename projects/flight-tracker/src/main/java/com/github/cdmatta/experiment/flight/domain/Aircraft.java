package com.github.cdmatta.experiment.flight.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@Document(collection = "aircraft")
public class Aircraft {
    @Id
    private String icao;
    private String registration;
    private String manufacturericao;
    private String manufacturername;
    private String model;
    private String owner;
    private String operator;
    private String reguntil;
    private String engines;
    private String built;
}