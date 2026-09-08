package ru.yandex.practicum.transfer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.transfer.client.AccountClient;
import ru.yandex.practicum.transfer.client.NotificationClient;
import ru.yandex.practicum.transfer.dto.ServiceResultDto;
import ru.yandex.practicum.transfer.dto.TransferDto;

@Slf4j
@Service
public class TransferService {

    private final AccountClient accountClient;
    private final NotificationClient notificationClient;


    public TransferService(AccountClient accountClient, NotificationClient notificationClient) {
        this.accountClient = accountClient;
        this.notificationClient = notificationClient;
    }

    public ServiceResultDto makeTransfer(TransferDto body) {
        ServiceResultDto result = accountClient.transfer(body);
        notificationClient.sendNotification("Перевод выполнен: "
                + body.getSum()
                + " со счёта " + body.getFromAcc()
                + " на счёт " + body.getToAcc());
        return result;
    }
}
