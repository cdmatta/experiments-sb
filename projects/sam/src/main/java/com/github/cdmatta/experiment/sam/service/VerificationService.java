package com.github.cdmatta.experiment.sam.service;

import com.github.cdmatta.experiment.sam.domain.HttpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.Future;

@Service
@Slf4j
public class VerificationService {

    @Async
    public Future<Boolean> isServiceRunning(final HttpService service, final int timeout) {
        try {
            log.info("Check " + service);
            return new AsyncResult<>(service.checkStatus(timeout));
        } catch (Exception e) {
            log.error(service + " check failed.", e);
            return new AsyncResult<>(false);
        }
    }
}
