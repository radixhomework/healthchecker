package io.github.radixhomework.healthchecker.repository;

import io.github.radixhomework.healthchecker.entity.HealthCheckEntity;
import io.github.radixhomework.healthchecker.enums.EnumStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HealthCheckRepository extends MongoRepository<HealthCheckEntity, String> {

    HealthCheckEntity findFirstByHostOrderByTimestampDesc(String host);

    HealthCheckEntity findFirstByHostAndStatusOrderByTimestampDesc(String host, EnumStatus status);
}
