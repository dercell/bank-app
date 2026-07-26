package ru.yandex.practicum.notification.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.notification.model.LogEntity;
import ru.yandex.practicum.notification.service.NotificationService;

@Slf4j
@RestController
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('SERVICE') && hasAuthority('notification.write')")
    public ResponseEntity<Void> sendLog(@RequestBody LogEntity logEntity) {
        notificationService.sendNotification(logEntity);
        return ResponseEntity.noContent().build();
    }
}
