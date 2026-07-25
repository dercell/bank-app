package ru.yandex.practicum.mybankfront.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.yandex.practicum.mybankfront.client.AccountClient;
import ru.yandex.practicum.mybankfront.controller.dto.AccountDto;
import ru.yandex.practicum.mybankfront.controller.dto.AccountInfoDto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

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
