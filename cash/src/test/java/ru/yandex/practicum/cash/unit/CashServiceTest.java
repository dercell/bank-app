package ru.yandex.practicum.cash.unit;


import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.cash.client.AccountClient;
import ru.yandex.practicum.cash.client.NotificationClient;
import ru.yandex.practicum.cash.service.CashService;

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

    private static final String TEST_LOGIN = "luke";
    private static final Integer TEST_SUM = 500;

    @Test
    void chargeSum_Deposit_Success() {

        cashService.chargeSum(TEST_LOGIN, "PUT", TEST_SUM);

        verify(accountClient).chargeBalance(TEST_LOGIN, "PUT", TEST_SUM);
        verify(notificationClient).sendNotification("Положено 500 руб");
    }

    @Test
    void chargeSum_Withdraw_Success() {
        cashService.chargeSum(TEST_LOGIN, "GET", TEST_SUM);

        verify(accountClient).chargeBalance(TEST_LOGIN, "GET", TEST_SUM);
        verify(notificationClient).sendNotification("Снято 500 руб");
    }

    @Test
    void chargeSum_InsufficientFunds_Error() {
        String action = "GET";
        int largeSum = 999999;

        doThrow(new RuntimeException("Недостаточно средств"))
                .when(accountClient).chargeBalance(TEST_LOGIN, action, largeSum);

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> cashService.chargeSum(TEST_LOGIN, action, largeSum));

        verify(accountClient).chargeBalance(TEST_LOGIN, action, largeSum);
        verify(notificationClient, never()).sendNotification(anyString());
    }
}