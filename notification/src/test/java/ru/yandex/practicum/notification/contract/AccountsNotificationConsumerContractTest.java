package ru.yandex.practicum.notification.contract;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.spec.Contract;
import org.springframework.cloud.contract.stubrunner.StubConfiguration;
import org.springframework.cloud.contract.stubrunner.StubFinder;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import ru.yandex.practicum.notification.config.ContractTriggerKafkaConfig;
import ru.yandex.practicum.notification.model.LogEntity;
import ru.yandex.practicum.notification.model.SourceService;
import ru.yandex.practicum.notification.service.NotificationKafkaListener;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@Tag("contract")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EmbeddedKafka(topics = {"bank-app-notification"}, partitions = 1)
@AutoConfigureStubRunner(ids = "ru.yandex.practicum:accounts:+:stubs", stubsMode = StubRunnerProperties.StubsMode.LOCAL)
@Import(ContractTriggerKafkaConfig.class)
class AccountsNotificationConsumerContractTest {

    @Autowired
    private StubFinder stubFinder;

    @Autowired
    private KafkaTemplate<Object, Object> contractTriggerKafkaTemplate;

    @MockitoSpyBean
    private NotificationKafkaListener notificationKafkaListener;

    @Test
    void cashContract_IsHandledByNotificationListener() {
        Map<StubConfiguration, Collection<Contract>> allContracts = stubFinder.getContracts();

        Contract contract = allContracts.entrySet().stream()
                .filter(e -> "accounts".equals(e.getKey().getArtifactId()))
                .flatMap(e -> e.getValue().stream())
                .filter(c -> "accounts_notification_event".equals(c.getLabel()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("accounts_notification_event contract not found"));

        String destination = contract.getOutputMessage().getSentTo().getClientValue();
        Map<String, Object> body = (Map<String, Object>) contract.getOutputMessage().getBody().getClientValue();
        contractTriggerKafkaTemplate.send(destination, UUID.randomUUID(), body);

        ArgumentCaptor<LogEntity> captor = ArgumentCaptor.forClass(LogEntity.class);
        verify(notificationKafkaListener, timeout(5_000)).handlerNotification(captor.capture(), any(Acknowledgment.class));

        LogEntity received = captor.getValue();
        assertThat(received.getSourceService()).isEqualTo(SourceService.valueOf((String) body.get("sourceService")));
        assertThat(received.getMessage()).isEqualTo(body.get("message"));
    }
}


