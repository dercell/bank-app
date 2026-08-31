package ru.yandex.practicum.cash.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;


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
