package ru.yandex.practicum.mybankfront.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mybankfront.model.ServiceResultDto;


@Slf4j
@Component
public class TransferClient {

    private final WebClient webClient;

    public TransferClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public ServiceResultDto transfer(String fromLogin, String toLogin, int sum) {
        return webClient
                .put().uri(uriBuilder -> uriBuilder
                        .path("/transfer/submit")
                        .queryParam("from", fromLogin)
                        .queryParam("to", toLogin)
                        .queryParam("sum", sum)
                        .build())
                .retrieve()
                .bodyToMono(ServiceResultDto.class)
                .onErrorResume(throwable -> Mono.just(new ServiceResultDto("Ошибка при обращении к transfer-service: " + throwable.getMessage())))
                .block();
    }
}
