package io.github.radixhomework.healthchecker.entity;

import io.github.radixhomework.healthchecker.enums.EnumStatus;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Data
@Document(collection = "healthcheck")
public class HealthCheckEntity {

    private String id;
    private EnumStatus status;
    private String host;
    private Instant begin;
    private Instant end;
    private HttpStatus httpStatus;
    private String message;
}
