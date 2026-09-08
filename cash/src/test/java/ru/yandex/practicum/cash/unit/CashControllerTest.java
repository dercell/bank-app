package ru.yandex.practicum.cash.unit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.cash.config.TestSecurityConfig;
import ru.yandex.practicum.cash.controller.CashController;
import ru.yandex.practicum.cash.dto.CashAction;
import ru.yandex.practicum.cash.dto.CashOpDto;
import ru.yandex.practicum.cash.service.CashService;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@Tag("controller")
@ActiveProfiles("test")
@WebMvcTest(CashController.class)
@Import(TestSecurityConfig.class)
class CashControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CashService cashService;

    private static final ObjectMapper om = new ObjectMapper();
    private static final CashOpDto TEST_BODY = CashOpDto.builder()
            .action(CashAction.GET).accNumber("lukeAcc").sum(BigDecimal.valueOf(1000))
            .build();


    @Test
    void chargeSum_Success() throws Exception {

        doNothing().when(cashService).chargeSum(TEST_BODY);

        mockMvc.perform(put("/cash")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(TEST_BODY))
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", List.of("USER", "CASH_WRITE"))
                        )))
                .andExpect(status().isNoContent());

        verify(cashService).chargeSum(any(CashOpDto.class));
    }


    @Test
    void chargeSum_Error() throws Exception {
        CashOpDto b1 = CashOpDto.builder().action(CashAction.GET).accNumber("lukeAcc").sum(BigDecimal.valueOf(-100)).build();
        doThrow(new IllegalArgumentException("Сумма не может быть отрицательной"))
                .when(cashService).chargeSum(b1);

        mockMvc.perform(put("/cash")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(b1))
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER", "CASH_WRITE")))
                        )))
                .andExpect(status().isInternalServerError());
    }


    @Test
    void chargeSum_Forbidden() throws Exception {
        mockMvc.perform(put("/cash")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(TEST_BODY)))
                .andExpect(status().isUnauthorized());

        verify(cashService, never()).chargeSum(any(CashOpDto.class));
    }
}
