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
import ru.yandex.practicum.accounts.model.CashAction;
import ru.yandex.practicum.accounts.model.dto.CashOpDto;
import ru.yandex.practicum.accounts.model.dto.TransferDto;
import ru.yandex.practicum.accounts.model.entity.BankAccount;
import ru.yandex.practicum.accounts.model.entity.UserProfile;
import ru.yandex.practicum.accounts.repository.BankAccountRepository;
import ru.yandex.practicum.accounts.repository.UserProfileRepository;
import ru.yandex.practicum.accounts.service.AccountsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@Tag("unit")
@Tag("service")
@ExtendWith(MockitoExtension.class)
class AccountsServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private NotificationClient notificationClient;


    @InjectMocks
    private AccountsService accountsService;

    private UserProfile userProfile;
    private BankAccount bankAccount;
    private static final String TEST_LOGIN = "luke";
    private static final String TEST_ACC_NUM = "abc";
    private static final String TEST_NAME = "Luke Skywalker";
    private static final LocalDate TEST_BIRTHDATE = LocalDate.of(1990, 1, 15);

    @BeforeEach
    void setUp() {
        userProfile = UserProfile.builder()
                .login(TEST_LOGIN)
                .username(TEST_NAME)
                .birthDate(TEST_BIRTHDATE)
                .build();

        bankAccount = BankAccount.builder()
                .id(1L).accountNum(TEST_ACC_NUM).login(TEST_LOGIN).balance(new BigDecimal(100))
                .build();

        userProfile.setAccountList(List.of(bankAccount));

    }

    @Test
    void getAccountByLogin_Success() {
        when(userProfileRepository.getAccountByLogin(TEST_LOGIN)).thenReturn(Optional.of(userProfile));

        UserProfile result = accountsService.getAccountByLogin(TEST_LOGIN);

        assertThat(result).isNotNull();
        assertThat(result.getLogin()).isEqualTo(TEST_LOGIN);
        verify(userProfileRepository).getAccountByLogin(TEST_LOGIN);
    }

    @Test
    void getAccountInfo_Success() {

        BankAccount hanAcc = BankAccount.builder()
                .id(2L).accountNum("qwe").login("han").balance(new BigDecimal(200))
                .build();

        UserProfile anotherAccount = UserProfile.builder()
                .login("han")
                .username("Han Solo")
                .accountList(List.of(hanAcc))
                .build();

        when(userProfileRepository.findAll()).thenReturn(List.of(userProfile, anotherAccount));

        var result = accountsService.getAccountInfo(TEST_LOGIN);

        assertThat(result).isNotNull();
        assertEquals(TEST_LOGIN, result.getUserProfileDto().getLogin());
        assertEquals("luke",result.getAccounts().getFirst().getLogin());
        assertThat(result.getAccounts()).hasSize(2);
        verify(userProfileRepository).findAll();
    }

    @Test
    void updateAccount_Success() {
        String newName = "Luke Starkiller";
        LocalDate newBirthdate = LocalDate.of(1985, 5, 10);

        when(userProfileRepository.getAccountByLogin(TEST_LOGIN)).thenReturn(Optional.of(userProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(userProfile);
        when(userProfileRepository.findAll()).thenReturn(List.of(userProfile));

        var result = accountsService.updateAccount(TEST_LOGIN, newName, newBirthdate);

        assertThat(result).isNotNull();
        assertThat(result.getUserProfileDto().getUsername()).isEqualTo(newName);
        verify(userProfileRepository).save(userProfile);
        verify(notificationClient).sendNotification(anyString());
    }

    @Test
    void updateAccount_EmptyName_Error() {
        when(userProfileRepository.getAccountByLogin(TEST_LOGIN)).thenReturn(Optional.of(userProfile));

        assertThatThrownBy(() -> accountsService.updateAccount(TEST_LOGIN, "", TEST_BIRTHDATE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Имя не может быть пустым");
    }

    @Test
    void updateAccount_UnderAge_Error() {
        LocalDate underageDate = LocalDate.now().minusYears(17);
        when(userProfileRepository.getAccountByLogin(TEST_LOGIN)).thenReturn(Optional.of(userProfile));

        assertThatThrownBy(() -> accountsService.updateAccount(TEST_LOGIN, TEST_NAME, underageDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Возраст должен быть больше 18 лет");
    }

    @Test
    void transfer_Success() {
        String fromAcc = "qwe";
        String toAcc = "asd";
        BankAccount fromAccount = BankAccount.builder()
                .id(1L).accountNum(fromAcc).login(TEST_LOGIN).balance(new BigDecimal(1000))
                .build();
        BankAccount toAccount = BankAccount.builder()
                .id(2L).accountNum(toAcc).login(TEST_LOGIN).balance(new BigDecimal(200))
                .build();

        when(bankAccountRepository.getBankAccountsByAccountNum(fromAcc)).thenReturn(Optional.of(fromAccount));
        when(bankAccountRepository.getBankAccountsByAccountNum(toAcc)).thenReturn(Optional.of(toAccount));

        TransferDto body = TransferDto.builder().fromAcc(fromAcc).toAcc(toAcc).sum(BigDecimal.valueOf(500)).build();
        accountsService.transfer(body);

        assertEquals(0, fromAccount.getBalance().compareTo(BigDecimal.valueOf(500)));
        assertEquals(0, toAccount.getBalance().compareTo(BigDecimal.valueOf(700)));
        verify(bankAccountRepository).saveAll(List.of(fromAccount, toAccount));
    }

    @Test
    void transfer_InsufficientFunds_Error() {
        String fromAcc = "qwe";
        String toAcc = "asd";
        BankAccount fromAccount = BankAccount.builder()
                .id(1L).accountNum(fromAcc).login(TEST_LOGIN).balance(new BigDecimal(100))
                .build();
        BankAccount toAccount = BankAccount.builder()
                .id(2L).accountNum(toAcc).login(TEST_LOGIN).balance(new BigDecimal(100))
                .build();

        when(bankAccountRepository.getBankAccountsByAccountNum(fromAcc)).thenReturn(Optional.of(fromAccount));
        when(bankAccountRepository.getBankAccountsByAccountNum(toAcc)).thenReturn(Optional.of(toAccount));

        TransferDto body = TransferDto.builder().fromAcc(fromAcc).toAcc(toAcc).sum(BigDecimal.valueOf(500)).build();

        assertThatThrownBy(() -> accountsService.transfer(body))
                .isInstanceOf(NotEnoughMoneyException.class)
                .hasMessage("Недостаточно средств на счету");
    }

    @Test
    void chargeBalance_Deposit_Success() {
        when(bankAccountRepository.getBankAccountsByAccountNum(TEST_ACC_NUM)).thenReturn(Optional.of(bankAccount));
        CashOpDto body = CashOpDto.builder().action(CashAction.PUT).accNumber(TEST_ACC_NUM).sum(BigDecimal.valueOf(300)).build();

        accountsService.chargeBalance(body);

        verify(bankAccountRepository).save(bankAccount);
    }

    @Test
    void chargeBalance_Withdraw_Success() {
        when(bankAccountRepository.getBankAccountsByAccountNum(TEST_ACC_NUM)).thenReturn(Optional.of(bankAccount));

        CashOpDto body = CashOpDto.builder().action(CashAction.GET).accNumber(TEST_ACC_NUM).sum(BigDecimal.valueOf(50)).build();

        accountsService.chargeBalance(body);

        verify(bankAccountRepository).save(bankAccount);
    }

    @Test
    void chargeBalance_InsufficientFunds_Error() {
        when(bankAccountRepository.getBankAccountsByAccountNum(TEST_ACC_NUM)).thenReturn(Optional.of(bankAccount));

        CashOpDto body = CashOpDto.builder().action(CashAction.GET).accNumber(TEST_ACC_NUM).sum(BigDecimal.valueOf(2000)).build();

        assertThatThrownBy(() -> accountsService.chargeBalance(body))
                .isInstanceOf(NotEnoughMoneyException.class)
                .hasMessage("Недостаточно средств на счету");
    }
}
