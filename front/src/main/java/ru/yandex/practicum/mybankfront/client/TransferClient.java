package ru.yandex.practicum.mybankfront.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mybankfront.model.ServiceResultDto;
import ru.yandex.practicum.mybankfront.model.client.TransferDto;


@Slf4j
@Component
public class TransferClient {

    private final WebClient webClient;

    public TransferClient(WebClient webClient) {
        this.webClient = webClient.mutate().baseUrl("http://localhost:8085").build();
    }

    public ServiceResultDto transfer(TransferDto body) {
        return webClient
                .put().uri("/transfer/submit")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(ServiceResultDto.class)
                .block();
    }
}
