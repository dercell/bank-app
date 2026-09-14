package ru.yandex.practicum.mybankfront.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.yandex.practicum.mybankfront.client.AccountClient;
import ru.yandex.practicum.mybankfront.config.TestSecurityConfig;
import ru.yandex.practicum.mybankfront.controller.MainController;
import ru.yandex.practicum.mybankfront.model.*;
import ru.yandex.practicum.mybankfront.service.AccountService;
import ru.yandex.practicum.mybankfront.service.CashService;
import ru.yandex.practicum.mybankfront.service.TransferService;
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

@Tag("unit")
@Tag("controller")
@WebMvcTest(MainController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class MainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private CashService cashService;

    @MockitoBean
    private TransferService transferService;

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
        when(accountService.getAccByLogin("luke")).thenReturn(testDto);

        mockMvc.perform(get("/account")
                        .with(oidcLogin().idToken(token -> token.subject("luke")))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("name", "Luke Skywalker"))
                .andExpect(model().attribute("user_accounts", hasSize(1)));

        verify(accountService, times(1)).getAccByLogin("luke");
    }

    @Test
    void getAccount_WithNoUsername_ShouldReturnProfileView() throws Exception {

        ServiceResultDto res = new ServiceResultDto("AccountNotExists", "Профиль пользователя luke отсутствует");
        WebClientResponseException we = WebClientResponseException.create(400, null, null, om.writeValueAsBytes(res), null);

        when(accountService.getAccByLogin("luke")).thenThrow(we);

        mockMvc.perform(get("/account")
                        .with(oidcLogin().idToken(token -> token.subject("luke")))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attribute("name", nullValue()))
                .andExpect(model().attribute("birthdate", nullValue()));

        verify(accountService, times(1)).getAccByLogin("luke");
    }

    @Test
    void getAccount_Unauthorized() throws Exception {
        mockMvc.perform(get("/account"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/oauth2/authorization/keycloak"));
    }


    @Test
    void editAccount_Success() throws Exception {
        when(accountService.updateAccount(eq("luke"), anyString(), any(LocalDate.class)))
                .thenReturn(testDto);

        mockMvc.perform(post("/account")
                        .param("name", "Luke Skywalker")
                        .param("birthdate", "1990-01-15")
                        .with(oidcLogin().idToken(token -> token.subject("luke")))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("info", "Пользователь изменен"))
                .andExpect(model().attribute("name", "Luke Skywalker"));

        verify(accountService, times(1)).updateAccount(eq("luke"), anyString(), any(LocalDate.class));
    }

    @Test
    void editAccount_Error() throws Exception {
        when(accountService.updateAccount(eq("luke"), anyString(), any(LocalDate.class)))
                .thenThrow(new RuntimeException("Ошибка обновления"));

        mockMvc.perform(post("/account")
                        .param("name", "Luke Skywalker")
                        .param("birthdate", "1990-01-15")
                        .with(oidcLogin().idToken(token -> token.subject("luke")))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("errors", List.of("Ошибка обновления")));

        verify(accountService, times(1)).updateAccount(eq("luke"), anyString(), any(LocalDate.class));
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
        doNothing().when(cashService).editCash("luke", CashAction.PUT, BigDecimal.valueOf(1000));
        when(accountService.getAccByLogin("luke")).thenReturn(testDto);

        mockMvc.perform(post("/cash")
                        .param("value", "1000")
                        .param("action", "PUT")
                        .param("fromAccountNumber", "luke")
                        .with(oidcLogin().idToken(token -> token.subject("luke")))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("info", "Положено 1000,00 руб"));

        verify(cashService, times(1)).editCash("luke", CashAction.PUT, BigDecimal.valueOf(1000));
        verify(accountService, times(1)).getAccByLogin("luke");
    }

    @Test
    void editCash_WithWithdraw_Success() throws Exception {
        doNothing().when(cashService).editCash("luke", CashAction.GET, BigDecimal.valueOf(500));
        when(accountService.getAccByLogin("luke")).thenReturn(testDto);

        mockMvc.perform(post("/cash")
                        .param("value", "500")
                        .param("action", "GET")
                        .param("fromAccountNumber", "luke")
                        .with(oidcLogin().idToken(token -> token.subject("luke")))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("info", "Снято 500,00 руб"));

        verify(cashService, times(1)).editCash("luke", CashAction.GET, BigDecimal.valueOf(500));
        verify(accountService, times(1)).getAccByLogin("luke");
    }

    @Test
    void editCash_Error() throws Exception {
        String errorMessage = "Недостаточно средств";
        doThrow(new RuntimeException(errorMessage))
                .when(cashService).editCash("luke", CashAction.GET, BigDecimal.valueOf(999999));

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

        verify(cashService, times(1)).editCash("luke", CashAction.GET, BigDecimal.valueOf(999999));
    }


    @Test
    void transfer_Success() throws Exception {
        when(transferService.makeTransfer("luke", "han", BigDecimal.valueOf(1000)))
                .thenReturn(new ServiceResultDto("Перевод выполнен: 1000 со счёта luke на счёт han"));
        when(accountService.getAccByLogin("luke")).thenReturn(testDto);

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

        verify(transferService, times(1)).makeTransfer("luke", "han", BigDecimal.valueOf(1000));
        verify(accountService, times(1)).getAccByLogin("luke");
    }

    @Test
    void transfer_Error() throws Exception {
        when(transferService.makeTransfer("luke", "han", BigDecimal.valueOf(999999)))
                .thenThrow(new RuntimeException("Недостаточно средств"));
        when(accountService.getAccByLogin("luke")).thenReturn(testDto);

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

        verify(transferService, times(1)).makeTransfer("luke", "han", BigDecimal.valueOf(999999));
        verify(accountService, times(0)).getAccByLogin("luke");
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