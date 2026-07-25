package ru.yandex.practicum.mybankfront.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.practicum.mybankfront.controller.dto.AccountInfoDto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class AccountClient {

    private final WebClient webClient;

    public AccountClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public AccountInfoDto getAccByLogin(String login) {
        try {
            log.info("Request for user {}", login);
            AccountInfoDto acc = webClient.get()
                    .uri("/accounts/info/{login}", login)
                    .retrieve()
                    .bodyToMono(AccountInfoDto.class)
                    .block();
            log.info("Account info :{}", acc);

            return acc;
        } catch (Exception error) {
            log.error("Error while getting current user: {}", error.getMessage(), error);
            throw error;
        }
    }

    public AccountInfoDto updateAccount(String login, String username, LocalDate birthdate) {
        try {
            log.info("Request for account update login: {}, username: {}, birthdate: {}", login, username, birthdate.format(DateTimeFormatter.ISO_DATE));
            AccountInfoDto acc = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/accounts/info/{login}")
                            .queryParam("username", username)
                            .queryParam("birthdate", birthdate)
                            .build(login))
                    .retrieve()
                    .bodyToMono(AccountInfoDto.class)
                    .block();
            log.info("Account info :{}", acc);
            return acc;

        } catch (Exception error) {
            log.error("Error while getting current user: {}", error.getMessage(), error);
            throw error;
        }
    }

}
