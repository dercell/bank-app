package ru.yandex.practicum.accounts.repository;

import org.springframework.data.repository.CrudRepository;
import ru.yandex.practicum.accounts.model.entity.BankAccount;
import ru.yandex.practicum.accounts.model.entity.UserProfile;

import java.util.Optional;

public interface BankAccountRepository extends CrudRepository<BankAccount, Long> {

    Optional<BankAccount> getBankAccountsByAccountNum(String accountNum);

}
