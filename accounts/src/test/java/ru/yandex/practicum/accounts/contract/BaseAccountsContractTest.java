package ru.yandex.practicum.accounts.contract;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.accounts.client.NotificationClient;
import ru.yandex.practicum.accounts.config.ContractTestSecurityConfig;
import ru.yandex.practicum.accounts.model.entity.Account;
import ru.yandex.practicum.accounts.model.dto.AccountDto;
import ru.yandex.practicum.accounts.model.dto.AccountStripped;
import ru.yandex.practicum.accounts.service.AccountsService;


import java.time.LocalDate;
import java.util.List;


import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("contract-test")
@Import(ContractTestSecurityConfig.class)
public abstract class BaseAccountsContractTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected AccountsService accountsService;

    @MockitoBean
    private NotificationClient notificationClient;


    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.mockMvc(mockMvc);
        setupMocks();
    }

    private void setupMocks() {
        doNothing().when(notificationClient).sendNotification(anyString());
        doNothing().when(accountsService).transfer("luke", "han", 500);

        AccountDto accountDto = createAccountDto();
        when(accountsService.getAccountInfo("luke"))
                .thenReturn(accountDto);

        AccountDto updatedDto = createUpdatedAccountDto();
        when(accountsService.updateAccount(eq("luke"), anyString(), any(LocalDate.class)))
                .thenReturn(updatedDto);

    }

    private AccountDto createAccountDto() {
        Account account = Account.builder()
                .login("luke")
                .username("Luke Skywalker")
                .birthDate(LocalDate.of(1990, 1, 15))
                .balance(1000L)
                .build();

        AccountStripped stripped = new AccountStripped();
        stripped.setLogin("han");
        stripped.setUsername("Han Solo");

        AccountDto dto = new AccountDto();
        dto.setCurAccount(account);
        dto.setAccounts(List.of(stripped));

        return dto;
    }

    private AccountDto createUpdatedAccountDto() {
        Account account = Account.builder()
                .login("luke")
                .username("Luke Starkiller")
                .birthDate(LocalDate.of(1970, 1, 15))
                .balance(1000L)
                .build();

        AccountStripped stripped = new AccountStripped();
        stripped.setLogin("han");
        stripped.setUsername("Han Solo");

        AccountDto dto = new AccountDto();
        dto.setCurAccount(account);
        dto.setAccounts(List.of(stripped));

        return dto;
    }
}