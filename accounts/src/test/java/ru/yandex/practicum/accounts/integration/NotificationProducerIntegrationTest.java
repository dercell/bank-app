package ru.yandex.practicum.accounts.integration;

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
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.accounts.model.dto.LogEntity;
import ru.yandex.practicum.accounts.model.dto.SourceService;
import ru.yandex.practicum.accounts.service.AccountsService;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles({"contract-test", "test"})
@EmbeddedKafka(topics = {"bank-app-notification"}, partitions = 1)
@Transactional
class NotificationProducerIntegrationTest {

    @Autowired
    private AccountsService accountsService;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

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
        String login = "luke";
        String username = "Luke Skywalker";
        LocalDate birthdate = LocalDate.of(1990, 1, 15);

        accountsService.updateAccount(login, username, birthdate);

        ConsumerRecord<UUID, LogEntity> record =
                KafkaTestUtils.getSingleRecord(consumer, "bank-app-notification", Duration.ofSeconds(10));

        LogEntity received = record.value();
        assertThat(received.getSourceService()).isEqualTo(SourceService.ACCOUNTS);
        assertThat(received.getMessage()).isEqualTo("Профиль luke обновлен");
    }
}

