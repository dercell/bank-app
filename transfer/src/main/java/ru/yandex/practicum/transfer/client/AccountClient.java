package ru.yandex.practicum.transfer.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.transfer.dto.ServiceResultDto;

@Slf4j
@Component
public class AccountClient {

    private final WebClient webClient;

    public AccountClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public ServiceResultDto transfer(String from, String to, int sum) {
        try {
            return webClient
                    .put().uri(uriBuilder -> uriBuilder
                            .path("/accounts/transfer")
                            .queryParam("from", from)
                            .queryParam("to", to)
                            .queryParam("sum", sum)
                            .build())
                    .header("Content-Type", "application/json")
                    .retrieve()
                    .bodyToMono(ServiceResultDto.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("WebClientResponseException in AccountClient transfer: {}", e.getMessage(), e);
            throw e;
        } catch (Exception error) {
            log.error("Error while transfer: {}", error.getMessage(), error);
            throw error;
        }

    }

}
