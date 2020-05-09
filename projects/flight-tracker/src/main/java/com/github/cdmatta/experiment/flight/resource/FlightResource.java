package com.github.cdmatta.experiment.flight.resource;

import com.github.cdmatta.experiment.flight.domain.Aircraft;
import com.github.cdmatta.experiment.flight.domain.Flight;
import com.github.cdmatta.experiment.flight.service.FlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class FlightResource {
    private final FlightService service;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE, value = "/flights")
    @ResponseBody
    Flux<Flight> flights() {
        return Flux.from(service.getAllFlights());
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/aircraft/{icao}")
    @ResponseBody
    Mono<Aircraft> aircraft(@PathVariable String icao) {
        return service.getFlightDetail(icao);
    }
}
