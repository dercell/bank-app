package ru.yandex.practicum.mybankfront.controller;


import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.yandex.practicum.mybankfront.model.AccountDto;
import ru.yandex.practicum.mybankfront.model.AccountInfoDto;
import ru.yandex.practicum.mybankfront.model.CashAction;
import ru.yandex.practicum.mybankfront.service.AccountService;
import ru.yandex.practicum.mybankfront.service.CashService;
import ru.yandex.practicum.mybankfront.service.TransferService;

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
        AccountInfoDto acc = accountService.getAccByLogin(login);
        fillModel(model, acc, null, null);

        return "main";
    }

    @PostMapping("/account")
    public String editAccount(
            Model model,
            @RequestParam("name") String name,
            @RequestParam("birthdate") LocalDate birthdate,
            @AuthenticationPrincipal OidcUser oidcUser
    ) {

        String login = oidcUser.getName();
        AccountInfoDto acc = accountService.updateAccount(login, name, birthdate);
        fillModel(model, acc, "Пользователь изменен", null);

        return "main";
    }


    @PostMapping("/cash")
    public String editCash(
            Model model,
            @RequestParam("value") int value,
            @RequestParam("action") CashAction action,
            @AuthenticationPrincipal OidcUser oidcUser
    ) {
        String info = null;
        List<String> error = null;
        String login = oidcUser.getName();
        try {
            cashService.editCash(login, action, value);
            info = action == CashAction.GET ? "Снято %d руб".formatted(value) : "Положено %d руб".formatted(value);
        } catch (WebClientResponseException wcre) {
            error = List.of(wcre.getResponseBodyAsString());
        } catch (Exception ex) {
            error = List.of(ex.getMessage());
        } finally {
            AccountInfoDto acc = accountService.getAccByLogin(login);
            fillModel(model, acc, info, error);
        }

        return "main";
    }

    @PostMapping("/transfer")
    public String transfer(
            Model model,
            @RequestParam("value") int value,
            @RequestParam("login") String toLogin,
            @AuthenticationPrincipal OidcUser oidcUser
    ) {

        String fromLogin = oidcUser.getName();
        String info = transferService.makeTransfer(fromLogin, toLogin, value);
        AccountInfoDto acc = accountService.getAccByLogin(fromLogin);
        fillModel(model, acc, info, null);

        return "main";
    }

    private static void fillModel(Model model, AccountInfoDto dto, String info, List<String> error) {
        String name = Optional.of(dto.getCurAccount()).map(AccountDto::getUsername).orElse(null);
        String birthDate = Optional.of(dto.getCurAccount()).map(AccountDto::getBirthDate)
                .map(bdate -> bdate.format(DateTimeFormatter.ISO_DATE)).orElse(null);

        model.addAttribute("name", name);
        model.addAttribute("birthdate", birthDate);
        model.addAttribute("sum", dto.getCurAccount().getBalance());
        model.addAttribute("accounts", dto.getAccounts());
        model.addAttribute("info", info);
        model.addAttribute("errors", error);
    }


}
