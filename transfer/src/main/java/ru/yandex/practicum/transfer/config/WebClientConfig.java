package ru.yandex.practicum.transfer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;


@Configuration
public class WebClientConfig {

    @Value("${custom.baseUrl.api-gateway}")
    private String gatewayUrl;

    @Bean
    @LoadBalanced
    public Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient prepareWebClient(
            Builder loadBalancedWebClientBuilder,
            OAuth2AuthorizedClientManager clientManager) {

        var oauth2Client = new ServletOAuth2AuthorizedClientExchangeFilterFunction(clientManager);
        oauth2Client.setDefaultClientRegistrationId("keycloak");

        return loadBalancedWebClientBuilder.baseUrl(gatewayUrl)
                .apply(oauth2Client.oauth2Configuration())
                .build();
    }

}
