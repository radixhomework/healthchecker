package io.github.radixhomework.healthchecker.entity;

import io.github.radixhomework.healthchecker.enums.EnumStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.TimeSeries;
import org.springframework.data.mongodb.core.timeseries.Granularity;
import org.springframework.http.HttpStatusCode;

import java.time.Instant;

@Data
@TimeSeries(collection = "healthcheck", timeField = "timestamp", metaField = "host", granularity = Granularity.MINUTES)
public class HealthCheckEntity {

    @EqualsAndHashCode.Exclude
    private String id;

    private EnumStatus status;

    @Setter(AccessLevel.NONE)
    private final String host;

    @EqualsAndHashCode.Exclude
    private Instant timestamp = Instant.now();

    private HttpStatusCode httpStatus;

    private String message;

}
