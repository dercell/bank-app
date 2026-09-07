package ru.yandex.practicum.cash.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.cash.client.AccountClient;
import ru.yandex.practicum.cash.client.NotificationClient;
import ru.yandex.practicum.cash.dto.CashOpDto;
import ru.yandex.practicum.cash.exceptions.InvalidCashAction;

@Service
public class CashService {

    private final AccountClient accountClient;
    private final NotificationClient notificationClient;

    public CashService(AccountClient accountClient, NotificationClient notificationClient) {
        this.accountClient = accountClient;
        this.notificationClient = notificationClient;
    }

    public void chargeSum(CashOpDto body) {
        String msg;
        switch (body.getAction()) {
            case GET -> msg = "Снято %.2f руб".formatted(body.getSum());
            case PUT -> msg = "Положено %.2f руб".formatted(body.getSum());
            default -> throw new InvalidCashAction("Недопустимая операция");
        }

        accountClient.chargeBalance(body);
        notificationClient.sendNotification(msg);
    }
}
