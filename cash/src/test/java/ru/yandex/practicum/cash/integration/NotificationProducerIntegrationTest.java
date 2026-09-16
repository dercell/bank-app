package ru.yandex.practicum.cash.integration;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.UUIDDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.practicum.cash.client.AccountClient;
import ru.yandex.practicum.cash.dto.CashAction;
import ru.yandex.practicum.cash.dto.CashOpDto;
import ru.yandex.practicum.cash.dto.LogEntity;
import ru.yandex.practicum.cash.dto.SourceService;
import ru.yandex.practicum.cash.service.CashService;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("contract-test")
@EmbeddedKafka(topics = {"bank-app-notification"}, partitions = 1)
class NotificationProducerIntegrationTest {

    @Autowired
    private CashService cashService;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @MockitoBean
    private AccountClient accountClient;

    private Consumer<UUID, LogEntity> consumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> props = KafkaTestUtils.consumerProps(embeddedKafkaBroker, "notification-producer-it", true);
        consumer = new DefaultKafkaConsumerFactory<>(
                props, new UUIDDeserializer(), new JacksonJsonDeserializer<>(LogEntity.class))
                .createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, "bank-app-notification");
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    void chargeSum_Withdrawal_PublishesLogEntityToKafka() {
        CashOpDto body = CashOpDto.builder()
                .action(CashAction.GET)
                .accNumber("integrationTestAcc")
                .sum(BigDecimal.valueOf(50))
                .build();

        cashService.chargeSum(body);

        ConsumerRecord<UUID, LogEntity> record =
                KafkaTestUtils.getSingleRecord(consumer, "bank-app-notification", Duration.ofSeconds(10));

        LogEntity received = record.value();
        assertThat(received.getSourceService()).isEqualTo(SourceService.CASH);
        assertThat(received.getMessage()).isEqualTo("Снято 50,00 руб");
    }
}

