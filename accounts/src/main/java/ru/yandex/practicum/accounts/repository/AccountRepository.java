package ru.yandex.practicum.accounts.repository;

import org.springframework.data.repository.CrudRepository;
import ru.yandex.practicum.accounts.model.Account;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends CrudRepository<Account, Long> {

    Optional<Account> getAccountByLogin(String login);

    List<Account> findAll();

}
