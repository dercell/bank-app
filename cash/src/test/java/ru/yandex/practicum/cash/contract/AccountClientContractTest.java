package ru.yandex.practicum.cash.contract;


import org.junit.jupiter.api.Tag;
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
import ru.yandex.practicum.cash.dto.CashAction;
import ru.yandex.practicum.cash.dto.CashOpDto;

import java.math.BigDecimal;
import java.util.Map;

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
    void successCharge() {
        CashOpDto body = CashOpDto.builder().action(CashAction.PUT).accNumber("lukeAcc").sum(BigDecimal.valueOf(5000)).build();
        assertDoesNotThrow(() -> accountClient.chargeBalance(body));
    }

    @Test
    void failCharge() {

        CashOpDto body = CashOpDto.builder().action(CashAction.PUT).accNumber("hanAcc").sum(BigDecimal.valueOf(-1000)).build();

        WebClientResponseException wcre = assertThrows(WebClientResponseException.class,
                () -> accountClient.chargeBalance(body));
        String errorMsg = wcre.getResponseBodyAs(Map.class).get("message").toString();
        assertEquals("sum: Сумма должна быть больше 0", errorMsg);
    }

}
