package ru.yandex.practicum.mybankfront.contract;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.yandex.practicum.mybankfront.client.CashClient;
import ru.yandex.practicum.mybankfront.config.ContractTestWebClientConfig;
import ru.yandex.practicum.mybankfront.model.CashAction;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("contract-test")
@AutoConfigureStubRunner(
        ids = "ru.yandex.practicum:cash:+:stubs:8888",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@Import(ContractTestWebClientConfig.class)
class CashClientContractTest {

    @Autowired
    private CashClient cashClient;

    @Test
    void successCharge() {
        assertDoesNotThrow(() -> cashClient.chargeSum("luke", CashAction.PUT, 5000));
    }

    @Test
    void failCharge() {
        WebClientResponseException wcre = assertThrows(WebClientResponseException.class, () -> cashClient.chargeSum("han", CashAction.PUT, -1000));
        String errorMsg = wcre.getResponseBodyAs(Map.class).get("message").toString();
        assertEquals("chargeSum.sum: must be greater than or equal to 0", errorMsg);
    }

}
