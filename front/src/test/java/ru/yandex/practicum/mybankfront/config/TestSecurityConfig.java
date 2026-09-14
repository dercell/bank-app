package ru.yandex.practicum.mybankfront.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@EnableWebSecurity
@Profile("test")
public class TestSecurityConfig {

    @Bean
    @Primary
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(keycloakClientRegistration());
    }

    private ClientRegistration keycloakClientRegistration() {
        return ClientRegistration.withRegistrationId("keycloak")
                .clientId("test-client")
                .clientSecret("test-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile")
                .authorizationUri("http://localhost:8080/realms/bank-app/protocol/openid-connect/auth")
                .tokenUri("http://localhost:8080/realms/bank-app/protocol/openid-connect/token")
                .userInfoUri("http://localhost:8080/realms/bank-app/protocol/openid-connect/userinfo")
                .jwkSetUri("http://localhost:8080/realms/bank-app/protocol/openid-connect/certs")
                .userNameAttributeName("preferred_username")
                .clientName("keycloak")
                .build();
    }

    @Bean
    @Primary
    public SecurityFilterChain getSecurityWebFilterChain(HttpSecurity httpSecurity) {
        return httpSecurity
                .authorizeHttpRequests(auth ->
                        auth.anyRequest().authenticated())
                .oauth2Login(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .build();

    }

}