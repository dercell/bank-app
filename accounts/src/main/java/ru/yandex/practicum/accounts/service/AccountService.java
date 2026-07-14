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
        return accountRepository.getAccountByLogin(login).orElseThrow(() -> new IllegalArgumentException("User does not exist!"));
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
        accInfo.setAccounts(otherAccs);
        return accInfo;
    }

    public Account updateAccount(String login, String name, LocalDate bdate) {
        Account currentUser = getAccountByLogin(login);

        currentUser.setUsername(name);
        currentUser.setBirthDate(bdate);

        return accountRepository.save(currentUser);

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
