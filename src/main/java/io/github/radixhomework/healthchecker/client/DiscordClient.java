package io.github.radixhomework.healthchecker.client;

import io.github.radixhomework.healthchecker.model.DiscordMessage;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

public interface DiscordClient {

    @PostExchange
    void postMessage(@RequestBody DiscordMessage message);
}
