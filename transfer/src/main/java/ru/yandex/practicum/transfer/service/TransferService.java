package ru.yandex.practicum.transfer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.transfer.client.AccountClient;
import ru.yandex.practicum.transfer.client.NotificationClient;

@Slf4j
@Service
public class TransferService {

    private final AccountClient accountClient;
    private final NotificationClient notificationClient;


    public TransferService(AccountClient accountClient, NotificationClient notificationClient) {
        this.accountClient = accountClient;
        this.notificationClient = notificationClient;
    }

    public String makeTransfer(String fromLogin, String toLogin, int sum) {
        String result = accountClient.transfer(fromLogin, toLogin, sum);
        notificationClient.sendNotification("Перевод выполнен: "
                + sum
                + " со счёта " + fromLogin
                + " на счёт " + toLogin);
        return result;
    }
}
