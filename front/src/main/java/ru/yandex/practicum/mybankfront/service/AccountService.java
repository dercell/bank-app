package ru.yandex.practicum.mybankfront.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.mybankfront.client.AccountClient;
import ru.yandex.practicum.mybankfront.model.PageInfoDto;
import ru.yandex.practicum.mybankfront.model.ProfileCreateDto;

import java.time.LocalDate;

@Service
@AllArgsConstructor
public class AccountService {

    private AccountClient accountClient;

    public PageInfoDto getAccByLogin(String login) {
        return accountClient.getAccByLogin(login);
    }

    public PageInfoDto updateAccount(String login, String username, LocalDate birthdate) {
        return accountClient.updateAccount(login, username, birthdate);
    }

    public void registerUser(String login, String name, LocalDate birthdate) {
        ProfileCreateDto body = ProfileCreateDto.builder().login(login).username(name).birthDate(birthdate).build();

        accountClient.createAccount(body);
    }
}
