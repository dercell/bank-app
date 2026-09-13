package ru.yandex.practicum.cash.contract;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.cash.config.ContractTestSecurityConfig;
import ru.yandex.practicum.cash.config.ContractTestWebClientConfig;
import ru.yandex.practicum.cash.dto.CashOpDto;
import ru.yandex.practicum.cash.service.CashService;
import ru.yandex.practicum.cash.service.NotificationProducer;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("contract-test")
@Import({ContractTestSecurityConfig.class, ContractTestWebClientConfig.class})
public abstract class BaseCashContractTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    private NotificationProducer notificationProducer;

    @MockitoBean
    private CashService cashService;


    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.mockMvc(mockMvc);
        doNothing().when(notificationProducer).sendNotification(anyString());
        doNothing().when(cashService).chargeSum(any(CashOpDto.class));
    }

}
