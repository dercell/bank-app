package ru.yandex.practicum.notification.config;

import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.UUID;

@TestConfiguration
public class ContractTriggerKafkaConfig {

    @Bean
    public ProducerFactory<Object, Object> contractYamlProducerFactory(EmbeddedKafkaBroker embeddedKafkaBroker) {
        UUIDSerializer uuidSerializer = new UUIDSerializer();
        Serializer<Object> keySerializer = (topic, data) -> uuidSerializer.serialize(topic, (UUID) data);
        return new DefaultKafkaProducerFactory<>(KafkaTestUtils.producerProps(embeddedKafkaBroker),
                keySerializer,
                new JacksonJsonSerializer<>());
    }

    @Bean
    public KafkaTemplate<Object, Object> contractYamlKafkaTemplate(ProducerFactory<Object, Object> contractYamlProducerFactory) {
        return new KafkaTemplate<>(contractYamlProducerFactory);
    }

}

