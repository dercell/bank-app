package ru.yandex.practicum.mybankfront.unit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.mybankfront.client.CashClient;
import ru.yandex.practicum.mybankfront.model.CashAction;
import ru.yandex.practicum.mybankfront.service.CashService;

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

    private static final String TEST_LOGIN = "luke";
    private static final int TEST_VALUE = 500;

    @Test
    void editCash_Deposit_Success() {
        CashAction action = CashAction.GET;

        cashService.editCash(TEST_LOGIN, action, TEST_VALUE);

        verify(cashClient).chargeSum(TEST_LOGIN, action, TEST_VALUE);
        verifyNoMoreInteractions(cashClient);
    }

    @Test
    void editCash_Withdraw_Success() {
        CashAction action = CashAction.GET;

        cashService.editCash(TEST_LOGIN, action, TEST_VALUE);

        verify(cashClient).chargeSum(TEST_LOGIN, action, TEST_VALUE);
        verifyNoMoreInteractions(cashClient);
    }

    @Test
    void editCash_InsufficientFunds_Error() {
        CashAction action = CashAction.GET;
        int largeValue = 999999;

        doThrow(new RuntimeException("Недостаточно средств"))
                .when(cashClient).chargeSum(TEST_LOGIN, action, largeValue);

        assertThatThrownBy(() -> cashService.editCash(TEST_LOGIN, action, largeValue))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Недостаточно средств");

        verify(cashClient).chargeSum(TEST_LOGIN, action, largeValue);
    }
}
