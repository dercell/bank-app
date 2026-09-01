package ru.yandex.practicum.transfer.unit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.transfer.client.AccountClient;
import ru.yandex.practicum.transfer.client.NotificationClient;
import ru.yandex.practicum.transfer.dto.ServiceResultDto;
import ru.yandex.practicum.transfer.service.TransferService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Tag("unit")
@Tag("service")
@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountClient accountClient;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private TransferService transferService;

    private static final String FROM_LOGIN = "from_user";
    private static final String TO_LOGIN = "to_user";
    private static final int SUM = 500;

    @Test
    void makeTransfer_Success() {
        ServiceResultDto expectedResponse = new ServiceResultDto("Перевод выполнен: 500 со счёта from_user на счёт to_user");

        when(accountClient.transfer(FROM_LOGIN, TO_LOGIN, SUM)).thenReturn(expectedResponse);

        ServiceResultDto result = transferService.makeTransfer(FROM_LOGIN, TO_LOGIN, SUM);

        assertThat(result.getMessage()).isNotNull().isEqualTo(expectedResponse.getMessage());

        verify(accountClient).transfer(FROM_LOGIN, TO_LOGIN, SUM);
        verify(notificationClient).sendNotification("Перевод выполнен: 500 со счёта from_user на счёт to_user");
    }

    @Test
    void makeTransfer_InsufficientFunds_Error() {
        when(accountClient.transfer(FROM_LOGIN, TO_LOGIN, SUM))
                .thenThrow(new RuntimeException("Недостаточно средств на счету"));

        assertThatThrownBy(() -> transferService.makeTransfer(FROM_LOGIN, TO_LOGIN, SUM))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Недостаточно средств на счету");

        verify(accountClient).transfer(FROM_LOGIN, TO_LOGIN, SUM);
        verify(notificationClient, never()).sendNotification(anyString());
    }

    @Test
    void makeTransfer_SelfTransfer_Error() {
        when(accountClient.transfer(FROM_LOGIN, FROM_LOGIN, SUM))
                .thenThrow(new IllegalArgumentException("Нельзя переводить деньги самому себе"));

        assertThatThrownBy(() -> transferService.makeTransfer(FROM_LOGIN, FROM_LOGIN, SUM))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Нельзя переводить деньги самому себе");

        verify(accountClient).transfer(FROM_LOGIN, FROM_LOGIN, SUM);
        verify(notificationClient, never()).sendNotification(anyString());
    }

    @Test
    void makeTransfer_NegativeSum_Error() {
        int negativeSum = -100;

        when(accountClient.transfer(FROM_LOGIN, TO_LOGIN, negativeSum))
                .thenThrow(new IllegalArgumentException("Сумма не может быть отрицательной"));

        assertThatThrownBy(() -> transferService.makeTransfer(FROM_LOGIN, TO_LOGIN, negativeSum))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Сумма не может быть отрицательной");

        verify(accountClient).transfer(FROM_LOGIN, TO_LOGIN, negativeSum);
        verify(notificationClient, never()).sendNotification(anyString());
    }
}