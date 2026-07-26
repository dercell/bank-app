package ru.yandex.practicum.transfer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.transfer.client.AccountClient;

@Slf4j
@Service
public class TransferService {

    private final AccountClient accountClient;


    public TransferService(AccountClient accountClient) {
        this.accountClient = accountClient;
    }

    public String makeTransfer(String fromLogin, String toLogin, int sum) {
        return accountClient.transfer(fromLogin, toLogin, sum);
    }
}
