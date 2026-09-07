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
import ru.yandex.practicum.accounts.model.dto.AccountDto;
import ru.yandex.practicum.accounts.model.dto.UserProfileDto;
import ru.yandex.practicum.accounts.model.dto.PageInfoDto;
import ru.yandex.practicum.accounts.model.dto.UserAccountInfoDto;
import ru.yandex.practicum.accounts.service.AccountsService;


import java.math.BigDecimal;
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
        doNothing().when(accountsService).transfer("luke", "han", BigDecimal.valueOf(500));

        PageInfoDto accountDto = getAccountDto("Luke Skywalker", LocalDate.of(1990, 1, 15));
        when(accountsService.getAccountInfo("luke")).thenReturn(accountDto);

        PageInfoDto updatedDto = getAccountDto("Luke Starkiller", LocalDate.of(1970, 1, 15));
        when(accountsService.updateAccount(eq("luke"), anyString(), any(LocalDate.class))).thenReturn(updatedDto);

    }

    private PageInfoDto getAccountDto(String username, LocalDate bdate) {

        UserProfileDto upd = UserProfileDto.builder().login("luke").username(username).birthDate(bdate).build();

        UserAccountInfoDto uaid = UserAccountInfoDto.builder().login("han").username("Han Solo")
                .accounts(List.of(AccountDto.builder().accountNumber("asd").balance(BigDecimal.valueOf(100)).build()))
                .build();
        PageInfoDto testDto = new PageInfoDto();
        testDto.setUserProfileDto(upd);
        testDto.setCurAccounts(List.of(AccountDto.builder().accountNumber("qwe").balance(BigDecimal.valueOf(200)).build()));
        testDto.setAccounts(List.of(uaid));

        return testDto;
    }
}