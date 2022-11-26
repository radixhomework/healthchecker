package io.github.radixhomework.healthchecker.service;

import io.github.radixhomework.healthchecker.entity.HealthCheckEntity;
import io.github.radixhomework.healthchecker.enums.EnumStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@SpringBootTest
class ValidationServiceTest {

    @Autowired
    ValidationService service;

    @Test
    void nullHost() {
        HealthCheckEntity entity = new HealthCheckEntity(null);
        entity.setId(UUID.randomUUID().toString());
        entity.setHttpStatus(HttpStatus.OK);
        entity.setStatus(EnumStatus.SUCCESS);
        entity.setMessage("This is a message");
        Assertions.assertFalse(service.validate(entity, "Null Host"));
    }

    @Test
    void nullStatus() {
        HealthCheckEntity entity = new HealthCheckEntity("http://fake.uri");
        entity.setId(UUID.randomUUID().toString());
        entity.setHttpStatus(HttpStatus.OK);
        entity.setStatus(null);
        entity.setMessage("This is a message");
        Assertions.assertFalse(service.validate(entity, "Null Status"));
    }

    @Test
    void nullTimestamp() {
        HealthCheckEntity entity = new HealthCheckEntity("http://fake.uri");
        entity.setId(UUID.randomUUID().toString());
        entity.setHttpStatus(HttpStatus.OK);
        entity.setTimestamp(null);
        entity.setStatus(EnumStatus.SUCCESS);
        entity.setMessage("This is a message");
        Assertions.assertFalse(service.validate(entity, "Null Timestamp"));
    }
}
