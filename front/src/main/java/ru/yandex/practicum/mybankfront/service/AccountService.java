package ru.yandex.practicum.mybankfront.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.mybankfront.client.AccountClient;
import ru.yandex.practicum.mybankfront.model.AccountInfoDto;

import java.time.LocalDate;

@Service
@AllArgsConstructor
public class AccountService {

    private AccountClient accountClient;

    public AccountInfoDto getAccByLogin(String login) {
        return accountClient.getAccByLogin(login);
    }

    public AccountInfoDto updateAccount(String login, String username, LocalDate birthdate) {
        return accountClient.updateAccount(login, username, birthdate);
    }

}
