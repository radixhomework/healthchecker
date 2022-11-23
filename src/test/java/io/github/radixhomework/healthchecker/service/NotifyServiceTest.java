package io.github.radixhomework.healthchecker.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.icegreen.greenmail.store.FolderException;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
//@WireMockTest(httpPort = 64001)
class NotifyServiceTest {

    @Value("${health.check.uri}")
    String uri;

    @Autowired
    NotifyService service;

    static GreenMail greenMail;
    static WireMockServer wireMockServer;

    @BeforeAll
    static void beforeAll() throws InterruptedException {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8089));
        wireMockServer.start();
        while (!wireMockServer.isRunning()) {
            // wait until WireMock server is ready
            Thread.currentThread().wait(100);
        }

        greenMail = new GreenMail(ServerSetupTest.SMTP);
        greenMail.setUser("login", "password");
        greenMail.start();
        while (!greenMail.isRunning()) {
            // wait until SMTP server is ready
            Thread.currentThread().wait(100);
        }
    }

    @AfterEach
    void afterEach() throws FolderException {
        greenMail.purgeEmailFromAllMailboxes();
    }

    @AfterAll
    static void afterAll() {
        wireMockServer.stop();
        greenMail.stop();
    }

    @Test
    void notifyMail() throws IOException, MessagingException {
        HealthCheckEntity entity = new HealthCheckEntity(uri);
        entity.setStatus(EnumStatus.SUCCESS);
        entity.setMessage("HTTP 200 - OK");
        entity.setTimestamp(Instant.ofEpochMilli(100000));
        entity.setHttpStatus(HttpStatus.valueOf(200));

        assertTrue(service.notifyMail(entity));

        MimeMessage received = greenMail.getReceivedMessages()[0];
        assertEquals("[Success] Internet Connection", received.getSubject());
        Multipart content = (Multipart) received.getContent();
        MimeBodyPart bodyPart = (MimeBodyPart) content.getBodyPart(0);
        String body = new String(bodyPart.getInputStream().readAllBytes());
        String expected = new String(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("templates/mail-ok.html").readAllBytes());

        assertTrue(body.translateEscapes().contains(expected.translateEscapes()));
    }

    @Test
    void notifyMailNullHttpStatus() throws IOException, MessagingException {
        HealthCheckEntity entity = new HealthCheckEntity(uri);
        entity.setStatus(EnumStatus.SUCCESS);
        entity.setMessage("HTTP 200 - OK");
        entity.setTimestamp(Instant.ofEpochMilli(100000));

        assertTrue(service.notifyMail(entity));

        MimeMessage received = greenMail.getReceivedMessages()[0];
        assertEquals("[Success] Internet Connection", received.getSubject());
        Multipart content = (Multipart) received.getContent();
        MimeBodyPart bodyPart = (MimeBodyPart) content.getBodyPart(0);
        String body = new String(bodyPart.getInputStream().readAllBytes());
        String expected = new String(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("templates/mail-ok-null-message.html").readAllBytes());

        log.info(body);
        log.info(expected);

        assertTrue(body.translateEscapes().contains(expected.translateEscapes()));
    }

    @Test
    void notifyDiscord() throws IOException {
        wireMockServer.stubFor(WireMock.post("/discord").willReturn(WireMock.ok()));

        HealthCheckEntity entity = new HealthCheckEntity(uri);
        entity.setStatus(EnumStatus.SUCCESS);
        entity.setMessage("HTTP 200 - OK");
        entity.setTimestamp(Instant.ofEpochMilli(100000));
        entity.setHttpStatus(HttpStatus.valueOf(200));

        assertTrue(service.notifyDiscord(entity));

        List<ServeEvent> allServeEvents = wireMockServer.getAllServeEvents();
        String body = allServeEvents.get(0).getRequest().getBodyAsString();
        String expected = new String(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("templates/discord-ok.json").readAllBytes());
        assertEquals(expected.translateEscapes(), body.translateEscapes());
    }

    @Test
    void notifyWithException() {
        NotifyService localService = new NotifyService(null, null);

        assertFalse(localService.notifyDiscord(new HealthCheckEntity(uri)));
        assertFalse(localService.notifyMail(new HealthCheckEntity(uri)));
        assertFalse(localService.notifyAll(new HealthCheckEntity(uri)));
    }

}
