package ru.yandex.practicum.mybankfront.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.mybankfront.client.AccountClient;
import ru.yandex.practicum.mybankfront.controller.dto.AccountInfoDto;

@Service
@AllArgsConstructor
public class AccountService {

    private AccountClient accountClient;

    public AccountInfoDto getMyAcc(){
        return accountClient.getMyAccount();
    }

}
