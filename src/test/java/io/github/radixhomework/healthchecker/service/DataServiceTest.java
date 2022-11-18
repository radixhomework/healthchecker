package io.github.radixhomework.healthchecker.service;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import io.github.radixhomework.healthchecker.entity.HealthCheckEntity;
import io.github.radixhomework.healthchecker.enums.EnumStatus;
import io.github.radixhomework.healthchecker.repository.HealthCheckRepository;
import io.github.radixhomework.healthchecker.util.EmbedMongoDBUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DataServiceTest {

    @Value("${health.check.uri}")
    String urlToCheck;

    @Value("${spring.data.mongodb.username}")
    String dbUsername;

    @Value("${spring.data.mongodb.password}")
    String dbPassword;

    @Autowired
    DataService service;

    @Autowired
    HealthCheckRepository repository;

    MongodExecutable mongodExecutable;
    MongodProcess dbProcess;

    @BeforeEach
    void beforeEach() throws Exception {
        mongodExecutable = EmbedMongoDBUtils.prepareExecutable("localhost", 27017, dbUsername, dbPassword);
        dbProcess = mongodExecutable.start();
        while (!dbProcess.isProcessRunning()) {
            // wait until embed Mongodb is ready
            Thread.currentThread().wait(100);
        }
    }

    @AfterEach
    void afterEach() throws Exception {
        mongodExecutable.stop();
        while (dbProcess.isProcessRunning()) {
            // wait until embed Mongodb is stopped
            Thread.currentThread().wait(100);
        }
    }

    @Test
    void getInitialStatusFromDatabase() {
        EnumStatus given = service.getLastStatus(urlToCheck);
        Assertions.assertNotNull(given);
        Assertions.assertEquals(EnumStatus.UNKNOWN, given);
    }

    @ParameterizedTest
    @EnumSource(value = EnumStatus.class, names = {"SUCCESS", "FAILURE"})
    void testSaveAndGetLastStatusFromDatabase(EnumStatus status) {
        HealthCheckEntity entity = new HealthCheckEntity(urlToCheck);
        entity.setStatus(status);
        service.saveHealthCheckResult(entity);

        EnumStatus given = service.getLastStatus(urlToCheck);
        Assertions.assertNotNull(given);
        Assertions.assertEquals(status, given);
    }
}
