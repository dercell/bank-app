package ru.yandex.practicum.mybankfront.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.mybankfront.config.TestSecurityConfig;
import ru.yandex.practicum.mybankfront.controller.MainController;
import ru.yandex.practicum.mybankfront.model.AccountDto;
import ru.yandex.practicum.mybankfront.model.AccountInfoDto;
import ru.yandex.practicum.mybankfront.model.CashAction;
import ru.yandex.practicum.mybankfront.service.AccountService;
import ru.yandex.practicum.mybankfront.service.CashService;
import ru.yandex.practicum.mybankfront.service.TransferService;


import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("unit")
@Tag("controller")
@WebMvcTest(MainController.class)
@Import(TestSecurityConfig.class)
class MainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private CashService cashService;

    @MockitoBean
    private TransferService transferService;

    private AccountInfoDto testAccountInfo;

    @BeforeEach
    void setUp() {
        AccountDto account = AccountDto.builder()
                .login("luke")
                .username("Luke Skywalker")
                .birthDate(LocalDate.of(1990, 1, 15))
                .balance(5000L)
                .build();

        testAccountInfo = AccountInfoDto.builder()
                .curAccount(account)
                .accounts(List.of())
                .build();
    }


    @Test
    //@WithMockUser(username = "luke")
    void getAccount_Success() throws Exception {
        when(accountService.getAccByLogin("luke")).thenReturn(testAccountInfo);

        mockMvc.perform(get("/account")
                                .with(oidcLogin().idToken(token -> token.subject("luke")))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("name", "Luke Skywalker"))
                .andExpect(model().attribute("sum", 5000L));

        verify(accountService, times(1)).getAccByLogin("luke");
    }

    @Test
    void getAccount_WithNoUsername_ShouldReturnMainView() throws Exception {
        AccountDto accountWithoutName = AccountDto.builder()
                .login("luke")
                .birthDate(LocalDate.of(1990, 1, 15))
                .balance(5000L)
                .build();

        AccountInfoDto infoWithoutName = AccountInfoDto.builder()
                .curAccount(accountWithoutName)
                .accounts(List.of())
                .build();

        when(accountService.getAccByLogin("luke")).thenReturn(infoWithoutName);

        mockMvc.perform(get("/account")
                        .with(oidcLogin().idToken(token -> token.subject("luke")))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("name", nullValue()))
                .andExpect(model().attribute("sum", 5000L));

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
                .thenReturn(testAccountInfo);

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
        doNothing().when(cashService).editCash("luke", CashAction.PUT, 1000);
        when(accountService.getAccByLogin("luke")).thenReturn(testAccountInfo);

        mockMvc.perform(post("/cash")
                        .param("value", "1000")
                        .param("action", "PUT")
                        .with(oidcLogin().idToken(token -> token.subject("luke")))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("info", "Положено 1000 руб"));

        verify(cashService, times(1)).editCash("luke", CashAction.PUT, 1000);
        verify(accountService, times(1)).getAccByLogin("luke");
    }

    @Test
    void editCash_WithWithdraw_Success() throws Exception {
        doNothing().when(cashService).editCash("luke", CashAction.GET, 500);
        when(accountService.getAccByLogin("luke")).thenReturn(testAccountInfo);

        mockMvc.perform(post("/cash")
                        .param("value", "500")
                        .param("action", "GET")
                        .with(oidcLogin().idToken(token -> token.subject("luke")))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("info", "Снято 500 руб"));

        verify(cashService, times(1)).editCash("luke", CashAction.GET, 500);
        verify(accountService, times(1)).getAccByLogin("luke");
    }

    @Test
    void editCash_Error() throws Exception {
        String errorMessage = "Недостаточно средств";
        doThrow(new RuntimeException(errorMessage))
                .when(cashService).editCash("luke", CashAction.GET, 999999);
        when(accountService.getAccByLogin("luke")).thenReturn(testAccountInfo);

        mockMvc.perform(post("/cash")
                        .param("value", "999999")
                        .param("action", "GET")
                        .with(oidcLogin().idToken(token -> token.subject("luke")))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("errors", List.of(errorMessage)))
                .andExpect(model().attribute("info", nullValue()));

        verify(cashService, times(1)).editCash("luke", CashAction.GET, 999999);
        verify(accountService, times(1)).getAccByLogin("luke");
    }


    @Test
    void transfer_Success() throws Exception {
        when(transferService.makeTransfer("luke", "han", 1000))
                .thenReturn("Перевод выполнен: 1000 со счёта luke на счёт han");
        when(accountService.getAccByLogin("luke")).thenReturn(testAccountInfo);

        mockMvc.perform(post("/transfer")
                        .param("value", "1000")
                        .param("login", "han")
                        .with(oidcLogin().idToken(token -> token.subject("luke")))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("info", "Перевод выполнен: 1000 со счёта luke на счёт han"))
                .andExpect(model().attribute("name", "Luke Skywalker"));

        verify(transferService, times(1)).makeTransfer("luke", "han", 1000);
        verify(accountService, times(1)).getAccByLogin("luke");
    }

    @Test
    void transfer_Error() throws Exception {
        when(transferService.makeTransfer("luke", "han", 999999))
                .thenThrow(new RuntimeException("Недостаточно средств"));
        when(accountService.getAccByLogin("luke")).thenReturn(testAccountInfo);

        mockMvc.perform(post("/transfer")
                        .param("value", "999999")
                        .param("login", "han")
                        .with(oidcLogin().idToken(token -> token.subject("luke")))
                )
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("errors", List.of("Недостаточно средств")))
                .andExpect(model().attribute("info", nullValue()));

        verify(transferService, times(1)).makeTransfer("luke", "han", 999999);
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