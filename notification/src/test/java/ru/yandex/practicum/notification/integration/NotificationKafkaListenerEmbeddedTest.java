package ru.yandex.practicum.notification.integration;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import ru.yandex.practicum.notification.config.KafkaProducerConfig;
import ru.yandex.practicum.notification.model.LogEntity;
import ru.yandex.practicum.notification.model.SourceService;
import ru.yandex.practicum.notification.service.NotificationKafkaListener;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static ru.yandex.practicum.notification.integration.NotificationKafkaListenerEmbeddedTest.TEST_TOPIC_NAME;

@SpringBootTest
@Import({KafkaProducerConfig.class})
@EmbeddedKafka(topics = {TEST_TOPIC_NAME}, partitions = 1)
class NotificationKafkaListenerEmbeddedTest {

    public static final String TEST_TOPIC_NAME = "bank-app-notification";

    @Autowired
    private KafkaTemplate<UUID, LogEntity> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @MockitoSpyBean
    private NotificationKafkaListener notificationKafkaListener;

    @BeforeEach
    public void setUp() {
        for (MessageListenerContainer messageListenerContainer : kafkaListenerEndpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer,
                    embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }

    @Test
    void shouldReceiveNotification() {
        LogEntity le = new LogEntity();
        le.setSourceService(SourceService.CASH);
        le.setMessage("Снято %.2f руб".formatted(BigDecimal.valueOf(50)));

        kafkaTemplate.send(TEST_TOPIC_NAME, UUID.randomUUID(), le).join();

        verify(notificationKafkaListener, timeout(10_000))
                .handlerNotification(le);

    }
}