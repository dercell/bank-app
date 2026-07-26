package ru.yandex.practicum.cash.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class AccountClient {

    private final WebClient webClient;

    public AccountClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public void chargeBalance(String login, String action, int sum) {
        try {
            webClient.put().uri(uriBuilder -> uriBuilder
                            .path("/accounts/charge/{login}")
                            .queryParam("action", action)
                            .queryParam("sum", sum)
                            .build(login))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception error) {
            log.error("Error while getting current user: {}", error.getMessage(), error);
            throw error;
        }
    }

}
