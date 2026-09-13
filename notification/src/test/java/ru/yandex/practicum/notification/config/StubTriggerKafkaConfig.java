package ru.yandex.practicum.notification.config;

import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierReceiver;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierSender;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.messaging.Message;
import ru.yandex.practicum.notification.model.LogEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@TestConfiguration
public class StubTriggerKafkaConfig {

    @Bean
    public ProducerFactory<Object, Object> stubProducerFactory(EmbeddedKafkaBroker embeddedKafkaBroker) {
        Map<String, Object> props = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        UUIDSerializer uuidSerializer = new UUIDSerializer();
        Serializer<Object> keySerializer = (topic, data) -> uuidSerializer.serialize(topic, (UUID) data);
        return new DefaultKafkaProducerFactory<>(props, keySerializer, new JacksonJsonSerializer<>());
    }

    @Bean
    public KafkaTemplate<Object, Object> stubKafkaTemplate(ProducerFactory<Object, Object> stubProducerFactory) {
        return new KafkaTemplate<>(stubProducerFactory);
    }


    @Bean
    public ContractVerifierMessaging<Message<?>> kafkaContractVerifierMessaging(KafkaTemplate<Object, Object> stubKafkaTemplate) {
        MessageVerifierSender<Message<?>> sender = new MessageVerifierSender<>() {
            @Override
            public void send(Message<?> message, String destination, YamlContract contract) {
                stubKafkaTemplate.send(destination, UUID.randomUUID(), message.getPayload());
            }

            @Override
            public <T> void send(T payload, Map<String, Object> headers, String destination, YamlContract contract) {
                stubKafkaTemplate.send(destination, UUID.randomUUID(), payload);
            }
        };
        MessageVerifierReceiver<Message<?>> noOpReceiver = new MessageVerifierReceiver<>() {
            @Override
            public Message<?> receive(String destination, long timeout, TimeUnit timeUnit, YamlContract contract) {
                return null;
            }

            @Override
            public Message<?> receive(String destination, YamlContract contract) {
                return null;
            }
        };
        return new ContractVerifierMessaging<>(sender, noOpReceiver);
    }
}

