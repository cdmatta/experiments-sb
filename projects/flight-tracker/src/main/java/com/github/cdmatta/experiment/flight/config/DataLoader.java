package com.github.cdmatta.experiment.flight.config;

import com.github.cdmatta.experiment.flight.domain.Aircraft;
import com.github.cdmatta.experiment.flight.repository.AircraftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    private final ResourceLoader resourceLoader;
    private final AircraftRepository repository;

    List<Aircraft> aircrafts = new ArrayList<>();

    @Override
    public void run(String... args) throws Exception {
        Resource resource = resourceLoader.getResource("classpath:aircraftDatabase.csv");
        log.info("Start loading data ...");
        InputStream is = resource.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        CSVParser parser = new CSVParser(br, CSVFormat.EXCEL.withHeader().withTrim());

        Iterable<CSVRecord> records = parser.getRecords();
        records.forEach(r -> {
            if (!r.get("icao24").isEmpty())
                aircrafts.add(
                        new Aircraft(r.get("icao24"),
                                r.get("registration"),
                                r.get("manufacturericao"),
                                r.get("manufacturername"),
                                r.get("model"),
                                r.get("owner"),
                                r.get("operator"),
                                r.get("reguntil"),
                                r.get("engines"),
                                r.get("built")
                        ));
        });

        repository.saveAll(aircrafts)
                .subscribe(
                        v -> log.info("saving {}", v),
                        e -> log.error("Saving Failed", e),
                        () -> log.info("Loading Data Complete!")
                );
    }
}