package ru.yandex.practicum.transfer.unit;


import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.transfer.config.TestSecurityConfig;
import ru.yandex.practicum.transfer.controller.TransferController;
import ru.yandex.practicum.transfer.service.TransferService;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("unit")
@Tag("controller")
@WebMvcTest(TransferController.class)
@Import(TestSecurityConfig.class)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransferService transferService;

    private static final String FROM_LOGIN = "luke";
    private static final String TO_LOGIN = "han";
    private static final int SUM = 1000;


    @Test
    void transfer_Success() throws Exception {
        String expectedResponse = "Перевод выполнен: 1000 со счёта luke на счёт han";
        when(transferService.makeTransfer(FROM_LOGIN, TO_LOGIN, SUM))
                .thenReturn(expectedResponse);

        mockMvc.perform(put("/transfer/submit")
                        .param("from", FROM_LOGIN)
                        .param("to", TO_LOGIN)
                        .param("sum", String.valueOf(SUM))
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER", "TRANSFER_WRITE")))
                        )))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));

        verify(transferService, times(1)).makeTransfer(FROM_LOGIN, TO_LOGIN, SUM);
    }

    @Test
    void transfer_Error() throws Exception {
        mockMvc.perform(put("/transfer/submit")
                        .param("from", FROM_LOGIN)
                        .param("to", TO_LOGIN)
                        .param("sum", "-100")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER", "TRANSFER_WRITE")))
                        )))
                .andExpect(status().isInternalServerError());

        verify(transferService, times(0)).makeTransfer(FROM_LOGIN, TO_LOGIN, -100);
    }

    @Test
    void transfer_Forbidden() throws Exception {
        mockMvc.perform(put("/transfer/submit")
                        .param("from", FROM_LOGIN)
                        .param("to", TO_LOGIN)
                        .param("sum", String.valueOf(SUM)))
                .andExpect(status().isUnauthorized());

        verify(transferService, never()).makeTransfer(anyString(), anyString(), anyInt());
    }
}
