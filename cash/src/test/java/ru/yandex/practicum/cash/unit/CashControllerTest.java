package ru.yandex.practicum.cash.unit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.cash.config.TestSecurityConfig;
import ru.yandex.practicum.cash.controller.CashController;
import ru.yandex.practicum.cash.service.CashService;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("unit")
@Tag("controller")
@WebMvcTest(CashController.class)
@Import(TestSecurityConfig.class)
class CashControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CashService cashService;

    private static final String LOGIN = "luke";
    private static final String ACTION = "GET";
    private static final int SUM = 1000;

    @Test
    void chargeSum_Success() throws Exception {
        doNothing().when(cashService).chargeSum(LOGIN, ACTION, SUM);

        mockMvc.perform(put("/cash/{login}", LOGIN)
                        .param("action", ACTION)
                        .param("sum", String.valueOf(SUM))
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", List.of("USER", "CASH_WRITE"))
                        )))
                .andExpect(status().isNoContent());

        verify(cashService, times(1)).chargeSum(LOGIN, ACTION, SUM);
    }


    @Test
    void chargeSum_Error() throws Exception {
        doThrow(new IllegalArgumentException("Сумма не может быть отрицательной"))
                .when(cashService).chargeSum(LOGIN, ACTION, -100);

        mockMvc.perform(put("/cash/{login}", LOGIN)
                        .param("action", ACTION)
                        .param("sum", "-100")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER", "CASH_WRITE")))
                        )))
                .andExpect(status().isInternalServerError());
    }


    @Test
    void chargeSum_Forbidden() throws Exception {
        mockMvc.perform(put("/cash/{login}", LOGIN)
                        .param("action", ACTION)
                        .param("sum", String.valueOf(SUM)))
                .andExpect(status().isUnauthorized());

        verify(cashService, never()).chargeSum(anyString(), anyString(), anyInt());
    }
}
