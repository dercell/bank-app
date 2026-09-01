package ru.yandex.practicum.notification.unit;


import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.notification.model.LogEntity;
import ru.yandex.practicum.notification.model.SourceService;
import ru.yandex.practicum.notification.service.NotificationService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


@Tag("unit")
@Tag("service")
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void sendNotification_Success() {
        LogEntity logEntity = new LogEntity();
        logEntity.setSourceService(SourceService.ACCOUNTS);
        logEntity.setMessage("Профиль обновлен");

        assertDoesNotThrow(() -> notificationService.sendNotification(logEntity));

    }

    @Test
    void sendNotification_WithNullEntity() {
        assertDoesNotThrow(() -> notificationService.sendNotification(null));
    }
}