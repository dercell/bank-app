package ru.yandex.practicum.transfer.contract;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.transfer.TransferApplication;
import ru.yandex.practicum.transfer.config.ContractTestSecurityConfig;
import ru.yandex.practicum.transfer.config.ContractTestWebClientConfig;
import ru.yandex.practicum.transfer.dto.ServiceResultDto;
import ru.yandex.practicum.transfer.dto.TransferDto;
import ru.yandex.practicum.transfer.service.NotificationProducer;
import ru.yandex.practicum.transfer.service.TransferService;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = TransferApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("contract-test")
@Import({ContractTestSecurityConfig.class, ContractTestWebClientConfig.class})
public abstract class BaseTransferContractTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    private NotificationProducer notificationProducer;

    @MockitoBean
    private TransferService transferService;


    @BeforeEach
    public void setup() {
        TransferDto body = TransferDto.builder().fromAcc("lukeAcc").toAcc("hanAcc").sum(BigDecimal.valueOf(500)).build();
        RestAssuredMockMvc.mockMvc(mockMvc);
        doNothing().when(notificationProducer).sendNotification(anyString());
        when(transferService.makeTransfer(any(TransferDto.class))).thenReturn(new ServiceResultDto("Перевод выполнен: 500 со счёта lukeAcc на счёт hanAcc"));
    }

}
