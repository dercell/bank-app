package ru.yandex.practicum.mybankfront.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.yandex.practicum.mybankfront.model.PageInfoDto;
import ru.yandex.practicum.mybankfront.model.ProfileCreateDto;
import ru.yandex.practicum.mybankfront.model.AccountInfoDto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class AccountClient {

    private final WebClient webClient;

    public AccountClient(WebClient webClient) {
        this.webClient = webClient.mutate().baseUrl("http://localhost:8081").build();
    }

    public PageInfoDto getAccByLogin(String login) {
        try {
            log.info("Request for user {}", login);
            PageInfoDto acc = webClient.get()
                    .uri("/accounts/info/{login}", login)
                    .retrieve()
                    .bodyToMono(PageInfoDto.class)
                    .block();
            log.info("Account info :{}", acc);

            return acc;
        } catch (WebClientResponseException e) {
            log.error("WebClientResponseException in AccountClient getAccByLogin: {}", e.getMessage(), e);
            throw e;
        } catch (Exception error) {
            log.error("Error while getting current user: {}", error.getMessage(), error);
            throw error;
        }
    }

    public PageInfoDto updateAccount(String login, String username, LocalDate birthdate) {
        try {
            log.info("Request for account update login: {}, username: {}, birthdate: {}", login, username, birthdate.format(DateTimeFormatter.ISO_DATE));
            PageInfoDto acc = webClient.put()
                    .uri(uriBuilder -> uriBuilder
                            .path("/accounts/info/{login}")
                            .queryParam("username", username)
                            .queryParam("birthdate", birthdate)
                            .build(login))
                    .retrieve()
                    .bodyToMono(PageInfoDto.class)
                    .block();
            log.info("Account info :{}", acc);
            return acc;

        } catch (Exception error) {
            log.error("Error while getting current user: {}", error.getMessage(), error);
            throw error;
        }
    }

    public void createAccount(ProfileCreateDto body) {
        try {
            log.info("Request for creating user {}", body);
            webClient.post()
                    .uri("/accounts/create")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(AccountInfoDto.class)
                    .block();
            log.info("Account {} created", body);

        } catch (WebClientResponseException e) {
            log.error("WebClientResponseException in AccountClient createAccount: {}", e.getMessage(), e);
            throw e;
        } catch (Exception error) {
            log.error("Error while creating current user: {}", error.getMessage(), error);
            throw error;
        }
    }
}
