package ru.yandex.practicum.accounts.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.accounts.exceptions.NotEnoughMoneyException;
import ru.yandex.practicum.accounts.model.Account;
import ru.yandex.practicum.accounts.model.AccountDto;
import ru.yandex.practicum.accounts.model.AccountStripped;
import ru.yandex.practicum.accounts.model.CashAction;
import ru.yandex.practicum.accounts.repository.AccountRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class AccountService {

    private AccountRepository accountRepository;

    public Account getAccountByLogin(String login) {
        return accountRepository.getAccountByLogin(login)
                .orElse(Account.builder().login(login).build());
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
            Account newAcc = Account.builder().login(login).build();
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
        return getAccountInfo(login);

    }

    @Transactional
    public void transfer(String fromLogin, String toLogin, int value) {
        Account from = getAccountByLogin(fromLogin);
        Account to = getAccountByLogin(toLogin);

        from.setBalance(from.getBalance() - value);
        to.setBalance(to.getBalance() + value);

        accountRepository.saveAll(List.of(from, to));
    }

    public String chargeBalance(String login, String action, long sum) {
        Account curAccount = getAccountByLogin(login);
        String msg;
        if (action.equals(CashAction.GET.toString()) && sum < curAccount.getBalance()) {
            throw new NotEnoughMoneyException("Недостаточно средств на счету");
        }

        if (action.equals(CashAction.GET.toString())) {
            curAccount.setBalance(sum - curAccount.getBalance());
            msg = "Снято %d руб".formatted(sum);
        } else {
            curAccount.setBalance(sum + curAccount.getBalance());
            msg = "Положено %d руб".formatted(curAccount.getBalance());
        }
        accountRepository.save(curAccount);
        return msg;
    }


}
