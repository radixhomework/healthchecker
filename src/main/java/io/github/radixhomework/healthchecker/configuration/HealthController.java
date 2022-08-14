package io.github.radixhomework.healthchecker.configuration;

import io.github.radixhomework.healthchecker.service.HealthCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class HealthController {

    HealthCheckService service;

    @Value("${health.check.uri}")
    private String uri;

    @Scheduled(fixedDelayString = "${health.check.delay}")
    public void checkHealth() {
        log.info("HealthChecking Uri: {}", uri);
        service.doCheck(uri);
    }
}
