package ru.yandex.practicum.transfer.integration;

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
import ru.yandex.practicum.transfer.client.AccountClient;
import ru.yandex.practicum.transfer.dto.LogEntity;
import ru.yandex.practicum.transfer.dto.SourceService;
import ru.yandex.practicum.transfer.dto.TransferDto;
import ru.yandex.practicum.transfer.service.TransferService;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("contract-test")
@EmbeddedKafka(topics = {"bank-app-notification"}, partitions = 1)
class NotificationProducerIntegrationTest {

    @Autowired
    private TransferService transferService;

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
    void transfer_PublishesLogEntityToKafka() {
        TransferDto body = TransferDto.builder()
                .fromAcc("lukeAcc").toAcc("hanAcc").sum(BigDecimal.valueOf(500)).build();

        transferService.makeTransfer(body);

        ConsumerRecord<UUID, LogEntity> record =
                KafkaTestUtils.getSingleRecord(consumer, "bank-app-notification", Duration.ofSeconds(10));

        LogEntity received = record.value();
        assertThat(received.getSourceService()).isEqualTo(SourceService.TRANSFER);
        assertThat(received.getMessage()).isEqualTo("Перевод выполнен: 500,00 со счёта lukeAcc на счёт hanAcc");
    }
}

