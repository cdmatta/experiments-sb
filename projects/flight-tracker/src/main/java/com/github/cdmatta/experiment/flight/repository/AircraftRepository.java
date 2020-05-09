package com.github.cdmatta.experiment.flight.repository;

import com.github.cdmatta.experiment.flight.domain.Aircraft;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface AircraftRepository extends ReactiveCrudRepository<Aircraft, Long> {
    Mono<Aircraft> findByIcao(String icao);
}