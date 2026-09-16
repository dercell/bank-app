package ru.yandex.practicum.transfer.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierSender;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessage;
import org.springframework.cloud.contract.verifier.messaging.internal.ContractVerifierMessaging;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.messaging.Message;
import ru.yandex.practicum.transfer.contract.KafkaMessageVerifier;

import java.util.Map;


@EnableKafka
@TestConfiguration
public class KafkaContractTestConfig {

    @Bean
    public KafkaMessageVerifier kafkaMessageVerifier() {
        return new KafkaMessageVerifier();
    }

    @Bean
    public ContractVerifierMessaging<Message<?>> kafkaContractVerifierMessaging(KafkaMessageVerifier kafkaMessageVerifier) {
        MessageVerifierSender<Message<?>> noOpSender = new MessageVerifierSender<>() {
            @Override
            public void send(Message<?> message, String destination, YamlContract contract) {
            }

            @Override
            public <T> void send(T payload, Map<String, Object> headers, String destination, YamlContract contract) {
            }
        };
        return new ContractVerifierMessaging<>(noOpSender, kafkaMessageVerifier) {
            @Override
            protected ContractVerifierMessage convert(Message<?> receive) {
                if (receive == null) {
                    return null;
                }
                return new ContractVerifierMessage(receive.getPayload(), receive.getHeaders());
            }
        };
    }
}

