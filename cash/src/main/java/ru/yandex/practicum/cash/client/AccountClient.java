package ru.yandex.practicum.cash.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.practicum.cash.dto.CashOpDto;

@Slf4j
@Component
public class AccountClient {

    private final WebClient webClient;

    public AccountClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public void chargeBalance(CashOpDto body) {
        webClient.put().uri("/accounts/charge")
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

}
