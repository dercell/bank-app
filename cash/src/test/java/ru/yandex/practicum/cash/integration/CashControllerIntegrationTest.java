package ru.yandex.practicum.cash.integration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.cash.client.AccountClient;
import ru.yandex.practicum.cash.client.NotificationClient;
import ru.yandex.practicum.cash.config.TestSecurityConfig;
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@Tag("controller")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class CashControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountClient accountClient;

    @MockitoBean
    private NotificationClient notificationClient;

    private static final String LOGIN = "luke";
    private static final String ACTION = "GET";
    private static final int SUM = 1000;

    @Test
    void chargeSum_Success() throws Exception {
        mockMvc.perform(put("/cash/{login}", LOGIN)
                        .param("action", ACTION)
                        .param("sum", String.valueOf(SUM))
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", List.of("USER", "CASH_WRITE"))
                        )))
                .andExpect(status().isNoContent());

    }


    @Test
    void chargeSum_Error() throws Exception {

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

    }
}
