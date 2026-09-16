package ru.yandex.practicum.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.notification.model.LogEntity;

@Slf4j
@Service
public class NotificationKafkaListener {

    @KafkaListener(topics = "${custom.kafka.notification-topic}")
    public void handlerNotification(LogEntity logEntity, Acknowledgment ack) {
        log.info("Send notification about {}", logEntity);
        ack.acknowledge();
    }

}
