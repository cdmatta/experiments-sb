package com.github.cdmatta.experiment.flight.service;

import com.github.cdmatta.experiment.flight.config.OpenSkyConfigProperties;
import com.github.cdmatta.experiment.flight.domain.Aircraft;
import com.github.cdmatta.experiment.flight.domain.Flight;
import com.github.cdmatta.experiment.flight.repository.AircraftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
@RequiredArgsConstructor
public class FlightService {
    private final AircraftRepository repository;
    private final OpenSkyConfigProperties openSkyConfigProperties;
    private final WebClient webClient;

    public Mono<Flight> getAllFlights() {
        return webClient.get()
                .uri(openSkyConfigProperties.getAllStates())
                .accept(APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Flight.class);
    }

    public Mono<Aircraft> getFlightDetail(String icao24) {
        return repository.findByIcao(icao24);
    }
}