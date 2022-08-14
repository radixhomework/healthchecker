package io.github.radixhomework.healthchecker.repository;

import io.github.radixhomework.healthchecker.entity.HealthCheckEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HealthCheckRepository extends MongoRepository<HealthCheckEntity, String> {

    HealthCheckEntity findFirstByHostOrderByEndDesc(String host);
}
