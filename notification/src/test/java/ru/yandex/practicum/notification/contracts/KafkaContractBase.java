package ru.yandex.practicum.notification.contracts;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import ru.yandex.practicum.notification.config.KafkaProducerConfig;
import ru.yandex.practicum.notification.model.LogEntity;
import ru.yandex.practicum.notification.model.SourceService;

import java.math.BigDecimal;
import java.util.UUID;


@Slf4j
@AutoConfigureMessageVerifier
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(KafkaProducerConfig.class)
@EmbeddedKafka(topics = {KafkaContractBase.TEST_TOPIC_NAME}, partitions = 1)
public abstract class KafkaContractBase {

    public static final String TEST_TOPIC_NAME = "bank-app-notification";

    @Autowired
    private KafkaTemplate<UUID, LogEntity> kafkaTemplate;

    public void triggerLogEntity() {

        LogEntity le = new LogEntity();
        le.setSourceService(SourceService.CASH);
        le.setMessage("Снято %.2f руб".formatted(BigDecimal.valueOf(50)));
        UUID key = UUID.randomUUID();

        kafkaTemplate.send(TEST_TOPIC_NAME, key, le);
        log.info("Send {} with key {} to topic {}", le, key, TEST_TOPIC_NAME);
    }

}
