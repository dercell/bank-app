package ru.yandex.practicum.transfer.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;

@Slf4j
@Component
public class NotificationClient {

    private final WebClient webClient;

    public NotificationClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public void sendNotification(String message) {
        webClient
                .post().uri("/notification")
                .bodyValue(Map.of("sourceService", "TRANSFER", "message", message))
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(throwable -> log.error("Ошибка при обращении к notification-service: " + throwable.getMessage()))
                .block();
    }

}
