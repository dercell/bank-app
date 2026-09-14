package ru.yandex.practicum.transfer.integration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.transfer.client.AccountClient;
import ru.yandex.practicum.transfer.client.NotificationClient;
import ru.yandex.practicum.transfer.config.TestSecurityConfig;
import ru.yandex.practicum.transfer.dto.ServiceResultDto;
import ru.yandex.practicum.transfer.dto.TransferDto;
import wiremock.com.fasterxml.jackson.databind.ObjectMapper;


import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("controller")
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class TransferControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountClient accountClient;

    @MockitoBean
    private NotificationClient notificationClient;

    private static final ObjectMapper om = new ObjectMapper();
    private static final TransferDto TEST_BODY = TransferDto.builder().fromAcc("lukeAcc").toAcc("hanAcc").sum(BigDecimal.valueOf(1000)).build();


    @Test
    void transfer_Success() throws Exception {
        ServiceResultDto expectedResponse = new ServiceResultDto("Перевод выполнен: 1000 со счёта luke на счёт han");
        when(accountClient.transfer(any(TransferDto.class))).thenReturn(expectedResponse);
        doNothing().when(notificationClient).sendNotification(anyString());

        mockMvc.perform(put("/transfer/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(TEST_BODY))
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER", "TRANSFER_WRITE")))
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(expectedResponse.getMessage()));


    }

    @Test
    void transfer_Error() throws Exception {
        TransferDto badBody = TransferDto.builder().fromAcc("lukeAcc").toAcc("hanAcc").sum(BigDecimal.valueOf(-100)).build();
        mockMvc.perform(put("/transfer/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(badBody))
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER", "TRANSFER_WRITE")))
                        )))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_Forbidden() throws Exception {
        mockMvc.perform(put("/transfer/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(TEST_BODY)))
                .andExpect(status().isUnauthorized());
    }
}