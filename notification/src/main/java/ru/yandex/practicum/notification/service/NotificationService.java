package ru.yandex.practicum.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.notification.model.LogEntity;

@Slf4j
@Service
public class NotificationService {

    public void sendNotification(LogEntity logEntity) {
        log.info("Send notification about {}", logEntity);
    }
}
