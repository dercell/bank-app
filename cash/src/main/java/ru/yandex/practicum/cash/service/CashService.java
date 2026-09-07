package ru.yandex.practicum.cash.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.cash.client.AccountClient;
import ru.yandex.practicum.cash.client.NotificationClient;
import ru.yandex.practicum.cash.dto.CashAction;
import ru.yandex.practicum.cash.exceptions.InvalidCashAction;

@Service
public class CashService {

    private final AccountClient accountClient;
    private final NotificationClient notificationClient;

    public CashService(AccountClient accountClient, NotificationClient notificationClient) {
        this.accountClient = accountClient;
        this.notificationClient = notificationClient;
    }

    public void chargeSum(String login, CashAction action, Integer sum) {
        String msg;
        switch (action) {
            case GET -> msg = "Снято %d руб".formatted(sum);
            case PUT -> msg = "Положено %d руб".formatted(sum);
            default -> throw new InvalidCashAction("Недопустимая операция");
        }

        accountClient.chargeBalance(login, action, sum);
        notificationClient.sendNotification(msg);
    }
}
