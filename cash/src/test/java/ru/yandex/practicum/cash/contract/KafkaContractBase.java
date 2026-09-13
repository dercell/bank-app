package ru.yandex.practicum.cash.contract;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.practicum.cash.client.AccountClient;
import ru.yandex.practicum.cash.config.KafkaContractTestConfig;
import ru.yandex.practicum.cash.dto.CashAction;
import ru.yandex.practicum.cash.dto.CashOpDto;
import ru.yandex.practicum.cash.service.CashService;

import java.math.BigDecimal;

@Slf4j
@AutoConfigureMessageVerifier
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("contract-test")
@Import(KafkaContractTestConfig.class)
@EmbeddedKafka(topics = {KafkaContractBase.TEST_TOPIC_NAME}, partitions = 1)
public abstract class KafkaContractBase {

    public static final String TEST_TOPIC_NAME = "bank-app-notification";

    @Autowired
    private CashService cashService;

    @MockitoBean
    private AccountClient accountClient;

    public void triggerWithdrawal() {
        CashOpDto body = CashOpDto.builder()
                .action(CashAction.GET)
                .accNumber("contractTestAcc")
                .sum(BigDecimal.valueOf(50))
                .build();

        cashService.chargeSum(body);
        log.info("Triggered chargeSum({}) for contract test", body);
    }

}

