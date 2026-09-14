package ru.yandex.practicum.mybankfront.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.yandex.practicum.mybankfront.client.TransferClient;
import ru.yandex.practicum.mybankfront.model.ServiceResultDto;
import ru.yandex.practicum.mybankfront.model.client.TransferDto;
import ru.yandex.practicum.mybankfront.service.TransferService;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Tag("unit")
@Tag("service")
@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransferClient transferClient;

    @InjectMocks
    private TransferService transferService;

    private static final String FROM_LOGIN = "lukeAcc";
    private static final String TO_LOGIN = "hanAcc";
    private static final BigDecimal SUM = BigDecimal.valueOf(500);
    private static TransferDto TEST_BODY;

    private final ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void setUp() {
        TEST_BODY = TransferDto.builder().fromAcc("lukeAcc").toAcc("hanAcc").sum(BigDecimal.valueOf(500)).build();
    }

    @Test
    void makeTransfer_Success() {
        ServiceResultDto expectedResponse = new ServiceResultDto("Перевод выполнен: 500 со счёта from_user на счёт to_user");

        when(transferClient.transfer(TEST_BODY)).thenReturn(expectedResponse);

        ServiceResultDto result = transferService.makeTransfer(FROM_LOGIN, TO_LOGIN, SUM);

        assertEquals(result.getMessage(), expectedResponse.getMessage());
        verify(transferClient).transfer(TEST_BODY);
    }

    @Test
    void makeTransfer_InsufficientFunds_Error() {
        when(transferClient.transfer(TEST_BODY))
                .thenThrow(new RuntimeException("Недостаточно средств на счету"));

        assertThatThrownBy(() -> transferService.makeTransfer(FROM_LOGIN, TO_LOGIN, SUM))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Недостаточно средств на счету");

        verify(transferClient).transfer(TEST_BODY);
    }

    @Test
    void makeTransfer_SelfTransfer_Error() {

        ServiceResultDto errorResp = new ServiceResultDto("SelfTransferException", "Нельзя переводить на тот же самый счет");
        WebClientResponseException we = WebClientResponseException.create(400, null, null, om.writeValueAsBytes(errorResp), null);
        TEST_BODY.setToAcc(FROM_LOGIN);
        when(transferClient.transfer(TEST_BODY))
                .thenThrow(we);

        assertThrows(WebClientResponseException.class, () -> transferService.makeTransfer(FROM_LOGIN, FROM_LOGIN, SUM));

        verify(transferClient).transfer(TEST_BODY);
    }

    @Test
    void makeTransfer_NegativeSum_Error() {
        BigDecimal negativeSum = BigDecimal.valueOf(-100);

        TEST_BODY.setSum(negativeSum);
        when(transferClient.transfer(TEST_BODY))
                .thenThrow(new IllegalArgumentException("Сумма не может быть отрицательной"));

        assertThatThrownBy(() -> transferService.makeTransfer(FROM_LOGIN, TO_LOGIN, negativeSum))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Сумма не может быть отрицательной");

        verify(transferClient).transfer(TEST_BODY);
    }
}