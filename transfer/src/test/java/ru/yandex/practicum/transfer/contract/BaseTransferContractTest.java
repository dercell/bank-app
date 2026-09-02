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
import ru.yandex.practicum.transfer.client.NotificationClient;
import ru.yandex.practicum.transfer.config.ContractTestSecurityConfig;
import ru.yandex.practicum.transfer.dto.ServiceResultDto;
import ru.yandex.practicum.transfer.service.TransferService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("contract-test")
@Import(ContractTestSecurityConfig.class)
public abstract class BaseTransferContractTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    private NotificationClient notificationClient;

    @MockitoBean
    private TransferService transferService;


    @BeforeEach
    public void setup() {

        RestAssuredMockMvc.mockMvc(mockMvc);
        doNothing().when(notificationClient).sendNotification(anyString());
        when(transferService.makeTransfer("luke", "han", 500)).thenReturn(new ServiceResultDto("Перевод выполнен: 500 со счёта luke на счёт han"));
    }

}
