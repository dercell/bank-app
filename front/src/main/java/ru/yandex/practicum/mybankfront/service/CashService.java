package ru.yandex.practicum.mybankfront.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.mybankfront.client.CashClient;
import ru.yandex.practicum.mybankfront.model.CashAction;
import ru.yandex.practicum.mybankfront.model.client.CashOpDto;

import java.math.BigDecimal;

@Slf4j
@Service
public class CashService {

    private final CashClient cashClient;

    public CashService(CashClient cashClient) {
        this.cashClient = cashClient;
    }

    public void editCash(String accNum, CashAction action, BigDecimal value) {
        CashOpDto body = CashOpDto.builder()
                .action(action).accNumber(accNum).sum(value)
                .build();
        cashClient.chargeSum(body);

    }

}
