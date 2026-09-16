package ru.yandex.practicum.transfer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.transfer.client.AccountClient;
import ru.yandex.practicum.transfer.dto.ServiceResultDto;
import ru.yandex.practicum.transfer.dto.TransferDto;

@Slf4j
@Service
public class TransferService {

    private final AccountClient accountClient;
    private final NotificationProducer notificationProducer;


    public TransferService(AccountClient accountClient, NotificationProducer notificationProducer) {
        this.accountClient = accountClient;
        this.notificationProducer = notificationProducer;
    }

    public ServiceResultDto makeTransfer(TransferDto body) {
        ServiceResultDto result = accountClient.transfer(body);

        notificationProducer.sendNotification("Перевод выполнен: %.2f со счёта %s на счёт %s".formatted(body.getSum(), body.getFromAcc(), body.getToAcc()));
        return result;
    }
}
