package ru.yandex.practicum.mybankfront.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.yandex.practicum.mybankfront.client.AccountClient;
import ru.yandex.practicum.mybankfront.client.CashClient;
import ru.yandex.practicum.mybankfront.client.TransferClient;
import ru.yandex.practicum.mybankfront.config.TestSecurityConfig;

import ru.yandex.practicum.mybankfront.model.*;
import ru.yandex.practicum.mybankfront.model.client.CashOpDto;
import ru.yandex.practicum.mybankfront.model.client.TransferDto;
import tools.jackson.databind.ObjectMapper;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@Tag("controller")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class MainControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountClient accountClient;

    @MockitoBean
    private CashClient cashClient;

    @MockitoBean
    private TransferClient transferClient;

    private PageInfoDto testDto;

    private static final ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void setUp() {
        UserProfileDto upd = UserProfileDto.builder()
                .login("luke")
                .username("Luke Skywalker")
                .birthDate(LocalDate.of(1990, 1, 15))
                .build();

        UserAccountInfoDto uaid = UserAccountInfoDto.builder()
                .login("han").username("Han Solo").accounts(
                        List.of(AccountDto.builder().accountNumber("asd").balance(BigDecimal.valueOf(100)).build())
                )
                .build();
        testDto = new PageInfoDto();
        testDto.setUserProfileDto(upd);
        testDto.setCurAccounts(List.of(AccountDto.builder().accountNumber("qwe").balance(BigDecimal.valueOf(200)).build()));
        testDto.setAccounts(List.of(uaid));
    }


    @Test
    void getAccount_Success() throws Exception {
        when(accountClient.getAccByLogin("luke")).thenReturn(testDto);
        mockMvc.perform(get("/account")
                        .with(oidcLogin()
                                .idToken(token -> {
                                    token.subject("luke");
                                    token.claim("preferred_username", "luke");
                                    token.claim("email", "luke@example.com");
                                })
                                .userInfoToken(token -> {
                                    token.claim("sub", "luke");
                                    token.claim("name", "Luke Skywalker");
                                })
                        )
                )
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("name", "Luke Skywalker"))
                .andExpect(model().attribute("user_accounts", hasSize(1)));

    }

    @Test
    void getAccount_WithNoUsername_ShouldReturnProfileView() throws Exception {
        String login = "luke";
        ServiceResultDto res = new ServiceResultDto("AccountNotExists", "Профиль пользователя " + login + " отсутствует");

        WebClientResponseException we = WebClientResponseException.create(400, null, null, om.writeValueAsBytes(res), null);

        when(accountClient.getAccByLogin(login)).thenThrow(we);

        mockMvc.perform(get("/account")
                        .with(oidcLogin().idToken(token -> token.subject(login)))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attribute("name", nullValue()))
                .andExpect(model().attribute("birthdate", nullValue()));

    }

    @Test
    void getAccount_Unauthorized() throws Exception {
        mockMvc.perform(get("/account"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/oauth2/authorization/keycloak"));
    }


    @Test
    void editAccount_Success() throws Exception {
        when(accountClient.updateAccount("luke", "Luke Skywalker", LocalDate.of(1990, 1, 15)))
                .thenReturn(testDto);
        mockMvc.perform(post("/account")
                        .param("name", "Luke Skywalker")
                        .param("birthdate", "1990-01-15")
                        .with(oidcLogin().idToken(token -> token.subject("luke"))
                                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER"))))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("info", "Пользователь изменен"))
                .andExpect(model().attribute("name", "Luke Skywalker"));

    }

    @Test
    void editAccount_Error() throws Exception {
        when(accountClient.updateAccount("luke", "Luke Skywalker", LocalDate.of(2026, 1, 15)))
                .thenThrow(new WebClientResponseException(400, "Ошибка обновления", null, "Ошибка обновления".getBytes(), null));

        mockMvc.perform(post("/account")
                        .param("name", "Luke Skywalker")
                        .param("birthdate", "2026-01-15")
                        .with(oidcLogin().idToken(token -> token.subject("luke")))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("errors", List.of("Ошибка обновления")));

    }

    @Test
    void editAccount_Unauthorized() throws Exception {
        mockMvc.perform(post("/account")
                        .param("name", "Luke Skywalker")
                        .param("birthdate", "1990-01-15"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/oauth2/authorization/keycloak"));
    }


    @Test
    void editCash_WithDeposit_Success() throws Exception {
        when(accountClient.getAccByLogin("luke")).thenReturn(testDto);

        mockMvc.perform(post("/cash")
                        .param("value", "1000")
                        .param("action", "PUT")
                        .param("fromAccountNumber", "PUT")
                        .with(oidcLogin().idToken(token -> token.subject("luke")))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("info", "Положено 1000,00 руб"));


    }

    @Test
    void editCash_WithWithdraw_Success() throws Exception {
        when(accountClient.getAccByLogin("luke")).thenReturn(testDto);

        mockMvc.perform(post("/cash")
                        .param("value", "500")
                        .param("action", "GET")
                        .param("fromAccountNumber", "luke")
                        .with(oidcLogin().idToken(token -> token.subject("luke")))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("info", "Снято 500,00 руб"));
    }

    @Test
    void editCash_Error() throws Exception {
        String errorMessage = "Недостаточно средств";
        doThrow(new WebClientResponseException(400, "Недостаточно средств", null, "Недостаточно средств".getBytes(), null))
                .when(cashClient).chargeSum(any(CashOpDto.class));


        when(accountClient.getAccByLogin("luke")).thenReturn(testDto);

        mockMvc.perform(post("/cash")
                        .param("value", "999999")
                        .param("action", "GET")
                        .param("fromAccountNumber", "luke")
                        .with(oidcLogin().idToken(token -> token.subject("luke")))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("errors", List.of(errorMessage)))
                .andExpect(model().attribute("info", nullValue()));

    }


    @Test
    void transfer_Success() throws Exception {
        when(accountClient.getAccByLogin("luke")).thenReturn(testDto);
        when(transferClient.transfer(any(TransferDto.class)))
                .thenReturn(new ServiceResultDto("Перевод выполнен: 1000 со счёта luke на счёт han"));

        mockMvc.perform(post("/transfer")
                        .param("value", "1000")
                        .param("fromAccountNumber", "luke")
                        .param("toAccountNumber", "han")
                        .with(oidcLogin().idToken(token -> token.subject("luke")))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("info", "Перевод выполнен: 1000 со счёта luke на счёт han"))
                .andExpect(model().attribute("name", "Luke Skywalker"));


    }

    @Test
    void transfer_Error() throws Exception {
        when(transferClient.transfer(any(TransferDto.class)))
                .thenThrow(new WebClientResponseException(400, "Недостаточно средств", null, "Недостаточно средств".getBytes(), null));


        mockMvc.perform(post("/transfer")
                        .param("value", "999999")
                        .param("fromAccountNumber", "luke")
                        .param("toAccountNumber", "han")
                        .with(oidcLogin().idToken(token -> token.subject("luke")))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("errors", List.of("Недостаточно средств")))
                .andExpect(model().attribute("info", nullValue()));


    }

    @Test
    void transfer_Unauthorized() throws Exception {
        mockMvc.perform(post("/transfer")
                        .param("value", "1000")
                        .param("login", "han"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/oauth2/authorization/keycloak"));
    }
}