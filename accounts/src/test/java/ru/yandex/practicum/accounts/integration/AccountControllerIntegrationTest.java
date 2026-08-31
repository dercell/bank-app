package ru.yandex.practicum.accounts.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.accounts.client.NotificationClient;
import ru.yandex.practicum.accounts.config.TestSecurityConfig;
import ru.yandex.practicum.accounts.model.entity.Account;
import ru.yandex.practicum.accounts.model.dto.AccountDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@Tag("controller")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationClient notificationClient;

    private AccountDto testDto;

    @BeforeEach
    void setUp() {
        Account account = Account.builder()
                .login("luke")
                .username("Luke Skywalker")
                .birthDate(LocalDate.of(1990, 1, 15))
                .balance(5000L)
                .build();

        testDto = new AccountDto();
        testDto.setCurAccount(account);
        testDto.setAccounts(List.of());
    }

    @Test
    void getAccountInfo_Success() throws Exception {

        mockMvc.perform(get("/accounts/info/luke")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER")))
                        ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.curAccount.login").value("luke"))
                .andExpect(jsonPath("$.curAccount.username").value("Luke Skywalker"));
    }

    @Test
    void getAccountInfo_Error() throws Exception {
        Account acc = testDto.getCurAccount();
        acc.setUsername(null);
        acc.setBirthDate(null);


        mockMvc.perform(get("/accounts/info/unknown")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER")))
                        ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.curAccount.login").value("unknown"))
                .andExpect(jsonPath("$.curAccount.username").isEmpty());
    }

    @Test
    void getAccountInfo_Unauthorized() throws Exception {
        mockMvc.perform(get("/accounts/info/luke"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateAccount_Success() throws Exception {

        mockMvc.perform(put("/accounts/info/luke")
                        .param("username", "Luke Skywalker")
                        .param("birthdate", "1990-01-15")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER", "ACCOUNT_WRITE")))
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.curAccount.login").value("luke"));
    }

    @Test
    void updateAccount_Error() throws Exception {

        mockMvc.perform(put("/accounts/info/luke")
                        .param("username", "Luke Skywalker")
                        .param("birthdate", "invalid-date")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER", "ACCOUNT_WRITE")))
                        )))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateAccount_Forbidden() throws Exception {
        mockMvc.perform(put("/accounts/info/luke")
                        .param("username", "Luke Skywalker")
                        .param("birthdate", "1990-01-15"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void chargeBalance_Success() throws Exception {


        mockMvc.perform(put("/accounts/charge/luke")
                        .param("action", "GET")
                        .param("sum", "1000")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER", "ACCOUNT_WRITE")))
                        )))
                .andExpect(status().isNoContent());
    }

    @Test
    void chargeBalance_Error() throws Exception {

        mockMvc.perform(put("/accounts/charge/luke")
                        .param("action", "PUT")
                        .param("sum", "-100")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER", "ACCOUNT_WRITE")))
                        )))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void chargeBalance_Forbidden() throws Exception {
        mockMvc.perform(put("/charge/luke")
                        .param("action", "GET")
                        .param("sum", "1000"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void transfer_Success() throws Exception {

        mockMvc.perform(put("/accounts/transfer")
                        .param("from", "luke")
                        .param("to", "han")
                        .param("sum", "500")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER", "ACCOUNT_WRITE")))
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Перевод выполнен: 500 со счёта luke на счёт han"));
    }

    @Test
    void transfer_Error() throws Exception {

        mockMvc.perform(put("/accounts/transfer")
                        .param("from", "from")
                        .param("to", "to")
                        .param("sum", "-999999")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER", "ACCOUNT_WRITE")))
                        )))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void transfer_Forbidden() throws Exception {
        mockMvc.perform(put("/transfer")
                        .param("from", "from")
                        .param("to", "to")
                        .param("sum", "500"))
                .andExpect(status().isUnauthorized());
    }
}