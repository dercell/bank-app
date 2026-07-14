package ru.yandex.practicum.mybankfront.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.practicum.mybankfront.controller.dto.AccountInfoDto;

@Slf4j
@Component
public class AccountClient {

    private final WebClient webClient;

    public AccountClient(@Value("${custom.baseUrl.account}") String baseUrl) {
        webClient = WebClient.builder()
                .baseUrl(baseUrl).build();
    }

    public AccountInfoDto getMyAccount() {
        try {
            AccountInfoDto myAcc = webClient.get()
                    .uri("/myAccount")
                    .retrieve()
                    .bodyToMono(AccountInfoDto.class)
                    .block();
            log.info("my account {}", myAcc);

            return myAcc;
        } catch (Exception error) {
            log.error("Error while getting current user: {}", error.getMessage(), error);
            throw error;
        }
    }

}
