package ru.yandex.practicum.transfer.unit;


import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.transfer.config.TestSecurityConfig;
import ru.yandex.practicum.transfer.controller.TransferController;
import ru.yandex.practicum.transfer.dto.ServiceResultDto;
import ru.yandex.practicum.transfer.dto.TransferDto;
import ru.yandex.practicum.transfer.service.TransferService;
import wiremock.com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


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

    private static final ObjectMapper om = new ObjectMapper();
    private static final TransferDto TEST_BODY = TransferDto.builder()
            .fromAcc("lukeAcc").toAcc("hanAcc").sum(BigDecimal.valueOf(1000))
            .build();


    @Test
    void transfer_Success() throws Exception {
        ServiceResultDto expectedResponse = new ServiceResultDto("Перевод выполнен: 1000 со счёта luke на счёт han");
        when(transferService.makeTransfer(any(TransferDto.class)))
                .thenReturn(expectedResponse);

        mockMvc.perform(put("/transfer/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(TEST_BODY))
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER", "TRANSFER_WRITE")))
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(expectedResponse.getMessage()));

        verify(transferService, times(1)).makeTransfer(any(TransferDto.class));
    }

    @Test
    void transfer_Error() throws Exception {
        TransferDto body = TransferDto.builder().fromAcc("lukeAcc").toAcc("hanAcc").sum(BigDecimal.valueOf(-100)).build();
        mockMvc.perform(put("/transfer/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body))
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER", "TRANSFER_WRITE")))
                        )))
                .andExpect(status().isInternalServerError());

        verify(transferService, times(0)).makeTransfer(body);
    }

    @Test
    void transfer_Forbidden() throws Exception {
        mockMvc.perform(put("/transfer/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(TEST_BODY)))
                .andExpect(status().isUnauthorized());

        verify(transferService, never()).makeTransfer(any(TransferDto.class));
    }
}
