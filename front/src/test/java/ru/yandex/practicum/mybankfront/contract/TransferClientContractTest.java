package ru.yandex.practicum.mybankfront.contract;


import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.mybankfront.client.TransferClient;
import ru.yandex.practicum.mybankfront.config.ContractTestWebClientConfig;

import ru.yandex.practicum.mybankfront.model.ServiceResultDto;


import static org.junit.jupiter.api.Assertions.*;

@Tag("contract")
@SpringBootTest
@ActiveProfiles("contract-test")
@AutoConfigureStubRunner(
        ids = "ru.yandex.practicum:transfer:+:stubs:8888",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@Import(ContractTestWebClientConfig.class)
class TransferClientContractTest {

    @Autowired
    private TransferClient transferClient;

    @Test
    void successTransfer() {

        ServiceResultDto res = transferClient.transfer("luke", "han", 500);

        System.out.println(res);
        assertEquals("Перевод выполнен: 500 со счёта luke на счёт han", res.getMessage());

    }


}
