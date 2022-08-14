package io.github.radixhomework.healthchecker.model;

import io.github.radixhomework.healthchecker.enums.EnumStatus;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Data
public class HealthCheckResult {

    private EnumStatus status = EnumStatus.UNKNOWN;
    private final String host;
    private Instant begin = Instant.now();
    private Instant end;
    private HttpStatus httpStatus;
    private String message;
    private Instant lastSuccess;

    public Map<String, String> getReplacementMap() {
        Map<String, String> replacementMap = new HashMap<>();
        replacementMap.put("status", status.toString());
        replacementMap.put("host", getHost());
        replacementMap.put("begin", getBegin().toString());
        replacementMap.put("end", getEnd().toString());
        replacementMap.put("httpStatus", getHttpStatus() == null ? "N/A" : getHttpStatus().toString());
        replacementMap.put("message", getMessage() == null ? "N/A" : getMessage());
        replacementMap.put("lastSuccess", getLastSuccess() == null ? "N/A" : getLastSuccess().toString());
        return replacementMap;
    }
}
