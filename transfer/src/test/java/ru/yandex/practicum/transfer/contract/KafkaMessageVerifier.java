package ru.yandex.practicum.transfer.contract;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jspecify.annotations.Nullable;
import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierReceiver;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.JsonKafkaHeaderMapper;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.support.MessageBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class KafkaMessageVerifier implements MessageVerifierReceiver<Message<?>> {

    private final Map<String, BlockingQueue<Message<?>>> broker = new ConcurrentHashMap<>();

    @Override
    public Message<?> receive(String destination, long timeout, TimeUnit timeUnit,
                              @Nullable YamlContract contract) {
        BlockingQueue<Message<?>> messageQueue = broker.computeIfAbsent(destination,
                k -> new ArrayBlockingQueue<>(1));
        Message<?> message;
        try {
            message = messageQueue.poll(timeout, timeUnit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for message from: " + destination, e);
        }
        if (message != null) {
            log.info("Removed a message from topic [{}]", destination);
        }
        return message;
    }

    @Override
    public Message<?> receive(String destination, YamlContract contract) {
        return receive(destination, 15, TimeUnit.SECONDS, contract);
    }

    @KafkaListener(topics = KafkaContractBase.TEST_TOPIC_NAME,
            groupId = "contract-verifier-group")
    public void listen(ConsumerRecord<?, ?> payload,
                       @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.info("Got a message from topic [{}]", topic);
        Map<String, Object> headers = new HashMap<>();

        new JsonKafkaHeaderMapper().toHeaders(payload.headers(), headers);

        broker.computeIfAbsent(topic, k -> new ArrayBlockingQueue<>(1))
                .add(MessageBuilder.createMessage(payload.value(), new MessageHeaders(headers)));
    }
}

