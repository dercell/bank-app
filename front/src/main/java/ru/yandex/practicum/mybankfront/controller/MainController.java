package ru.yandex.practicum.mybankfront.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.yandex.practicum.mybankfront.controller.dto.AccountDto;
import ru.yandex.practicum.mybankfront.controller.dto.AccountInfoDto;
import ru.yandex.practicum.mybankfront.controller.dto.CashAction;
import ru.yandex.practicum.mybankfront.controller.stub.AccountStub;
import ru.yandex.practicum.mybankfront.service.AccountService;
import ru.yandex.practicum.mybankfront.service.CashService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;


/**
 * Контроллер main.html.
 * <p>
 * Используемая модель для main.html:
 * model.addAttribute("name", name);
 * model.addAttribute("birthdate", birthdate.format(DateTimeFormatter.ISO_DATE));
 * model.addAttribute("sum", sum);
 * model.addAttribute("accounts", accounts);
 * model.addAttribute("errors", errors);
 * model.addAttribute("info", info);
 * <p>
 * Поля модели:
 * name - Фамилия Имя текущего пользователя, String (обязательное)
 * birthdate - дата рождения текущего пользователя, String в формате 'YYYY-MM-DD' (обязательное)
 * sum - сумма на счету текущего пользователя, Integer (обязательное)
 * accounts - список аккаунтов, которым можно перевести деньги, List<AccountDto> (обязательное)
 * errors - список ошибок после выполнения действий, List<String> (не обязательное)
 * info - строка успешности после выполнения действия, String (не обязательное)
 * <p>
 * С примерами использования можно ознакомиться в тестовом классе заглушке AccountStub
 */
@Controller
public class MainController {
    // TODO: Удалить заглушку, так как используется только для ознакомительных целей
    @Autowired
    private AccountStub accountStub;

    @Autowired
    private AccountService accountService;

    @Autowired
    private CashService cashService;

    /**
     * GET /.
     * Редирект на GET /account
     */
    @GetMapping
    public String index() {
        return "redirect:/account";
    }

    /**
     * GET /account.
     * Что нужно сделать:
     * 1. Сходить в сервис accounts через Gateway API для получения данных аккаунта по REST
     * 2. Заполнить модель main.html полученными из ответа данными
     * 3. Текущего пользователя можно получить из контекста Security
     */
    @GetMapping("/account")
    public String getAccount(Model model,
                             @AuthenticationPrincipal OidcUser oidcUser) {

        String login = oidcUser.getName();
        AccountInfoDto acc = accountService.getAccByLogin(login);
        fillModel(model, acc, null, null);

        return "main";
    }

    /**
     * POST /account.
     * Что нужно сделать:
     * 1. Сходить в сервис accounts через Gateway API для изменения данных текущего пользователя по REST
     * 2. Заполнить модель main.html полученными из ответа данными
     * 3. Текущего пользователя можно получить из контекста Security
     * <p>
     * Изменяемые данные:
     * 1. name - Фамилия Имя
     * 2. birthdate - дата рождения в формате YYYY-DD-MM
     */
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

    /**
     * POST /cash.
     * Что нужно сделать:
     * 1. Сходить в сервис cash через Gateway API для снятия/пополнения счета текущего аккаунта по REST
     * 2. Заполнить модель main.html полученными из ответа данными
     * 3. Текущего пользователя можно получить из контекста Security
     * <p>
     * Параметры:
     * 1. value - сумма списания
     * 2. action - GET (снять), PUT (пополнить)
     */
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

    /**
     * POST /transfer.
     * Что нужно сделать:
     * 1. Сходить в сервис accounts через Gateway API для перевода со счета текущего аккаунта на счет другого аккаунта по REST
     * 2. Заполнить модель main.html полученными из ответа данными
     * 3. Текущего пользователя можно получить из контекста Security
     * <p>
     * Параметры:
     * 1. value - сумма списания
     * 2. login - логин пользователя получателя
     */
    @PostMapping("/transfer")
    public String transfer(
            Model model,
            @RequestParam("value") int value,
            @RequestParam("login") String login
    ) {
        // TODO: Заменить на то, что описано в комментарии к методу
        accountStub.transfer(model, value, login);

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
