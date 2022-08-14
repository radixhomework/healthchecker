package io.github.radixhomework.healthchecker.service;

import io.github.radixhomework.healthchecker.enums.EnumStatus;
import io.github.radixhomework.healthchecker.model.HealthCheckResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public class HealthCheckService {

    private final RestTemplate restTemplate;
    private final NotifyService notifyService;
    private final DataService dataService;

    @Value("${health.check.response}")
    private String expected;

    public void doCheck(String uri) {
        // Begin timestamp is set when the object is instantiated
        HealthCheckResult hcr = new HealthCheckResult(uri);
        // Consider that the health check is not OK by default
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
            hcr.setEnd(Instant.now());
            hcr.setHttpStatus(response.getStatusCode());
            if (response.getStatusCode().isError() || !expected.equals(response.getBody())) {
                hcr.setStatus(EnumStatus.ERROR);
                hcr.setMessage(response.getBody());
                log.warn("HealthCheck failure: {}", hcr);
            } else {
                hcr.setStatus(EnumStatus.SUCCESS);
                hcr.setMessage("N/A");
                log.info("HealthCheck success: {}", hcr);
            }
        } catch (HttpClientErrorException hcee) {
            hcr.setStatus(EnumStatus.ERROR);
            hcr.setEnd(Instant.now());
            hcr.setHttpStatus(hcee.getStatusCode());
            log.warn("HealthCheck failure: {}", hcr);
        } catch (Exception e) {
            hcr.setStatus(EnumStatus.ERROR);
            hcr.setEnd(Instant.now());
            hcr.setMessage(e.getMessage());
            log.error("HealthCheck error: {}", hcr);
        }  finally {
            EnumStatus lastStatus = dataService.getLastStatus(hcr.getHost());
            dataService.saveHealthCheckResult(hcr);
            if (!hcr.getStatus().equals(lastStatus)) {
                notifyService.notify(hcr);
            }
        }
    }
}
