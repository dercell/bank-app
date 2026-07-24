package ru.yandex.practicum.accounts.controller;

import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.accounts.model.AccountDto;
import ru.yandex.practicum.accounts.service.AccountService;

import java.time.LocalDate;

@RestController
@AllArgsConstructor
@RequestMapping("/accounts/info/{login}")
public class AccountController {

    private AccountService accountService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public AccountDto getAccountByLogin(@PathVariable("login") String login) {

        return accountService.getAccountInfo(login);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') && hasAuthority('account.write')")
    public AccountDto updateAccount(@PathVariable("login") String login,
                                    @RequestParam("username") String name,
                                    @RequestParam("birthdate") LocalDate birthdate) {
            return accountService.updateAccount(login, name, birthdate);
    }


}
