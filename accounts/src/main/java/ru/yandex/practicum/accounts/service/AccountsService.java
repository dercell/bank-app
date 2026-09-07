package ru.yandex.practicum.accounts.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.accounts.client.NotificationClient;
import ru.yandex.practicum.accounts.exceptions.AccountNotExists;
import ru.yandex.practicum.accounts.exceptions.InvalidCashAction;
import ru.yandex.practicum.accounts.exceptions.NotEnoughMoneyException;
import ru.yandex.practicum.accounts.exceptions.SelfTransferException;
import ru.yandex.practicum.accounts.model.entity.Account;
import ru.yandex.practicum.accounts.model.dto.AccountDto;
import ru.yandex.practicum.accounts.model.dto.AccountStripped;
import ru.yandex.practicum.accounts.model.CashAction;
import ru.yandex.practicum.accounts.repository.AccountRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class AccountsService {

    private final AccountRepository accountRepository;
    private final NotificationClient notificationClient;

    public Account getAccountByLogin(String login) {
        return accountRepository.getAccountByLogin(login).orElseThrow(() -> new AccountNotExists("Аккаунта " + login + " не существует"));
    }

    public AccountDto getAccountInfo(String login) {
        AccountDto accInfo = new AccountDto();
        List<AccountStripped> otherAccs = new ArrayList<>();
        List<Account> accounts = accountRepository.findAll();
        for (Account acc : accounts) {
            if (acc.getLogin().equals(login)) {
                accInfo.setCurAccount(acc);
            } else {
                otherAccs.add(new AccountStripped(acc.getLogin(), acc.getUsername()));
            }
        }
        if (accInfo.getCurAccount() == null) {
            Account newAcc = Account.builder().login(login).balance(0L).build();
            Account savedAcc = accountRepository.save(newAcc);
            accInfo.setCurAccount(savedAcc);
        }
        accInfo.setAccounts(otherAccs);
        return accInfo;
    }

    public AccountDto updateAccount(String login, String name, LocalDate bdate) {
        Account currentUser = getAccountByLogin(login);

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя не может быть пустым");
        }

        if (LocalDate.now().minusYears(18L).isBefore(bdate)) {
            throw new IllegalArgumentException("Возраст должен быть больше 18 лет");
        }

        currentUser.setUsername(name);
        currentUser.setBirthDate(bdate);

        accountRepository.save(currentUser);
        notificationClient.sendNotification("Профиль %s обновлен".formatted(login));
        return getAccountInfo(login);

    }

    @Transactional
    public void transfer(String fromLogin, String toLogin, int value) {
        Account from = getAccountByLogin(fromLogin);
        Account to = getAccountByLogin(toLogin);

        if (fromLogin.equals(toLogin)) {
            throw new SelfTransferException("Нельзя переводить самому себе");
        }

        if (value > from.getBalance()) {
            throw new NotEnoughMoneyException("Недостаточно средств на счету");
        }

        from.setBalance(from.getBalance() - value);
        to.setBalance(to.getBalance() + value);

        accountRepository.saveAll(List.of(from, to));
    }

    public void chargeBalance(String login, CashAction action, long sum) {
        Account curAccount = getAccountByLogin(login);
        String msg;

        switch (action) {
            case GET -> {
                if (sum > curAccount.getBalance()) {
                    throw new NotEnoughMoneyException("Недостаточно средств на счету");
                }
                curAccount.setBalance(curAccount.getBalance() - sum);
                msg = "Снято %d руб".formatted(sum);
            }
            case PUT -> {
                curAccount.setBalance(sum + curAccount.getBalance());
                msg = "Положено %d руб".formatted(curAccount.getBalance());
            }
            default -> throw new InvalidCashAction("Недопустимая операция");
        }

        accountRepository.save(curAccount);
        log.info(msg);
    }


}
