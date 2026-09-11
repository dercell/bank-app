package ru.yandex.practicum.transfer.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.yandex.practicum.transfer.dto.ServiceResultDto;
import ru.yandex.practicum.transfer.dto.TransferDto;

@Slf4j
@Component
public class AccountClient {

    private final WebClient webClient;

    public AccountClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public ServiceResultDto transfer(TransferDto body) {
        try {
            return webClient
                    .put().uri("/accounts/transfer")
                    .bodyValue(body)
                    .header("Content-Type", "application/json")
                    .retrieve()
                    .bodyToMono(ServiceResultDto.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("WebClientResponseException in AccountClient transfer: {}: {}", e.getMessage(), e.getResponseBodyAsString(), e);
            throw e;
        } catch (Exception error) {
            log.error("Error while transfer: {}", error.getMessage(), error);
            throw error;
        }

    }

}
