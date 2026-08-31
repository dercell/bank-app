package ru.yandex.practicum.cash.contract;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.yandex.practicum.cash.client.AccountClient;
import ru.yandex.practicum.cash.config.ContractTestWebClientConfig;

import java.util.Map;

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
    void successCharge() {
        assertDoesNotThrow(() -> accountClient.chargeBalance("luke", "PUT", 5000));
    }

    @Test
    void failCharge() {
        WebClientResponseException wcre = assertThrows(WebClientResponseException.class, () -> accountClient.chargeBalance("han", "PUT", -1000));
        String errorMsg = wcre.getResponseBodyAs(Map.class).get("message").toString();
        assertEquals("chargeBalance.sum: должно быть не меньше 0", errorMsg);
    }

}
