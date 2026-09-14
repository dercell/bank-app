package ru.yandex.practicum.mybankfront.unit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.mybankfront.client.CashClient;
import ru.yandex.practicum.mybankfront.model.CashAction;
import ru.yandex.practicum.mybankfront.model.client.CashOpDto;
import ru.yandex.practicum.mybankfront.service.CashService;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@Tag("unit")
@Tag("service")
@ExtendWith(MockitoExtension.class)
class CashServiceTest {

    @Mock
    private CashClient cashClient;

    @InjectMocks
    private CashService cashService;

    private static final String TEST_ACC = "lukeAcc";
    private static final BigDecimal TEST_VALUE = BigDecimal.valueOf(500);

    @Test
    void editCash_Deposit_Success() {
        CashAction action = CashAction.GET;

        cashService.editCash(TEST_ACC, action, TEST_VALUE);

        verify(cashClient).chargeSum(any(CashOpDto.class));
        verifyNoMoreInteractions(cashClient);
    }

    @Test
    void editCash_Withdraw_Success() {
        CashAction action = CashAction.GET;

        cashService.editCash(TEST_ACC, action, TEST_VALUE);

        verify(cashClient).chargeSum(any(CashOpDto.class));
        verifyNoMoreInteractions(cashClient);
    }

    @Test
    void editCash_InsufficientFunds_Error() {
        CashAction action = CashAction.GET;
        BigDecimal largeValue = BigDecimal.valueOf(999999);

        doThrow(new RuntimeException("Недостаточно средств"))
                .when(cashClient).chargeSum(any(CashOpDto.class));

        assertThatThrownBy(() -> cashService.editCash(TEST_ACC, action, largeValue))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Недостаточно средств");

        verify(cashClient).chargeSum(any(CashOpDto.class));
    }
}
