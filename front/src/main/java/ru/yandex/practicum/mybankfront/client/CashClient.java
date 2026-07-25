package ru.yandex.practicum.mybankfront.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.yandex.practicum.mybankfront.controller.dto.CashAction;

@Slf4j
@Component
public class CashClient {

    private final WebClient webClient;

    public CashClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public void chargeSum(String login, CashAction action, int value) {
        try {
            log.info("User {} trying to {} {} money", login, action, value);
            webClient.put().uri(uriBuilder -> uriBuilder
                            .path("/cash/{login}")
                            .queryParam("action", action.toString())
                            .queryParam("sum", value)
                            .build(login))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Error in CashClient chargeSum: {}", e.getMessage(), e);
            throw e;
        }
    }
}
