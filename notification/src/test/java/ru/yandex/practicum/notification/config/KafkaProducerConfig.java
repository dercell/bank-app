package ru.yandex.practicum.notification.config;

import org.apache.kafka.common.serialization.UUIDSerializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import ru.yandex.practicum.notification.model.LogEntity;

import java.util.UUID;

@TestConfiguration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<UUID, LogEntity> producerFactory(EmbeddedKafkaBroker embeddedKafkaBroker) {

        return new DefaultKafkaProducerFactory<>(KafkaTestUtils.producerProps(embeddedKafkaBroker),
                new UUIDSerializer(),
                new JacksonJsonSerializer<>());
    }

    @Bean
    public KafkaTemplate<UUID, LogEntity> kafkaTemplate(ProducerFactory<UUID, LogEntity> pf) {
        return new KafkaTemplate<>(pf);
    }

}
