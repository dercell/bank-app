package ru.yandex.practicum.mybankfront.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.mybankfront.client.AccountClient;
import ru.yandex.practicum.mybankfront.model.*;
import ru.yandex.practicum.mybankfront.service.AccountService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Tag("unit")
@Tag("service")
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountClient accountClient;

    @InjectMocks
    private AccountService accountService;

    private static final String TEST_LOGIN = "luke";
    private static final String TEST_USERNAME = "Luke Skywalker";
    private static final LocalDate TEST_BIRTHDATE = LocalDate.of(1990, 1, 15);
    private PageInfoDto testDto;

    @BeforeEach
    void setUp() {
        UserProfileDto upd = UserProfileDto.builder()
                .login("luke")
                .username("Luke Skywalker")
                .birthDate(LocalDate.of(1990, 1, 15))
                .build();

        UserAccountInfoDto uaid = UserAccountInfoDto.builder()
                .login("han").username("Han Solo").accounts(
                        List.of(AccountDto.builder().accountNumber("asd").balance(BigDecimal.valueOf(100)).build())
                )
                .build();
        testDto = new PageInfoDto();
        testDto.setUserProfileDto(upd);
        testDto.setCurAccounts(List.of(AccountDto.builder().accountNumber("qwe").balance(BigDecimal.valueOf(200)).build()));
        testDto.setAccounts(List.of(uaid));
    }

    @Test
    void getAccByLogin_Success() {

        when(accountClient.getAccByLogin(TEST_LOGIN)).thenReturn(testDto);

        PageInfoDto result = accountService.getAccByLogin(TEST_LOGIN);

        assertThat(result).isNotNull();
        assertThat(result.getUserProfileDto()).isNotNull();
        assertThat(result.getUserProfileDto().getLogin()).isEqualTo(TEST_LOGIN);
        assertThat(result.getUserProfileDto().getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(result.getCurAccounts().getFirst().getBalance()).isEqualTo(BigDecimal.valueOf(200));
        assertThat(result.getAccounts()).hasSize(1);

        verify(accountClient).getAccByLogin(TEST_LOGIN);
    }

    @Test
    void updateAccount_Success() {

        testDto.getUserProfileDto().setUsername("Luke Starkiller");

        when(accountClient.updateAccount(TEST_LOGIN, TEST_USERNAME, TEST_BIRTHDATE))
                .thenReturn(testDto);

        PageInfoDto result = accountService.updateAccount(TEST_LOGIN, TEST_USERNAME, TEST_BIRTHDATE);

        assertThat(result).isNotNull();
        assertThat(result.getUserProfileDto().getUsername()).isEqualTo("Luke Starkiller");
        assertThat(result.getUserProfileDto().getLogin()).isEqualTo(TEST_LOGIN);
        assertThat(result.getCurAccounts()).hasSize(1);

        verify(accountClient).updateAccount(TEST_LOGIN, TEST_USERNAME, TEST_BIRTHDATE);
    }

    @Test
    void updateAccount_Error() {
        when(accountClient.updateAccount(TEST_LOGIN, TEST_USERNAME, TEST_BIRTHDATE))
                .thenThrow(new IllegalArgumentException("Неверные данные"));

        assertThatThrownBy(() -> accountService.updateAccount(TEST_LOGIN, TEST_USERNAME, TEST_BIRTHDATE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Неверные данные");

        verify(accountClient).updateAccount(TEST_LOGIN, TEST_USERNAME, TEST_BIRTHDATE);
    }
}