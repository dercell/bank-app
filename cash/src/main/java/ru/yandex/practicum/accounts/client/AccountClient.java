package ru.yandex.practicum.accounts.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AccountClient {

    private final WebClient webClient;

    public AccountClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public void chargeBalance(String login, String action, int sum) {
        webClient.put().uri(uriBuilder -> uriBuilder
                        .path("/accounts/info/{login}")
                        .queryParam("action", action)
                        .queryParam("sum", sum)
                        .build(login))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

}
