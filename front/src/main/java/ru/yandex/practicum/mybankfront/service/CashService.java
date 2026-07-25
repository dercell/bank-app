package ru.yandex.practicum.mybankfront.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.mybankfront.client.CashClient;
import ru.yandex.practicum.mybankfront.controller.dto.CashAction;

@Slf4j
@Service
public class CashService {

    private final CashClient cashClient;

    public CashService(CashClient cashClient){
        this.cashClient = cashClient;
    }

    public void editCash(String login, CashAction action, int value){

        cashClient.chargeSum(login, action, value);

    }

}
