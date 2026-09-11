package ru.yandex.practicum.mybankfront.controller;


import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.mybankfront.model.*;
import ru.yandex.practicum.mybankfront.service.AccountService;
import ru.yandex.practicum.mybankfront.service.CashService;
import ru.yandex.practicum.mybankfront.service.TransferService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
public class MainController {


    private final AccountService accountService;
    private final CashService cashService;
    private final TransferService transferService;

    public MainController(AccountService accountService, CashService cashService, TransferService transferService) {
        this.accountService = accountService;
        this.cashService = cashService;
        this.transferService = transferService;
    }


    @GetMapping
    public String index() {
        return "redirect:/account";
    }

    @GetMapping("/account")
    public String getAccount(Model model,
                             @AuthenticationPrincipal OidcUser oidcUser) {

        String login = oidcUser.getName();
        PageInfoDto acc = accountService.getAccByLogin(login);
        fillModel(model, acc, null, null);

        return "main";
    }

    @PostMapping("/account/register")
    public String getAccount(@RequestParam("username") String name,
                             @RequestParam("birthdate") LocalDate birthdate,
                             @AuthenticationPrincipal OidcUser oidcUser) {

        String login = oidcUser.getName();
        accountService.registerUser(login, name, birthdate);

        return "redirect:/account";
    }

    @PostMapping("/account")
    public String editAccount(
            Model model,
            @RequestParam("name") String name,
            @RequestParam("birthdate") LocalDate birthdate,
            @AuthenticationPrincipal OidcUser oidcUser
    ) {

        String login = oidcUser.getName();
        PageInfoDto acc = accountService.updateAccount(login, name, birthdate);
        fillModel(model, acc, "Пользователь изменен", null);

        return "main";
    }


    @PostMapping("/cash")
    public String editCash(
            Model model,
            @RequestParam("value") BigDecimal value,
            @RequestParam("action") CashAction action,
            @RequestParam("fromAccountNumber") String accountNumber,
            @AuthenticationPrincipal OidcUser oidcUser
    ) {
        String login = oidcUser.getName();

        cashService.editCash(accountNumber, action, value);
        String info = action == CashAction.GET ? "Снято %.2f руб".formatted(value) : "Положено %.2f руб".formatted(value);
        PageInfoDto acc = accountService.getAccByLogin(login);
        fillModel(model, acc, info, null);

        return "main";
    }

    @PostMapping("/transfer")
    public String transfer(
            Model model,
            @RequestParam("value") BigDecimal value,
            @RequestParam("fromAccountNumber") String fromAcc,
            @RequestParam("toAccountNumber") String toAcc,
            @AuthenticationPrincipal OidcUser oidcUser
    ) {
        String fromLogin = oidcUser.getName();
        ServiceResultDto info = transferService.makeTransfer(fromAcc, toAcc, value);
        PageInfoDto acc = accountService.getAccByLogin(fromLogin);
        fillModel(model, acc, info.getMessage(), null);

        return "main";
    }

    private static void fillModel(Model model, PageInfoDto dto, String info, List<String> error) {
        String name = Optional.ofNullable(dto.getUserProfileDto()).map(UserProfileDto::getUsername).orElse(null);

        String birthDate = Optional.ofNullable(dto.getUserProfileDto()).map(UserProfileDto::getBirthDate)
                .map(bdate -> bdate.format(DateTimeFormatter.ISO_DATE)).orElse(null);

        model.addAttribute("name", name);
        model.addAttribute("birthdate", birthDate);
        model.addAttribute("user_accounts", dto.getCurAccounts());
        model.addAttribute("recipients", dto.getAccounts());
        model.addAttribute("info", info);
        model.addAttribute("errors", error);
    }


}
