package ru.yandex.practicum.accounts.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.accounts.client.NotificationClient;
import ru.yandex.practicum.accounts.exceptions.NotEnoughMoneyException;
import ru.yandex.practicum.accounts.model.entity.Account;
import ru.yandex.practicum.accounts.repository.AccountRepository;
import ru.yandex.practicum.accounts.service.AccountsService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Tag("unit")
@Tag("service")
@ExtendWith(MockitoExtension.class)
class AccountsServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private AccountsService accountsService;

    private Account testAccount;
    private static final String TEST_LOGIN = "luke";
    private static final String TEST_NAME = "Luke Skywalker";
    private static final LocalDate TEST_BIRTHDATE = LocalDate.of(1990, 1, 15);

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .login(TEST_LOGIN)
                .username(TEST_NAME)
                .birthDate(TEST_BIRTHDATE)
                .balance(1000L)
                .build();
    }

    @Test
    void getAccountByLogin_Success() {
        when(accountRepository.getAccountByLogin(TEST_LOGIN)).thenReturn(Optional.of(testAccount));

        Account result = accountsService.getAccountByLogin(TEST_LOGIN);

        assertThat(result).isNotNull();
        assertThat(result.getLogin()).isEqualTo(TEST_LOGIN);
        verify(accountRepository).getAccountByLogin(TEST_LOGIN);
    }

    @Test
    void getAccountInfo_Success() {
        Account anotherAccount = Account.builder()
                .login("han")
                .username("Han Solo")
                .build();

        when(accountRepository.findAll()).thenReturn(List.of(testAccount, anotherAccount));

        var result = accountsService.getAccountInfo(TEST_LOGIN);

        assertThat(result).isNotNull();
        assertThat(result.getCurAccount()).isEqualTo(testAccount);
        assertThat(result.getAccounts()).hasSize(1);
        verify(accountRepository).findAll();
    }

    @Test
    void updateAccount_Success() {
        String newName = "Luke Starkiller";
        LocalDate newBirthdate = LocalDate.of(1985, 5, 10);

        when(accountRepository.getAccountByLogin(TEST_LOGIN)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(accountRepository.findAll()).thenReturn(List.of(testAccount));

        var result = accountsService.updateAccount(TEST_LOGIN, newName, newBirthdate);

        assertThat(result).isNotNull();
        assertThat(result.getCurAccount().getUsername()).isEqualTo(newName);
        verify(accountRepository).save(testAccount);
        verify(notificationClient).sendNotification(anyString());
    }

    @Test
    void updateAccount_EmptyName_Error() {
        when(accountRepository.getAccountByLogin(TEST_LOGIN)).thenReturn(Optional.of(testAccount));

        assertThatThrownBy(() -> accountsService.updateAccount(TEST_LOGIN, "", TEST_BIRTHDATE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Имя не может быть пустым");
    }

    @Test
    void updateAccount_UnderAge_Error() {
        LocalDate underageDate = LocalDate.now().minusYears(17);
        when(accountRepository.getAccountByLogin(TEST_LOGIN)).thenReturn(Optional.of(testAccount));

        assertThatThrownBy(() -> accountsService.updateAccount(TEST_LOGIN, TEST_NAME, underageDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Возраст должен быть больше 18 лет");
    }

    @Test
    void transfer_Success() {
        Account fromAccount = Account.builder().login("from").balance(1000L).build();
        Account toAccount = Account.builder().login("to").balance(200L).build();

        when(accountRepository.getAccountByLogin("from")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.getAccountByLogin("to")).thenReturn(Optional.of(toAccount));

        accountsService.transfer("from", "to", 500);

        assertThat(fromAccount.getBalance()).isEqualTo(500L);
        assertThat(toAccount.getBalance()).isEqualTo(700L);
        verify(accountRepository).saveAll(List.of(fromAccount, toAccount));
    }

    @Test
    void transfer_InsufficientFunds_Error() {
        Account fromAccount = Account.builder().login("from").balance(100L).build();
        Account toAccount = Account.builder().login("to").balance(200L).build();

        when(accountRepository.getAccountByLogin("from")).thenReturn(Optional.of(fromAccount));
        when(accountRepository.getAccountByLogin("to")).thenReturn(Optional.of(toAccount));

        assertThatThrownBy(() -> accountsService.transfer("from", "to", 500))
                .isInstanceOf(NotEnoughMoneyException.class)
                .hasMessage("Недостаточно средств на счету");
    }

    @Test
    void chargeBalance_Deposit_Success() {
        when(accountRepository.getAccountByLogin(TEST_LOGIN)).thenReturn(Optional.of(testAccount));

        accountsService.chargeBalance(TEST_LOGIN, "PUT", 300L);

        assertThat(testAccount.getBalance()).isEqualTo(1300L);
        verify(accountRepository).save(testAccount);
    }

    @Test
    void chargeBalance_Withdraw_Success() {
        when(accountRepository.getAccountByLogin(TEST_LOGIN)).thenReturn(Optional.of(testAccount));

        accountsService.chargeBalance(TEST_LOGIN, "GET", 300L);

        assertThat(testAccount.getBalance()).isEqualTo(700L);
        verify(accountRepository).save(testAccount);
    }

    @Test
    void chargeBalance_InsufficientFunds_Error() {
        when(accountRepository.getAccountByLogin(TEST_LOGIN)).thenReturn(Optional.of(testAccount));

        assertThatThrownBy(() -> accountsService.chargeBalance(TEST_LOGIN, "GET", 2000L))
                .isInstanceOf(NotEnoughMoneyException.class)
                .hasMessage("Недостаточно средств на счету");
    }
}
