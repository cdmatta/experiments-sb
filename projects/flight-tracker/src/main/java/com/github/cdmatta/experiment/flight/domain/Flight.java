package com.github.cdmatta.experiment.flight.domain;

import lombok.Data;

import java.util.Collection;

@Data
public class Flight {
    private int time;
    private Collection<StateVector> states;
}