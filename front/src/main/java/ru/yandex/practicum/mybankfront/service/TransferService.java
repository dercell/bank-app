package ru.yandex.practicum.mybankfront.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.mybankfront.client.TransferClient;
import ru.yandex.practicum.mybankfront.model.ServiceResultDto;

@Service
public class TransferService {

    private final TransferClient transferClient;

    public TransferService(TransferClient transferClient) {
        this.transferClient = transferClient;
    }

    public ServiceResultDto makeTransfer(String fromLogin, String toLogin, int sum) {
        return transferClient.transfer(fromLogin, toLogin, sum);
    }

}
