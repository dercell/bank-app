package ru.yandex.practicum.transfer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.WebClient;

@TestConfiguration
@Profile("contract-test")
public class ContractTestWebClientConfig {

    @Value("${custom.baseUrl.api-gateway}")
    private String gatewayUrl;

    @Bean
    @Primary
    public WebClient testWebClient() {
        return WebClient.builder()
                .baseUrl(gatewayUrl)
                .build();
    }
}

