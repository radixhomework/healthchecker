package io.github.radixhomework.healthchecker.service;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import io.github.radixhomework.healthchecker.entity.HealthCheckEntity;
import io.github.radixhomework.healthchecker.enums.EnumStatus;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatusCode;

import java.io.IOException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest
class NotifyServiceTest {

    static final String EXAMPLE_URI = "http://an-example.uri";
    @Autowired
    NotifyService service;

    static GreenMail greenMail;

    @BeforeAll
    static void beforeAll() throws InterruptedException {
        greenMail = new GreenMail(ServerSetupTest.SMTP);
        greenMail.setUser("login", "password");
        greenMail.start();
        while (!greenMail.isRunning()) {
            // wait until SMTP server is ready
            Thread.currentThread().wait(100);
        }
    }

    @AfterAll
    static void afterAll() {
        greenMail.stop();
    }

    @Test
    void notifyWithHttpStatus() throws IOException, MessagingException {
        HealthCheckEntity entity = new HealthCheckEntity(EXAMPLE_URI);
        entity.setStatus(EnumStatus.SUCCESS);
        entity.setMessage("HTTP 200 - OK");
        entity.setTimestamp(Instant.ofEpochMilli(100000));
        entity.setHttpStatus(HttpStatusCode.valueOf(200));

        assertTrue(service.notify(entity));

        MimeMessage received = greenMail.getReceivedMessages()[0];
        assertEquals("[Success] Internet Connection", received.getSubject());
        Multipart content = (Multipart) received.getContent();
        MimeBodyPart bodyPart = (MimeBodyPart) content.getBodyPart(0);
        String body = new String(bodyPart.getInputStream().readAllBytes());
        String expected = new String(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("templates/expected-ok.html").readAllBytes());

        log.info("Expected: {}", expected);
        log.info("Given: {}", body);
        assertTrue(body.contains(expected));
    }

}
