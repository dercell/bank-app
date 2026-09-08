package ru.yandex.practicum.cash.unit;


import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.cash.client.AccountClient;
import ru.yandex.practicum.cash.client.NotificationClient;
import ru.yandex.practicum.cash.dto.CashAction;
import ru.yandex.practicum.cash.dto.CashOpDto;
import ru.yandex.practicum.cash.service.CashService;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@Tag("unit")
@Tag("service")
@ExtendWith(MockitoExtension.class)
class CashServiceTest {

    @Mock
    private AccountClient accountClient;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private CashService cashService;

    private static final String TEST_ACC = "lukeAcc";
    private static final BigDecimal TEST_SUM = BigDecimal.valueOf(500);

    @Test
    void chargeSum_Deposit_Success() {
        CashOpDto body = CashOpDto.builder().action(CashAction.PUT).accNumber(TEST_ACC).sum(TEST_SUM).build();

        cashService.chargeSum(body);

        verify(accountClient).chargeBalance(body);
        verify(notificationClient).sendNotification("Положено 500,00 руб");
    }

    @Test
    void chargeSum_Withdraw_Success() {
        CashOpDto body = CashOpDto.builder().action(CashAction.GET).accNumber(TEST_ACC).sum(TEST_SUM).build();
        cashService.chargeSum(body);

        verify(accountClient).chargeBalance(body);
        verify(notificationClient).sendNotification("Снято 500,00 руб");
    }

    @Test
    void chargeSum_InsufficientFunds_Error() {

        CashOpDto body = CashOpDto.builder().action(CashAction.GET).accNumber(TEST_ACC).sum(BigDecimal.valueOf(999999)).build();

        doThrow(new RuntimeException("Недостаточно средств"))
                .when(accountClient).chargeBalance(body);

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> cashService.chargeSum(body));

        verify(accountClient).chargeBalance(body);
        verify(notificationClient, never()).sendNotification(anyString());
    }
}