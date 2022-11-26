package io.github.radixhomework.healthchecker.service;

import io.github.radixhomework.healthchecker.client.HealthRestClient;
import io.github.radixhomework.healthchecker.entity.HealthCheckEntity;
import io.github.radixhomework.healthchecker.enums.EnumStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
class HealthCheckServiceTest {

    @MockBean
    HealthRestClient healthRestClient;
    @MockBean
    DataService dataService;
    @MockBean
    NotifyService notifyService;

    @Autowired
    HealthCheckService healthCheckService;

    @Value("${health.check.uri}")
    String urlToCheck;

    @Value("${health.check.response}")
    String expectedResponse;

    @Captor
    ArgumentCaptor<HealthCheckEntity> dataSaveCaptor;
    @Captor
    ArgumentCaptor<HealthCheckEntity> notifyCaptor;

    @Test
    void successWithoutChange() {
        ResponseEntity<String> response = new ResponseEntity<>(expectedResponse, HttpStatusCode.valueOf(200));
        when(healthRestClient.ping()).thenReturn(response);
        when(dataService.getLastStatus(anyString())).thenReturn(EnumStatus.SUCCESS);

        healthCheckService.doCheck(urlToCheck);

        verify(dataService).saveHealthCheckResult(dataSaveCaptor.capture());
        verifyNoInteractions(notifyService);

        HealthCheckEntity expected = new HealthCheckEntity(urlToCheck);
        expected.setHttpStatus(HttpStatus.OK);
        expected.setStatus(EnumStatus.SUCCESS);
        expected.setMessage("N/A");

        assertEquals(expected, dataSaveCaptor.getValue());
    }

    @ParameterizedTest
    @EnumSource(value = EnumStatus.class, names = {"UNKNOWN", "FAILURE"})
    void successWithChange(EnumStatus status) {
        ResponseEntity<String> response = new ResponseEntity<>(expectedResponse, HttpStatusCode.valueOf(200));
        when(healthRestClient.ping()).thenReturn(response);
        when(dataService.getLastStatus(anyString())).thenReturn(status);

        healthCheckService.doCheck(urlToCheck);

        verify(dataService).saveHealthCheckResult(dataSaveCaptor.capture());
        verify(notifyService).notifyAll(notifyCaptor.capture());

        HealthCheckEntity expected = new HealthCheckEntity(urlToCheck);
        expected.setHttpStatus(HttpStatus.OK);
        expected.setStatus(EnumStatus.SUCCESS);
        expected.setMessage("N/A");

        assertEquals(expected, dataSaveCaptor.getValue());
        assertEquals(expected, notifyCaptor.getValue());
    }

    @Test
    void responseFailureWithoutChange() {
        String responseMessage = "pang";
        ResponseEntity<String> response = new ResponseEntity<>(responseMessage, HttpStatusCode.valueOf(200));
        when(healthRestClient.ping()).thenReturn(response);
        when(dataService.getLastStatus(anyString())).thenReturn(EnumStatus.FAILURE);

        healthCheckService.doCheck(urlToCheck);

        verify(dataService).saveHealthCheckResult(dataSaveCaptor.capture());
        verifyNoInteractions(notifyService);

        HealthCheckEntity expected = new HealthCheckEntity(urlToCheck);
        expected.setHttpStatus(HttpStatus.OK);
        expected.setStatus(EnumStatus.FAILURE);
        expected.setMessage(responseMessage);

        assertEquals(expected, dataSaveCaptor.getValue());
    }

