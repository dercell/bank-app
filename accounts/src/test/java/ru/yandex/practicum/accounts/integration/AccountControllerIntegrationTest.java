package ru.yandex.practicum.accounts.integration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.accounts.client.NotificationClient;
import ru.yandex.practicum.accounts.config.TestSecurityConfig;
import ru.yandex.practicum.accounts.model.CashAction;
import ru.yandex.practicum.accounts.model.dto.CashOpDto;
import ru.yandex.practicum.accounts.model.dto.TransferDto;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
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

    private final ObjectMapper om = new ObjectMapper();


    @Test
    void getAccountInfo_Success() throws Exception {

        mockMvc.perform(get("/accounts/info/luke")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER")))
                        ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userProfileDto.login").value("luke"))
                .andExpect(jsonPath("$.userProfileDto.username").value("Luke Skywalker"))
                .andExpect(jsonPath("$.curAccounts[0].accountNumber").value("qwe"))
                .andExpect(jsonPath("$.accounts[0].username").value("Han Solo"))
                .andExpect(jsonPath("$.accounts[0].accounts.length()").value(1));
    }

    @Test
    void getAccountInfo_Error() throws Exception {

        mockMvc.perform(get("/accounts/info/unknown")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER")))
                        ))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Профиль пользователя unknown отсутствует"))
                .andExpect(jsonPath("$.resultCode").value("AccountNotExists"));
    }

    @Test
    void getAccountInfo_Unauthorized() throws Exception {
        mockMvc.perform(get("/accounts/info/luke"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateAccount_Success() throws Exception {

        mockMvc.perform(put("/accounts/info/luke")
                        .param("username", "Luke Starkiller")
                        .param("birthdate", "1990-01-15")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER", "ACCOUNT_WRITE")))
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userProfileDto.login").value("luke"))
                .andExpect(jsonPath("$.userProfileDto.username").value("Luke Starkiller"))
                .andExpect(jsonPath("$.curAccounts[0].accountNumber").value("qwe"))
                .andExpect(jsonPath("$.accounts[0].username").value("Han Solo"))
                .andExpect(jsonPath("$.accounts[0].accounts.length()").value(1));
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
        CashOpDto body = CashOpDto.builder().action(CashAction.GET).accNumber("qwe").sum(BigDecimal.valueOf(1000))
                .build();

        mockMvc.perform(put("/accounts/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body))
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
        mockMvc.perform(put("/charge/qwe")
                        .param("action", "GET")
                        .param("sum", "1000"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void transfer_Success() throws Exception {
        TransferDto body = TransferDto.builder().fromAcc("qwe").toAcc("asd").sum(BigDecimal.valueOf(500)).build();


        mockMvc.perform(put("/accounts/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body))
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER", "ACCOUNT_WRITE")))
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Перевод выполнен: 500 со счёта qwe на счёт asd"));
    }

    @Test
    void transfer_Error() throws Exception {

        mockMvc.perform(put("/accounts/transfer")
                        .param("from", "qwe")
                        .param("to", "asd")
                        .param("sum", "-999999")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER", "ACCOUNT_WRITE")))
                        )))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void transfer_Forbidden() throws Exception {
        mockMvc.perform(put("/transfer")
                        .param("from", "qwe")
                        .param("to", "asd")
                        .param("sum", "500"))
                .andExpect(status().isUnauthorized());
    }
}