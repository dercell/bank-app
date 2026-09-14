package ru.yandex.practicum.accounts.repository;

import org.springframework.data.repository.CrudRepository;
import ru.yandex.practicum.accounts.model.entity.UserProfile;

import java.util.List;
import java.util.Optional;

public interface UserProfileRepository extends CrudRepository<UserProfile, Long> {

    Optional<UserProfile> getAccountByLogin(String login);

    List<UserProfile> findAll();

}
