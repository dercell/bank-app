package ru.yandex.practicum.transfer.contract;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.practicum.transfer.client.AccountClient;
import ru.yandex.practicum.transfer.config.ContractTestWebClientConfig;
import ru.yandex.practicum.transfer.dto.ServiceResultDto;

import static org.junit.jupiter.api.Assertions.assertEquals;


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
    void successTransfer() {

        ServiceResultDto res = accountClient.transfer("luke", "han", 500);

        System.out.println(res);
        assertEquals("Перевод выполнен: 500 со счёта luke на счёт han", res.getMessage());

    }

    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://localhost:8888")
            .build();



}
