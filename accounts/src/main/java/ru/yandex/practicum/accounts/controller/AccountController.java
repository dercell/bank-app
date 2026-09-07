package ru.yandex.practicum.accounts.controller;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.accounts.model.dto.CashOpDto;
import ru.yandex.practicum.accounts.model.dto.PageInfoDto;
import ru.yandex.practicum.accounts.model.dto.ProfileCreateDto;
import ru.yandex.practicum.accounts.model.dto.ServiceResultDto;
import ru.yandex.practicum.accounts.service.AccountsService;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@AllArgsConstructor
@RequestMapping("/accounts")
@Validated
public class AccountController {

    private AccountsService accountService;

    @GetMapping("/info/{login}")
    @PreAuthorize("hasRole('USER')")
    public PageInfoDto getAccountByLogin(@PathVariable("login") String login) {

        return accountService.getAccountInfo(login);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('USER') && hasAuthority('account.write')")
    public ResponseEntity<Void> updateAccount(@RequestBody ProfileCreateDto profile) {
        accountService.createProfile(profile);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/info/{login}")
    @PreAuthorize("hasRole('USER') && hasAuthority('account.write')")
    public PageInfoDto updateAccount(@PathVariable("login") String login,
                                     @RequestParam("username") String name,
                                     @RequestParam("birthdate") LocalDate birthdate) {
        return accountService.updateAccount(login, name, birthdate);
    }

    @PutMapping("/charge")
    @PreAuthorize("hasRole('SERVICE') && hasAuthority('account.write')")
    public ResponseEntity<Void> chargeBalance(@RequestBody CashOpDto body) {

        accountService.chargeBalance(body);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/transfer")
    @PreAuthorize("hasRole('SERVICE') && hasAuthority('account.write')")
    public ServiceResultDto transfer(@RequestParam("from") String fromLogin,
                                     @RequestParam("to") String toLogin,
                                     @RequestParam("sum") @Positive BigDecimal sum) {
        accountService.transfer(fromLogin, toLogin, sum);
        return new ServiceResultDto("Перевод выполнен: "
                + sum
                + " со счёта " + fromLogin
                + " на счёт " + toLogin);
    }


}
