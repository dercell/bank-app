package ru.yandex.practicum.mybankfront.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.mybankfront.client.TransferClient;
import ru.yandex.practicum.mybankfront.model.ServiceResultDto;
import ru.yandex.practicum.mybankfront.model.client.TransferDto;

import java.math.BigDecimal;

@Service
public class TransferService {

    private final TransferClient transferClient;

    public TransferService(TransferClient transferClient) {
        this.transferClient = transferClient;
    }

    public ServiceResultDto makeTransfer(String fromAcc, String toAcc, BigDecimal sum) {
        TransferDto body = TransferDto.builder()
                .fromAcc(fromAcc).toAcc(toAcc).sum(sum)
                .build();

        return transferClient.transfer(body);
    }

}
