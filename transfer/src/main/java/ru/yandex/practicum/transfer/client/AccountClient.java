package ru.yandex.practicum.transfer.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
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
                .onErrorResume(throwable -> Mono.just(new ServiceResultDto("Ошибка при обращении к account-service: " + throwable.getMessage())))
                .block();
    }

}
