package io.github.radixhomework.healthchecker.service;

import io.github.radixhomework.healthchecker.entity.HealthCheckEntity;
import io.github.radixhomework.healthchecker.enums.EnumStatus;
import io.github.radixhomework.healthchecker.repository.HealthCheckRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.mockito.Mockito.*;

class DataServiceTest {

    public static final String EXAMPLE_URI = "http://an-example.uri";

    @Test
    void testGetLastStatusNoDataInDb() {
        HealthCheckRepository mockRepository = mock(HealthCheckRepository.class);
        when(mockRepository.findFirstByHostOrderByTimestampDesc(anyString())).thenReturn(null);
        DataService service = new DataService(mockRepository);

        EnumStatus given = service.getLastStatus(EXAMPLE_URI);
        Assertions.assertNotNull(given);
        Assertions.assertEquals(EnumStatus.UNKNOWN, given);
    }

    @ParameterizedTest
    @EnumSource(value = EnumStatus.class, names = { "SUCCESS", "FAILURE" }) // six numbers
    void testGetLastStatusDataFoundInDb(EnumStatus status) {
        HealthCheckRepository mockRepository = mock(HealthCheckRepository.class);
        HealthCheckEntity entity = new HealthCheckEntity(EXAMPLE_URI);
        entity.setStatus(status);
        when(mockRepository.findFirstByHostOrderByTimestampDesc(anyString())).thenReturn(entity);
        DataService service = new DataService(mockRepository);

        EnumStatus given = service.getLastStatus(EXAMPLE_URI);
        Assertions.assertNotNull(given);
        Assertions.assertEquals(status, given);
    }
}
