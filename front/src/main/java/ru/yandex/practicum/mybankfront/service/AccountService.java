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

    public static void fillModel(Model model, AccountInfoDto dto) {
        String name = Optional.of(dto.getCurAccount()).map(AccountDto::getUsername).orElse(null);
        String birthDate = Optional.of(dto.getCurAccount()).map(AccountDto::getBirthDate)
                .map(bdate -> bdate.format(DateTimeFormatter.ISO_DATE)).orElse(null);

        model.addAttribute("name", name);
        model.addAttribute("birthdate", birthDate);
        model.addAttribute("sum", dto.getCurAccount().getBalance());
        model.addAttribute("accounts", dto.getAccounts());
    }

}
