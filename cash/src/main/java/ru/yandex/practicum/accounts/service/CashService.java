package ru.yandex.practicum.accounts.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.accounts.client.AccountClient;

@Service
public class CashService {

    private final AccountClient accountClient;

    public CashService(AccountClient accountClient) {
        this.accountClient = accountClient;
    }

    public void chargeSum(String login, String action, Integer sum) {
        accountClient.chargeBalance(login, action, sum);
    }
}
