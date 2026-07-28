package ru.yandex.practicum.accounts.controller;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.accounts.model.AccountDto;
import ru.yandex.practicum.accounts.service.AccountsService;

import java.time.LocalDate;

@RestController
@AllArgsConstructor
@RequestMapping("/accounts")
@Validated
public class AccountController {

    private AccountsService accountService;

    @GetMapping("/info/{login}")
    @PreAuthorize("hasRole('USER')")
    public AccountDto getAccountByLogin(@PathVariable("login") String login) {

        return accountService.getAccountInfo(login);
    }

    @PutMapping("/info/{login}")
    @PreAuthorize("hasRole('USER') && hasAuthority('account.write')")
    public AccountDto updateAccount(@PathVariable("login") String login,
                                    @RequestParam("username") String name,
                                    @RequestParam("birthdate") LocalDate birthdate) {
        return accountService.updateAccount(login, name, birthdate);
    }

    @PutMapping("/charge/{login}")
    @PreAuthorize("hasRole('SERVICE') && hasAuthority('account.write')")
    public ResponseEntity<Void> chargeBalance(@PathVariable("login") String login,
                                              @RequestParam("action") String action,
                                              @RequestParam("sum") @Min(0) Integer sum) {

        accountService.chargeBalance(login, action, sum);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/transfer")
    @PreAuthorize("hasRole('SERVICE') && hasAuthority('account.write')")
    public String transfer(@RequestParam("from") String fromLogin,
                           @RequestParam("to") String toLogin,
                           @RequestParam("sum") @Min(0) int sum) {
        accountService.transfer(fromLogin, toLogin, sum);
        return "Перевод выполнен: "
                + sum
                + " со счёта " + fromLogin
                + " на счёт " + toLogin;
    }


}
