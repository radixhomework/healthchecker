package io.github.radixhomework.healthchecker.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.service.annotation.GetExchange;

public interface HealthRestClient {

    @GetExchange("/")
    ResponseEntity<String> ping();
}
