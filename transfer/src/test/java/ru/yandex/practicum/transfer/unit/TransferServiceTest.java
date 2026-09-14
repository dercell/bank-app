package ru.yandex.practicum.transfer.unit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.yandex.practicum.transfer.client.AccountClient;
import ru.yandex.practicum.transfer.client.NotificationClient;
import ru.yandex.practicum.transfer.dto.ServiceResultDto;
import ru.yandex.practicum.transfer.dto.TransferDto;
import ru.yandex.practicum.transfer.service.TransferService;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

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

    private final ObjectMapper om = new ObjectMapper();


    private static final TransferDto TEST_BODY = TransferDto.builder().fromAcc("lukeAcc").toAcc("hanAcc").sum(BigDecimal.valueOf(500)).build();

    @Test
    void makeTransfer_Success() {
        ServiceResultDto expectedResponse = new ServiceResultDto("500 со счёта lukeAcc на счёт hanAcc");

        when(accountClient.transfer(TEST_BODY)).thenReturn(expectedResponse);

        ServiceResultDto result = transferService.makeTransfer(TEST_BODY);

        assertThat(result.getMessage()).isNotNull().isEqualTo(expectedResponse.getMessage());

        verify(accountClient).transfer(TEST_BODY);
        verify(notificationClient).sendNotification("Перевод выполнен: 500 со счёта lukeAcc на счёт hanAcc");
    }

    @Test
    void makeTransfer_InsufficientFunds_Error() {
        when(accountClient.transfer(TEST_BODY))
                .thenThrow(new RuntimeException("Недостаточно средств на счету"));

        assertThatThrownBy(() -> transferService.makeTransfer(TEST_BODY))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Недостаточно средств на счету");

        verify(accountClient).transfer(TEST_BODY);
        verify(notificationClient, never()).sendNotification(anyString());
    }

    @Test
    void makeTransfer_SelfTransfer_Error() {
        ServiceResultDto errorResp = new ServiceResultDto("SelfTransferException", "Нельзя переводить на тот же самый счет");
        WebClientResponseException we = WebClientResponseException.create(400, null, null, om.writeValueAsBytes(errorResp), null);

        when(accountClient.transfer(TEST_BODY))
                .thenThrow(we);

        assertThatThrownBy(() -> transferService.makeTransfer(TEST_BODY))
                .isInstanceOf(WebClientResponseException.class);

        verify(accountClient).transfer(TEST_BODY);
        verify(notificationClient, never()).sendNotification(anyString());
    }

    @Test
    void makeTransfer_NegativeSum_Error() {
        TransferDto badBody = TransferDto.builder().fromAcc("lukeAcc").toAcc("hanAcc").sum(BigDecimal.valueOf(-100)).build();
        ServiceResultDto errorResp = new ServiceResultDto("NegativeSum", "Сумма не может быть отрицательной");
        WebClientResponseException we = WebClientResponseException.create(400, null, null, om.writeValueAsBytes(errorResp), null);

        when(accountClient.transfer(badBody))
                .thenThrow(we);

        assertThatThrownBy(() -> transferService.makeTransfer(badBody))
                .isInstanceOf(WebClientResponseException.class);

        verify(accountClient).transfer(any(TransferDto.class));
        verify(notificationClient, never()).sendNotification(anyString());
    }
}