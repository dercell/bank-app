package ru.yandex.practicum.mybankfront.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.yandex.practicum.mybankfront.model.client.CashOpDto;

@Slf4j
@Component
public class CashClient {

    private final WebClient webClient;

    public CashClient(WebClient webClient) {
        this.webClient = webClient.mutate().baseUrl("http://localhost:8082").build();
    }

    public void chargeSum(CashOpDto body) {
        try {
            log.info("Trying to {}", body);
            webClient.put().uri("/cash")
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException e) {
            log.error("WebClientResponseException in CashClient chargeSum: {}", e.getMessage(), e);
            throw e;
        }catch (Exception ex) {
            log.error("Exception in CashClient chargeSum: {}", ex.getMessage(), ex);
            throw ex;
        }

    }
}
