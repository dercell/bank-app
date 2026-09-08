package ru.yandex.practicum.mybankfront.contract;


import org.junit.jupiter.api.Tag;
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
import ru.yandex.practicum.mybankfront.model.client.CashOpDto;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("contract")
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
        CashOpDto body = CashOpDto.builder().action(CashAction.PUT).accNumber("lukeAcc").sum(BigDecimal.valueOf(5000)).build();
        assertDoesNotThrow(() -> cashClient.chargeSum(body));
    }

    @Test
    void failCharge() {
        CashOpDto body = CashOpDto.builder().action(CashAction.PUT).accNumber("hanAcc").sum(BigDecimal.valueOf(-1000)).build();
        WebClientResponseException wcre = assertThrows(WebClientResponseException.class, () -> cashClient.chargeSum(body));
        String errorMsg = wcre.getResponseBodyAs(Map.class).get("message").toString();
        assertEquals("sum: Сумма должна быть больше 0", errorMsg);
    }

}
