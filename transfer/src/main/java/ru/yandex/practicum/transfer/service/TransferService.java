package ru.yandex.practicum.transfer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.transfer.client.AccountClient;
import ru.yandex.practicum.transfer.client.NotificationClient;
import ru.yandex.practicum.transfer.dto.ServiceResultDto;

@Slf4j
@Service
public class TransferService {

    private final AccountClient accountClient;
    private final NotificationClient notificationClient;


    public TransferService(AccountClient accountClient, NotificationClient notificationClient) {
        this.accountClient = accountClient;
        this.notificationClient = notificationClient;
    }

    public ServiceResultDto makeTransfer(String fromLogin, String toLogin, int sum) {
        ServiceResultDto result = accountClient.transfer(fromLogin, toLogin, sum);
        notificationClient.sendNotification("Перевод выполнен: "
                + sum
                + " со счёта " + fromLogin
                + " на счёт " + toLogin);
        return result;
    }
}
