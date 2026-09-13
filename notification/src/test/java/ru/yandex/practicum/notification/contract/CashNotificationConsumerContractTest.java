package ru.yandex.practicum.notification.contract;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.StubFinder;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import ru.yandex.practicum.notification.config.StubTriggerKafkaConfig;
import ru.yandex.practicum.notification.model.LogEntity;
import ru.yandex.practicum.notification.model.SourceService;
import ru.yandex.practicum.notification.service.NotificationKafkaListener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;


@Tag("contract")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EmbeddedKafka(topics = {"bank-app-notification"}, partitions = 1, bootstrapServersProperty = "spring.kafka.bootstrap-servers")
@AutoConfigureStubRunner(ids = "ru.yandex.practicum:cash:+:stubs", stubsMode = StubRunnerProperties.StubsMode.LOCAL)
@Import(StubTriggerKafkaConfig.class)
class CashNotificationConsumerContractTest {

    @Autowired
    private StubFinder stubFinder;

    @MockitoSpyBean
    private NotificationKafkaListener notificationKafkaListener;

    @Test
    void cashWithdrawalStub_IsHandledByNotificationListener() {
        stubFinder.trigger("cash_notification_event");

        ArgumentCaptor<LogEntity> captor = ArgumentCaptor.forClass(LogEntity.class);
        verify(notificationKafkaListener, timeout(5_000)).handlerNotification(captor.capture());

        LogEntity received = captor.getValue();
        assertThat(received.getSourceService()).isEqualTo(SourceService.CASH);
        assertThat(received.getMessage()).isEqualTo("Снято 50,00 руб");
    }
}

