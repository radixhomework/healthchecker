package io.github.radixhomework.healthchecker.configuration;

import io.github.radixhomework.healthchecker.client.HealthRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpClientConfiguration {

    @Value("${health.check.uri}")
    private String baseUrl;

    @Value("${health.check.api-key}")
    private String apiKey;

    @Bean
    public HealthRestClient healthRestClient(HttpServiceProxyFactory httpServiceProxyFactory) {
        return httpServiceProxyFactory.createClient(HealthRestClient.class);
    }

    @Bean
    public HttpServiceProxyFactory httpServiceProxyFactory() {
        return HttpServiceProxyFactory.builder()
                .clientAdapter(WebClientAdapter.forClient(
                        WebClient.builder()
                                .baseUrl(baseUrl)
                                .defaultHeader("X-API-KEY", apiKey)
                                .build()))
                .build();
    }
}
