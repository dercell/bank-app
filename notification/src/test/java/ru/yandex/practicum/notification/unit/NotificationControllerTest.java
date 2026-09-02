package ru.yandex.practicum.notification.unit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.notification.config.TestSecurityConfig;
import ru.yandex.practicum.notification.controller.NotificationController;
import ru.yandex.practicum.notification.model.LogEntity;
import ru.yandex.practicum.notification.service.NotificationService;

import java.util.List;
import java.util.Map;


import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("unit")
@Tag("controller")
@WebMvcTest(NotificationController.class)
@Import(TestSecurityConfig.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    private static final String LOG_JSON = """
           {
                "sourceService": "ACCOUNTS",
                "message": "Профиль обновлен"
            }
            """;

    @Test
    void sendLog_Success() throws Exception {
        doNothing().when(notificationService).sendNotification(any(LogEntity.class));

        mockMvc.perform(post("/notification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOG_JSON)
                        .with(jwt().jwt(jwt -> jwt
                                .claim("realm_access", Map.of("roles", List.of("SERVICE", "NOTIFICATION_WRITE")))
                        )))
                .andExpect(status().isNoContent());

        verify(notificationService, times(1)).sendNotification(any(LogEntity.class));
    }


    @Test
    void sendLog_Forbidden() throws Exception {
        mockMvc.perform(post("/notification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOG_JSON))
                .andExpect(status().isUnauthorized());

        verify(notificationService, never()).sendNotification(any(LogEntity.class));
    }
}
