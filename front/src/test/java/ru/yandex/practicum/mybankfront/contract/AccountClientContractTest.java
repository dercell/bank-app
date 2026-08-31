package ru.yandex.practicum.mybankfront.contract;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.mybankfront.client.AccountClient;
import ru.yandex.practicum.mybankfront.config.ContractTestWebClientConfig;
import ru.yandex.practicum.mybankfront.model.AccountInfoDto;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("contract-test")
@AutoConfigureStubRunner(
        ids = "ru.yandex.practicum:accounts:+:stubs:8888",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@Import(ContractTestWebClientConfig.class)
class AccountClientContractTest {

    @Autowired
    private AccountClient accountClient;

    @Test
    void successGetInfo(){

        AccountInfoDto accountInfoDto = accountClient.getAccByLogin("luke");

        assertEquals("luke", accountInfoDto.getCurAccount().getLogin());
        assertEquals("han", accountInfoDto.getAccounts().get(0).getLogin());
    }

    @Test
    void successUpdateInfo(){
        AccountInfoDto accountInfoDto = accountClient.updateAccount("luke", "Luke Starkiller", LocalDate.of(1970, 1, 15));
        assertEquals("Luke Starkiller", accountInfoDto.getCurAccount().getUsername());
        assertEquals("han", accountInfoDto.getAccounts().get(0).getLogin());
    }

}
