package io.github.radixhomework.healthchecker.service;

import io.github.radixhomework.healthchecker.model.HealthCheckResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotifyService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String from;

    @Value("${spring.mail.to}")
    private String to;

    @Value("${spring.mail.subject}")
    private String subject;

    public void notify(HealthCheckResult healthCheckResult) {
        try (InputStream is = NotifyService.class.getClassLoader().getResourceAsStream("templates/main-template.html")) {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to.split(","));
            helper.setSubject(fill(subject, healthCheckResult.getReplacementMap()));
            helper.setText(fill(new String(is.readAllBytes()), healthCheckResult.getReplacementMap()), true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Error while sending failure notification", e);
        }
    }

    private String fill(String toFill, Map<String, String> values) {
        String clone = toFill;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            clone = clone.replaceAll(String.format("\\{\\{%s\\}\\}", entry.getKey()), entry.getValue());
        }
        return clone;
    }
}
