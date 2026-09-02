package ru.yandex.practicum.mybankfront.unit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.mybankfront.client.TransferClient;
import ru.yandex.practicum.mybankfront.model.ServiceResultDto;
import ru.yandex.practicum.mybankfront.service.TransferService;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@Tag("unit")
@Tag("service")
@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransferClient transferClient;

    @InjectMocks
    private TransferService transferService;

    private static final String FROM_LOGIN = "from_user";
    private static final String TO_LOGIN = "to_user";
    private static final int SUM = 500;

    @Test
    void makeTransfer_Success() {
        ServiceResultDto expectedResponse = new ServiceResultDto("Перевод выполнен: 500 со счёта from_user на счёт to_user");

        when(transferClient.transfer(FROM_LOGIN, TO_LOGIN, SUM)).thenReturn(expectedResponse);

        ServiceResultDto result = transferService.makeTransfer(FROM_LOGIN, TO_LOGIN, SUM);

        assertEquals(result.getMessage(), expectedResponse.getMessage());
        verify(transferClient).transfer(FROM_LOGIN, TO_LOGIN, SUM);
    }

    @Test
    void makeTransfer_InsufficientFunds_Error() {
        when(transferClient.transfer(FROM_LOGIN, TO_LOGIN, SUM))
                .thenThrow(new RuntimeException("Недостаточно средств на счету"));

        assertThatThrownBy(() -> transferService.makeTransfer(FROM_LOGIN, TO_LOGIN, SUM))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Недостаточно средств на счету");

        verify(transferClient).transfer(FROM_LOGIN, TO_LOGIN, SUM);
    }

    @Test
    void makeTransfer_SelfTransfer_Error() {
        when(transferClient.transfer(FROM_LOGIN, FROM_LOGIN, SUM))
                .thenThrow(new IllegalArgumentException("Нельзя переводить деньги самому себе"));

        assertThatThrownBy(() -> transferService.makeTransfer(FROM_LOGIN, FROM_LOGIN, SUM))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Нельзя переводить деньги самому себе");

        verify(transferClient).transfer(FROM_LOGIN, FROM_LOGIN, SUM);
    }

    @Test
    void makeTransfer_NegativeSum_Error() {
        int negativeSum = -100;

        when(transferClient.transfer(FROM_LOGIN, TO_LOGIN, negativeSum))
                .thenThrow(new IllegalArgumentException("Сумма не может быть отрицательной"));

        assertThatThrownBy(() -> transferService.makeTransfer(FROM_LOGIN, TO_LOGIN, negativeSum))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Сумма не может быть отрицательной");

        verify(transferClient).transfer(FROM_LOGIN, TO_LOGIN, negativeSum);
    }
}