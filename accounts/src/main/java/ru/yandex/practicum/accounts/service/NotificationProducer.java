package ru.yandex.practicum.accounts.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.accounts.model.dto.LogEntity;
import ru.yandex.practicum.accounts.model.dto.SourceService;


import java.util.UUID;

@Slf4j
@Service
public class NotificationProducer {

    private KafkaTemplate<UUID, LogEntity> kafkaTemplate;

    @Value("${custom.kafka.notification-topic}")
    private String TOPIC_NAME;

    public NotificationProducer(KafkaTemplate<UUID, LogEntity> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendNotification(String message) {

        UUID msgKey = UUID.randomUUID();
        log.info("Send message to kafka from Accounts: {}", message);
        LogEntity le = new LogEntity();
        le.setSourceService(SourceService.ACCOUNTS);
        le.setMessage(message);

        kafkaTemplate.send(TOPIC_NAME, msgKey, le);
        log.info("Message send");
    }

}
