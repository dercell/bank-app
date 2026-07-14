package ru.yandex.practicum.accounts.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.accounts.model.Account;
import ru.yandex.practicum.accounts.model.AccountDto;
import ru.yandex.practicum.accounts.service.AccountService;

@RestController
@AllArgsConstructor
public class AccountController {

    private AccountService accountService;

    @GetMapping("/myAccount")
    public AccountDto getMyAccount() {

        return accountService.getAccountInfo("luke");
    }

}
