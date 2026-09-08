package ru.yandex.practicum.accounts.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.accounts.config.TestSecurityConfig;
import ru.yandex.practicum.accounts.controller.AccountController;
import ru.yandex.practicum.accounts.exceptions.AccountNotExists;
import ru.yandex.practicum.accounts.model.CashAction;
import ru.yandex.practicum.accounts.model.dto.*;
import ru.yandex.practicum.accounts.model.entity.UserProfile;
import ru.yandex.practicum.accounts.service.AccountsService;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("unit")
@Tag("controller")
@WebMvcTest(AccountController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountsService accountService;

    private final ObjectMapper om = new ObjectMapper();

    private PageInfoDto testDto;

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
    void getAccountInfo_Success() throws Exception {
        when(accountService.getAccountInfo("luke")).thenReturn(testDto);

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
                .andExpect(jsonPath("$.accounts[0].accounts.length()").value(1))
        ;
    }

    @Test
    void getAccountInfo_Error() throws Exception {

        String login = "unknown";

        when(accountService.getAccountInfo(login))
                .thenThrow(new AccountNotExists("Профиль пользователя unknown отсутствует"));

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
        when(accountService.updateAccount(anyString(), anyString(), any(LocalDate.class)))
                .thenReturn(testDto);

        mockMvc.perform(put("/accounts/info/luke")
                        .param("username", "Luke Skywalker")
                        .param("birthdate", "1990-01-15")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER", "ACCOUNT_WRITE")))
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userProfileDto.login").value("luke"))
                .andExpect(jsonPath("$.userProfileDto.username").value("Luke Skywalker"))
                .andExpect(jsonPath("$.curAccounts[0].accountNumber").value("qwe"))
                .andExpect(jsonPath("$.accounts[0].username").value("Han Solo"))
                .andExpect(jsonPath("$.accounts[0].accounts.length()").value(1));
    }

    @Test
    void updateAccount_Error() throws Exception {
        when(accountService.updateAccount(anyString(), anyString(), any(LocalDate.class)))
                .thenThrow(new IllegalArgumentException("Неверная дата"));

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
        doNothing().when(accountService).chargeBalance(body);

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

        CashOpDto body = CashOpDto.builder().action(CashAction.PUT).accNumber("qwe").sum(BigDecimal.valueOf(-100))
                .build();

        doThrow(new IllegalArgumentException("Сумма не может быть отрицательной"))
                .when(accountService).chargeBalance(body);

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

        TransferDto body = TransferDto.builder().fromAcc("from").toAcc("to").sum(BigDecimal.valueOf(500)).build();
        doNothing().when(accountService).transfer(body);

        mockMvc.perform(put("/accounts/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body))
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER", "ACCOUNT_WRITE")))
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Перевод выполнен: 500 со счёта from на счёт to"));
    }

    @Test
    void transfer_Error() throws Exception {
        TransferDto body = TransferDto.builder().fromAcc("from").toAcc("to").sum(BigDecimal.valueOf(-999999)).build();
        doThrow(new IllegalStateException("Недостаточно средств"))
                .when(accountService).transfer(body);

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