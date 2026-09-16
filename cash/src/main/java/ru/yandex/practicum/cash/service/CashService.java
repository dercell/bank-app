package ru.yandex.practicum.cash.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.cash.client.AccountClient;
import ru.yandex.practicum.cash.dto.CashOpDto;
import ru.yandex.practicum.cash.exceptions.InvalidCashAction;

@Service
public class CashService {

    private final AccountClient accountClient;
    private final NotificationProducer notificationProducer;

    public CashService(AccountClient accountClient, NotificationProducer notificationProducer) {
        this.accountClient = accountClient;
        this.notificationProducer = notificationProducer;
    }

    public void chargeSum(CashOpDto body) {
        String msg;
        switch (body.getAction()) {
            case GET -> msg = "Снято %.2f руб".formatted(body.getSum());
            case PUT -> msg = "Положено %.2f руб".formatted(body.getSum());
            default -> throw new InvalidCashAction("Недопустимая операция");
        }

        accountClient.chargeBalance(body);
        notificationProducer.sendNotification(msg);
    }
}
