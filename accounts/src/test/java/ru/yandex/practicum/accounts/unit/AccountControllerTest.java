package ru.yandex.practicum.accounts.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.accounts.config.TestSecurityConfig;
import ru.yandex.practicum.accounts.controller.AccountController;
import ru.yandex.practicum.accounts.model.Account;
import ru.yandex.practicum.accounts.model.AccountDto;
import ru.yandex.practicum.accounts.service.AccountsService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("unit")
@Tag("controller")
@WebMvcTest(AccountController.class)
@Import(TestSecurityConfig.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountsService accountService;

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
        when(accountService.getAccountInfo("luke")).thenReturn(testDto);

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
        when(accountService.getAccountInfo("unknown"))
                .thenReturn(testDto);

        mockMvc.perform(get("/accounts/info/unknown")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER")))
                        ))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.curAccount.login").value("luke"))
                .andExpect(jsonPath("$.curAccount.username").isEmpty());
    }

    @Test
    void getAccountInfo_Unauthorized() throws Exception {
        mockMvc.perform(get("/accounts/info/luke"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateAccount_Success() throws Exception {
        when(accountService.updateAccount(anyString(), anyString(), any(LocalDate.class)))
                .thenReturn(testDto);

        mockMvc.perform(put("/accounts/info/luke")
                        .param("username", "Luke Skywalker")
                        .param("birthdate", "1990-01-15")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER", "account.write")))
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.curAccount.login").value("luke"));
    }

    @Test
    void updateAccount_Error() throws Exception {
        when(accountService.updateAccount(anyString(), anyString(), any(LocalDate.class)))
                .thenThrow(new IllegalArgumentException("Неверная дата"));

        mockMvc.perform(put("/accounts/info/luke")
                        .param("username", "Luke Skywalker")
                        .param("birthdate", "invalid-date")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER", "account.write")))
                        )))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void updateAccount_Forbidden() throws Exception {
        mockMvc.perform(put("/accounts/info/luke")
                        .param("username", "Luke Skywalker")
                        .param("birthdate", "1990-01-15"))
                .andExpect(status().isForbidden());
    }

    @Test
    void chargeBalance_Success() throws Exception {
        doNothing().when(accountService).chargeBalance("luke", "GET", 1000);

        mockMvc.perform(put("/accounts/charge/luke")
                        .param("action", "GET")
                        .param("sum", "1000")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER", "account.write")))
                        )))
                .andExpect(status().isNoContent());
    }

    @Test
    void chargeBalance_Error() throws Exception {
        doThrow(new IllegalArgumentException("Сумма не может быть отрицательной"))
                .when(accountService).chargeBalance("luke", "PUT", -100);

        mockMvc.perform(put("/accounts/charge/luke")
                        .param("action", "PUT")
                        .param("sum", "-100")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER", "account.write")))
                        )))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void chargeBalance_Forbidden() throws Exception {
        mockMvc.perform(put("/charge/luke")
                        .param("action", "DEPOSIT")
                        .param("sum", "1000"))
                .andExpect(status().isForbidden());
    }

    @Test
    void transfer_Success() throws Exception {
        doNothing().when(accountService).transfer("from", "to", 500);

        mockMvc.perform(put("/accounts/transfer")
                        .param("from", "from")
                        .param("to", "to")
                        .param("sum", "500")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER", "transfer.write")))
                        )))
                .andExpect(status().isOk())
                .andExpect(content().string("Перевод выполнен: 500 со счёта from на счёт to"));
    }

    @Test
    void transfer_Error() throws Exception {
        doThrow(new IllegalStateException("Недостаточно средств"))
                .when(accountService).transfer("from", "to", -999999);

        mockMvc.perform(put("/accounts/transfer")
                        .param("from", "from")
                        .param("to", "to")
                        .param("sum", "-999999")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER", "account.write")))
                        )))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void transfer_Forbidden() throws Exception {
        mockMvc.perform(put("/transfer")
                        .param("from", "from")
                        .param("to", "to")
                        .param("sum", "500"))
                .andExpect(status().isForbidden());
    }
}