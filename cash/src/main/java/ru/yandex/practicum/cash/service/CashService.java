package ru.yandex.practicum.cash.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.cash.client.AccountClient;
import ru.yandex.practicum.cash.client.NotificationClient;

@Service
public class CashService {

    private final AccountClient accountClient;
    private final NotificationClient notificationClient;

    public CashService(AccountClient accountClient, NotificationClient notificationClient) {
        this.accountClient = accountClient;
        this.notificationClient = notificationClient;
    }

    public void chargeSum(String login, String action, Integer sum) {
        accountClient.chargeBalance(login, action, sum);
        notificationClient.sendNotification("GET".equals(action) ? "Снято %d руб".formatted(sum) : "Положено %d руб".formatted(sum));
    }
}
