package io.github.radixhomework.healthchecker.service;

import io.github.radixhomework.healthchecker.client.HealthRestClient;
import io.github.radixhomework.healthchecker.entity.HealthCheckEntity;
import io.github.radixhomework.healthchecker.enums.EnumStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthCheckService {

    private final HealthRestClient healthRestClient;
    private final DataService dataService;
    private final NotifyService notifyService;

    @Value("${health.check.response}")
    private String expected;

    public void doCheck(String uri) {
        HealthCheckEntity healthCheck = new HealthCheckEntity(uri);
        try {
            ResponseEntity<String> response = healthRestClient.ping();
            healthCheck.setHttpStatus(HttpStatus.valueOf(response.getStatusCode().value()));
            if (response.getStatusCode().isError() || !expected.equals(response.getBody())) {
                healthCheck.setStatus(EnumStatus.FAILURE);
                healthCheck.setMessage(response.getBody());
                log.warn("HealthCheck failure: {}", healthCheck);
            } else {
                healthCheck.setStatus(EnumStatus.SUCCESS);
                healthCheck.setMessage("N/A");
                log.info("HealthCheck success: {}", healthCheck);
            }
        } catch (HttpClientErrorException hcee) {
            healthCheck.setStatus(EnumStatus.FAILURE);
            healthCheck.setHttpStatus(HttpStatus.valueOf(hcee.getStatusCode().value()));
            log.warn("HealthCheck failure: {}", healthCheck);
        } catch (Exception e) {
            healthCheck.setStatus(EnumStatus.FAILURE);
            healthCheck.setMessage(e.getMessage());
            log.error("HealthCheck error: {}", healthCheck);
        } finally {
            EnumStatus lastStatus = dataService.getLastStatus(uri);
            if (!lastStatus.equals(healthCheck.getStatus())) {
                log.info("Status has changed, notifying");
                notifyService.notify(healthCheck);
            }
            dataService.saveHealthCheckResult(healthCheck);
        }
    }
}
