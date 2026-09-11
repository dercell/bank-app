package ru.yandex.practicum.accounts.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.accounts.client.NotificationClient;
import ru.yandex.practicum.accounts.exceptions.*;
import ru.yandex.practicum.accounts.model.dto.*;
import ru.yandex.practicum.accounts.model.entity.BankAccount;
import ru.yandex.practicum.accounts.model.entity.UserProfile;
import ru.yandex.practicum.accounts.repository.BankAccountRepository;
import ru.yandex.practicum.accounts.repository.UserProfileRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class AccountsService {

    private final UserProfileRepository userProfileRepository;
    private final BankAccountRepository bankAccountRepository;

    private final NotificationClient notificationClient;

    public UserProfile getAccountByLogin(String login) {
        return userProfileRepository.getAccountByLogin(login).orElseThrow(() -> new AccountNotExists("Аккаунта " + login + " не существует"));
    }

    public PageInfoDto getAccountInfo(String login) {
        PageInfoDto pageInfoDto = new PageInfoDto();

        List<UserAccountInfoDto> otherAccs = new ArrayList<>();

        List<UserProfile> accounts = userProfileRepository.findAll();

        for (UserProfile acc : accounts) {
            List<AccountDto> accountDtos = acc.getAccountList().stream()
                    .map(a -> new AccountDto(a.getAccountNum(), a.getBalance()))
                    .toList();

            if (acc.getLogin().equals(login)) {
                UserProfileDto upd = new UserProfileDto(acc.getLogin(), acc.getUsername(), acc.getBirthDate());
                pageInfoDto.setUserProfileDto(upd);

                pageInfoDto.setCurAccounts(accountDtos);
            }

            UserAccountInfoDto uaid = new UserAccountInfoDto(acc.getLogin(), acc.getUsername(), accountDtos);
            otherAccs.add(uaid);
        }

        if (pageInfoDto.getUserProfileDto() == null) {
            throw new AccountNotExists("Профиль пользователя " + login + " отсутствует");
        }

        pageInfoDto.setAccounts(otherAccs);

        return pageInfoDto;
    }

    public PageInfoDto updateAccount(String login, String name, LocalDate bdate) {
        UserProfile currentUser = getAccountByLogin(login);

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя не может быть пустым");
        }

        if (LocalDate.now().minusYears(18L).isBefore(bdate)) {
            throw new IllegalArgumentException("Возраст должен быть больше 18 лет");
        }

        currentUser.setUsername(name);
        currentUser.setBirthDate(bdate);

        userProfileRepository.save(currentUser);
        notificationClient.sendNotification("Профиль %s обновлен".formatted(login));
        return getAccountInfo(login);

    }

    @Transactional
    public void transfer(TransferDto body) {

        String fromAcc = body.getFromAcc();
        String toAcc = body.getToAcc();
        BigDecimal value = body.getSum();

        BankAccount from = bankAccountRepository.getBankAccountsByAccountNum(fromAcc)
                .orElseThrow(() -> new IllegalStateException("Отсутствует счет отправителя " + toAcc));
        BankAccount to = bankAccountRepository.getBankAccountsByAccountNum(toAcc)
                .orElseThrow(() -> new IllegalStateException("Отсутствует счет получателя " + toAcc));

        if (value.compareTo(BigDecimal.valueOf(0)) < 0) {
            throw new NegativeSum("Сумма не может быть отрицательной");
        }

        if (fromAcc.equals(toAcc)) {
            throw new SelfTransferException("Нельзя переводить на тот же самый счет");
        }

        if (from.getBalance().compareTo(value) < 0) {
            throw new NotEnoughMoneyException("Недостаточно средств на счету");
        }

        from.setBalance(from.getBalance().subtract(value));
        to.setBalance(to.getBalance().add(value));

        bankAccountRepository.saveAll(List.of(from, to));

    }

    @Transactional
    public void chargeBalance(CashOpDto body) {
        BankAccount curAccount = bankAccountRepository.getBankAccountsByAccountNum(body.getAccNumber())
                .orElseThrow(() -> new IllegalStateException("Счет не найден: " + body.getAccNumber()));
        String msg;
        BigDecimal sum = body.getSum();

        switch (body.getAction()) {
            case GET -> {
                if (curAccount.getBalance().compareTo(sum) < 0) {
                    throw new NotEnoughMoneyException("Недостаточно средств на счету");
                }
                curAccount.setBalance(curAccount.getBalance().subtract(sum));
                msg = "Снято %.2f руб".formatted(sum);
            }
            case PUT -> {
                curAccount.setBalance(curAccount.getBalance().add(sum));
                msg = "Положено %.2f руб".formatted(sum);
            }
            default -> throw new InvalidCashAction("Недопустимая операция");
        }

        bankAccountRepository.save(curAccount);
        log.info(msg);
    }

    @Transactional
    public void createProfile(ProfileCreateDto profile) {
        UUID accNum = UUID.randomUUID();

        BankAccount ba = BankAccount.builder()
                .accountNum(accNum.toString())
                .login(profile.getLogin())
                .balance(new BigDecimal(0))
                .build();

        UserProfile up = UserProfile.builder()
                .login(profile.getLogin())
                .username(profile.getUsername())
                .birthDate(profile.getBirthDate())
                .accountList(List.of(ba))
                .build();

        userProfileRepository.save(up);
    }
}
