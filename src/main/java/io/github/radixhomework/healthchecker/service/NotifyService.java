package io.github.radixhomework.healthchecker.service;

import io.github.radixhomework.healthchecker.client.DiscordClient;
import io.github.radixhomework.healthchecker.entity.HealthCheckEntity;
import io.github.radixhomework.healthchecker.model.DiscordMessage;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.stringtemplate.v4.ST;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyService {

    private final JavaMailSender mailSender;
    private final DiscordClient discordClient;

    @Value("${spring.mail.from}")
    private String from;

    @Value("${spring.mail.to}")
    private String to;

    private static final String SUBJECT_PATTERN = "[%s] Internet Connection";

    public boolean notifyAll(HealthCheckEntity healthCheck) {
        boolean out = notifyMail(healthCheck);
        out &= notifyDiscord(healthCheck);
        return out;
    }

    public boolean notifyDiscord(HealthCheckEntity healthCheck) {
        boolean out = true;
        try (InputStream is = NotifyService.class.getClassLoader().getResourceAsStream("templates/discord-template.st")) {
            DiscordMessage message = new DiscordMessage();
            message.setUsername("HealthChecker");
            message.setContent(getBody(healthCheck, is));
            discordClient.postMessage(message);
        } catch (Exception e) {
            log.error("Error while sending failure notification", e);
            out = false;
        }
        return out;
    }

    public boolean notifyMail(HealthCheckEntity healthCheck) {
        boolean out = true;
        try (InputStream is = NotifyService.class.getClassLoader().getResourceAsStream("templates/main-template.st")) {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(from);
            helper.setTo(to.split(","));
            helper.setSubject(String.format(SUBJECT_PATTERN, healthCheck.getStatus().getCode()));
            helper.setText(getBody(healthCheck, is), true);

            mailSender.send(message);
        } catch (Exception e) {
            log.error("Error while sending failure notification", e);
            out = false;
        }
        return out;
    }

    private String getBody(HealthCheckEntity healthCheck, InputStream templateIs) throws IOException {
        ST body = new ST(new String(templateIs.readAllBytes()), '$', '$');
        body.add("status", healthCheck.getStatus().getCode());
        body.add("host", healthCheck.getHost());
        body.add("timestamp", healthCheck.getTimestamp());
        if (healthCheck.getHttpStatus() != null) {
            body.add("httpStatus", healthCheck.getHttpStatus().value());
        } else {
            body.add("httpStatus", "null");
        }
        body.add("message", healthCheck.getMessage());
        return body.render();
    }
}
