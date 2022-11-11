package io.github.radixhomework.healthchecker.service;

import io.github.radixhomework.healthchecker.entity.HealthCheckEntity;
import io.github.radixhomework.healthchecker.enums.EnumStatus;
import io.github.radixhomework.healthchecker.repository.HealthCheckRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataService {

    private final HealthCheckRepository repository;

    public EnumStatus getLastStatus(String uri) {
        HealthCheckEntity result = repository.findFirstByHostOrderByTimestampDesc(uri);
        if (result == null) {
            return EnumStatus.UNKNOWN;
        } else {
            return result.getStatus();
        }
    }

    public void saveHealthCheckResult(HealthCheckEntity result) {
        repository.save(result);
    }
}
