package io.github.radixhomework.healthchecker.entity;

import io.github.radixhomework.healthchecker.enums.EnumStatus;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "Status cannot be null")
    private EnumStatus status;

    @NotNull(message = "Hostname cannot be null")
    @Setter(AccessLevel.NONE)
    private final String host;

    @NotNull(message = "Timestamp cannot be null")
    @EqualsAndHashCode.Exclude
    private Instant timestamp = Instant.now();

    private HttpStatusCode httpStatus;

    private String message;

}
