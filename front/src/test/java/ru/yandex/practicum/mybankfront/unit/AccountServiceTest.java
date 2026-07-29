package ru.yandex.practicum.mybankfront.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.mybankfront.client.AccountClient;
import ru.yandex.practicum.mybankfront.model.AccountDto;
import ru.yandex.practicum.mybankfront.model.AccountInfoDto;
import ru.yandex.practicum.mybankfront.model.AccountStripped;
import ru.yandex.practicum.mybankfront.service.AccountService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountClient accountClient;

    @InjectMocks
    private AccountService accountService;

    private static final String TEST_LOGIN = "luke";
    private static final String TEST_USERNAME = "Luke Skywalker";
    private static final LocalDate TEST_BIRTHDATE = LocalDate.of(1990, 1, 15);

    private AccountInfoDto createTestAccountInfoDto() {
        AccountDto accountDto = new AccountDto();
        accountDto.setLogin(TEST_LOGIN);
        accountDto.setUsername(TEST_USERNAME);
        accountDto.setBirthDate(TEST_BIRTHDATE);
        accountDto.setBalance(1000L);

        AccountStripped stripped = new AccountStripped();
        stripped.setLogin("han");
        stripped.setName("Han Solo");

        AccountInfoDto dto = new AccountInfoDto();
        dto.setCurAccount(accountDto);
        dto.setAccounts(List.of(stripped));

        return dto;
    }

    @Test
    void getAccByLogin_Success() {
        AccountInfoDto expectedDto = createTestAccountInfoDto();

        when(accountClient.getAccByLogin(TEST_LOGIN)).thenReturn(expectedDto);

        AccountInfoDto result = accountService.getAccByLogin(TEST_LOGIN);

        assertThat(result).isNotNull();
        assertThat(result.getCurAccount()).isNotNull();
        assertThat(result.getCurAccount().getLogin()).isEqualTo(TEST_LOGIN);
        assertThat(result.getCurAccount().getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(result.getCurAccount().getBalance()).isEqualTo(1000L);
        assertThat(result.getAccounts()).hasSize(1);
        assertThat(result.getAccounts().getFirst().getLogin()).isEqualTo("han");

        verify(accountClient).getAccByLogin(TEST_LOGIN);
    }

    @Test
    void updateAccount_Success() {
        AccountInfoDto expectedDto = createTestAccountInfoDto();
        expectedDto.getCurAccount().setUsername("Luke Starkiller");

        when(accountClient.updateAccount(TEST_LOGIN, TEST_USERNAME, TEST_BIRTHDATE))
                .thenReturn(expectedDto);

        AccountInfoDto result = accountService.updateAccount(TEST_LOGIN, TEST_USERNAME, TEST_BIRTHDATE);

        assertThat(result).isNotNull();
        assertThat(result.getCurAccount().getUsername()).isEqualTo("Luke Starkiller");
        assertThat(result.getCurAccount().getLogin()).isEqualTo(TEST_LOGIN);
        assertThat(result.getAccounts()).hasSize(1);

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