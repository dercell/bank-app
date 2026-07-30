package ru.yandex.practicum.transfer.integration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.transfer.client.AccountClient;
import ru.yandex.practicum.transfer.client.NotificationClient;
import ru.yandex.practicum.transfer.config.TestSecurityConfig;


import java.util.List;
import java.util.Map;


import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    private static final String FROM_LOGIN = "luke";
    private static final String TO_LOGIN = "han";
    private static final int SUM = 1000;


    @Test
    void transfer_Success() throws Exception {
        String expectedResponse = "Перевод выполнен: 1000 со счёта luke на счёт han";
        when(accountClient.transfer(FROM_LOGIN, TO_LOGIN, SUM)).thenReturn(expectedResponse);
        doNothing().when(notificationClient).sendNotification(anyString());

        mockMvc.perform(put("/transfer/submit")
                        .param("from", FROM_LOGIN)
                        .param("to", TO_LOGIN)
                        .param("sum", String.valueOf(SUM))
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("USER", "TRANSFER_WRITE")))
                        )))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));


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
    }

    @Test
    void transfer_Forbidden() throws Exception {
        mockMvc.perform(put("/transfer/submit")
                        .param("from", FROM_LOGIN)
                        .param("to", TO_LOGIN)
                        .param("sum", String.valueOf(SUM)))
                .andExpect(status().isUnauthorized());
    }
}