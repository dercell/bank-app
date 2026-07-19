package ru.yandex.practicum.mybankfront.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.practicum.mybankfront.controller.dto.AccountInfoDto;

@Slf4j
@Component
public class AccountClient {

    private final WebClient webClient;

    public AccountClient
            (@Qualifier("loadBalancedWebClientBuilder") WebClient.Builder builder,
             @Value("${custom.baseUrl.api-gateway}") String baseUrl) {
        webClient = builder
                .baseUrl(baseUrl).build();
    }

    public AccountInfoDto getMyAccount() {
        try {
            AccountInfoDto myAcc = webClient.get()
                    .uri("/accounts/info")
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