    @ParameterizedTest
    @EnumSource(value = EnumStatus.class, names = {"UNKNOWN", "SUCCESS"})
    void responseFailureWithChange(EnumStatus status) {
        String responseMessage = "pang";
        ResponseEntity<String> response = new ResponseEntity<>(responseMessage, HttpStatusCode.valueOf(200));
        when(healthRestClient.ping()).thenReturn(response);
        when(dataService.getLastStatus(anyString())).thenReturn(status);

        healthCheckService.doCheck(urlToCheck);

        verify(dataService).saveHealthCheckResult(dataSaveCaptor.capture());
        verify(notifyService).notifyAll(notifyCaptor.capture());

        HealthCheckEntity expected = new HealthCheckEntity(urlToCheck);
        expected.setHttpStatus(HttpStatus.OK);
        expected.setStatus(EnumStatus.FAILURE);
        expected.setMessage(responseMessage);

        assertEquals(expected, dataSaveCaptor.getValue());
        assertEquals(expected, notifyCaptor.getValue());
    }

    @Test
    void httpClientExceptionWithoutChange() {
        when(healthRestClient.ping()).thenThrow(new HttpClientErrorException(HttpStatusCode.valueOf(404)));
        when(dataService.getLastStatus(anyString())).thenReturn(EnumStatus.FAILURE);

        healthCheckService.doCheck(urlToCheck);

        verify(dataService).saveHealthCheckResult(dataSaveCaptor.capture());
        verifyNoInteractions(notifyService);

        HealthCheckEntity expected = new HealthCheckEntity(urlToCheck);
        expected.setHttpStatus(HttpStatus.NOT_FOUND);
        expected.setStatus(EnumStatus.FAILURE);

        assertEquals(expected, dataSaveCaptor.getValue());
    }

    @ParameterizedTest
    @EnumSource(value = EnumStatus.class, names = {"UNKNOWN", "SUCCESS"})
    void httpClientExceptionWithChange(EnumStatus status) {
        when(healthRestClient.ping()).thenThrow(new HttpClientErrorException(HttpStatusCode.valueOf(404)));
        when(dataService.getLastStatus(anyString())).thenReturn(status);

        healthCheckService.doCheck(urlToCheck);

        verify(dataService).saveHealthCheckResult(dataSaveCaptor.capture());
        verify(notifyService).notifyAll(notifyCaptor.capture());

        HealthCheckEntity expected = new HealthCheckEntity(urlToCheck);
        expected.setHttpStatus(HttpStatus.NOT_FOUND);
        expected.setStatus(EnumStatus.FAILURE);

        assertEquals(expected, dataSaveCaptor.getValue());
        assertEquals(expected, notifyCaptor.getValue());
    }

    @Test
    void otherExceptionWithoutChange() {
        String message = "Fake Exception During Ping";
        when(healthRestClient.ping()).thenThrow(new RuntimeException(message));
        when(dataService.getLastStatus(anyString())).thenReturn(EnumStatus.FAILURE);

        healthCheckService.doCheck(urlToCheck);

        verify(dataService).saveHealthCheckResult(dataSaveCaptor.capture());
        verifyNoInteractions(notifyService);

        HealthCheckEntity expected = new HealthCheckEntity(urlToCheck);
        expected.setStatus(EnumStatus.FAILURE);
        expected.setMessage(message);

        assertEquals(expected, dataSaveCaptor.getValue());
    }

    @ParameterizedTest
    @EnumSource(value = EnumStatus.class, names = {"UNKNOWN", "SUCCESS"})
    void otherExceptionWithChange(EnumStatus status) {
        String message = "Fake Exception During Ping";
        when(healthRestClient.ping()).thenThrow(new RuntimeException(message));
        when(dataService.getLastStatus(anyString())).thenReturn(status);

        healthCheckService.doCheck(urlToCheck);

        verify(dataService).saveHealthCheckResult(dataSaveCaptor.capture());
        verify(notifyService).notifyAll(notifyCaptor.capture());

        HealthCheckEntity expected = new HealthCheckEntity(urlToCheck);
        expected.setStatus(EnumStatus.FAILURE);
        expected.setMessage(message);

        assertEquals(expected, dataSaveCaptor.getValue());
        assertEquals(expected, notifyCaptor.getValue());
    }
}
