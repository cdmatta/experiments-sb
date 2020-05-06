package com.github.cdmatta.experiment.test.clock;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;

@FixedClock
@ExtendWith(SpringExtension.class)
public class FixedClockUsageExampleTest {

    @Autowired
    private ClockUsingService service;

    @Test
    @FixedClock("2099-01-01T10:00:00Z")
    public void verifyMockedBeanTimeIsUtilized() {
        assertThat(service.getCurrentTime()).isEqualTo("2099-01-01T10:00:00Z");
    }

    @TestConfiguration
    @Service
    @RequiredArgsConstructor
    static class ClockUsingService {
        private final Clock clock;

        public String getCurrentTime() {
            return clock.instant().toString();
        }
    }
}
