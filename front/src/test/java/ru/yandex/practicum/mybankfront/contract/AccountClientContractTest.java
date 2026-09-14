package ru.yandex.practicum.mybankfront.contract;


import org.junit.jupiter.api.Tag;
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
import ru.yandex.practicum.mybankfront.model.PageInfoDto;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;


@Tag("contract")
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
    void successGetInfo() {

        PageInfoDto accountInfoDto = accountClient.getAccByLogin("luke");

        assertEquals("luke", accountInfoDto.getUserProfileDto().getLogin());
        assertEquals(1, accountInfoDto.getAccounts().size());
    }

    @Test
    void successUpdateInfo() {
        PageInfoDto accountInfoDto = accountClient.updateAccount("luke", "Luke Starkiller", LocalDate.of(1970, 1, 15));
        assertEquals("Luke Starkiller", accountInfoDto.getUserProfileDto().getUsername());
        assertEquals(1, accountInfoDto.getAccounts().size());
    }

}
