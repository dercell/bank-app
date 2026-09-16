package ru.yandex.practicum.transfer.contract;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.practicum.transfer.TransferApplication;
import ru.yandex.practicum.transfer.client.AccountClient;
import ru.yandex.practicum.transfer.config.KafkaContractTestConfig;
import ru.yandex.practicum.transfer.dto.TransferDto;
import ru.yandex.practicum.transfer.service.TransferService;

import java.math.BigDecimal;

@Slf4j
@AutoConfigureMessageVerifier
@SpringBootTest(classes = TransferApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("contract-test")
@Import(KafkaContractTestConfig.class)
@EmbeddedKafka(topics = {KafkaContractBase.TEST_TOPIC_NAME}, partitions = 1)
public abstract class KafkaContractBase {

    public static final String TEST_TOPIC_NAME = "bank-app-notification";

    @Autowired
    private TransferService transferService;

    @MockitoBean
    private AccountClient accountClient;

    public void triggerTransfer() {
        TransferDto body = TransferDto.builder()
                .fromAcc("lukeAcc").toAcc("hanAcc").sum(BigDecimal.valueOf(500)).build();

        transferService.makeTransfer(body);
        log.info("Triggered chargeSum({}) for contract test", body);
    }

}

