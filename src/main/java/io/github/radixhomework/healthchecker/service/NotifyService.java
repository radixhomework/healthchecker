package io.github.radixhomework.healthchecker.service;

import io.github.radixhomework.healthchecker.entity.HealthCheckEntity;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.stringtemplate.v4.ST;

import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String from;

    @Value("${spring.mail.to}")
    private String to;

    private static final String SUBJECT_PATTERN = "[%s] Internet Connection";

    public void notify(HealthCheckEntity healthCheck) {
        try (InputStream is = NotifyService.class.getClassLoader().getResourceAsStream("templates/main-template.st")) {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(from);
            helper.setTo(to.split(","));

            helper.setSubject(String.format(SUBJECT_PATTERN, healthCheck.getStatus().getCode()));

            ST body = new ST(new String(is.readAllBytes()), '$', '$');
            body.add("status", healthCheck.getStatus().getCode());
            body.add("host", healthCheck.getHost());
            body.add("timestamp", healthCheck.getTimestamp());
            if (healthCheck.getHttpStatus() != null) {
                body.add("httpStatus", healthCheck.getHttpStatus().value());
            } else {
                body.add("httpStatus", "null");
            }
            body.add("message", healthCheck.getMessage());

            helper.setText(body.render(), true);

            mailSender.send(message);
        } catch (Exception e) {
            log.error("Error while sending failure notification", e);
        }
    }
}
