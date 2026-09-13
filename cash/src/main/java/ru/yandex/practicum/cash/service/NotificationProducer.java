package ru.yandex.practicum.cash.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.cash.dto.LogEntity;
import ru.yandex.practicum.cash.dto.SourceService;

import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class NotificationProducer {

    private KafkaTemplate<UUID, LogEntity> kafkaTemplate;

    @Value("${custom.kafka.notification-topic}")
    private static String TOPIC_NAME;

    public void sendNotification(String message) {

        UUID msgKey = UUID.randomUUID();
        log.info("Send message to kafka from Cash: {}", message);
        LogEntity le = new LogEntity();
        le.setSourceService(SourceService.CASH);
        le.setMessage(message);

        kafkaTemplate.send(TOPIC_NAME, msgKey, le);
        log.info("Message send");
    }

}
