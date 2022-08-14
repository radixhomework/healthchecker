package io.github.radixhomework.healthchecker.mappers;

import io.github.radixhomework.healthchecker.entity.HealthCheckEntity;
import io.github.radixhomework.healthchecker.model.HealthCheckResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HealthCheckMapper {

    @Mapping(target = "id", ignore = true)
    HealthCheckEntity toEntity(HealthCheckResult result);

    @Mapping(target = "lastSuccess", ignore = true)
    HealthCheckResult fromEntity(HealthCheckEntity entity);
}
