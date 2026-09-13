package ru.yandex.practicum.accounts.contract;

import lombok.extern.slf4j.Slf4j;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.practicum.accounts.AccountsApplication;
import ru.yandex.practicum.accounts.config.KafkaContractTestConfig;
import ru.yandex.practicum.accounts.model.entity.UserProfile;
import ru.yandex.practicum.accounts.repository.UserProfileRepository;
import ru.yandex.practicum.accounts.service.AccountsService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

@Slf4j
@AutoConfigureMessageVerifier
@SpringBootTest(classes = AccountsApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("contract-test")
@Import(KafkaContractTestConfig.class)
@EmbeddedKafka(topics = {KafkaContractBase.TEST_TOPIC_NAME}, partitions = 1)
public abstract class KafkaContractBase {

    public static final String TEST_TOPIC_NAME = "bank-app-notification";

    @Autowired
    private AccountsService accountsService;

    @MockitoBean
    private UserProfileRepository userProfileRepository;

    public void triggerAccountUpdate() {
        String login = "luke";
        String username = "Luke Skywalker";
        LocalDate birthdate = LocalDate.of(1990, 1, 15);

        UserProfile userProfile = UserProfile.builder()
                .login(login)
                .username(username)
                .birthDate(birthdate)
                .build();

        when(userProfileRepository.findAll()).thenReturn(List.of(userProfile));
        when(userProfileRepository.getAccountByLogin(login)).thenReturn(Optional.of(userProfile));
        when(userProfileRepository.save(userProfile)).thenReturn(userProfile);

        accountsService.updateAccount(login, username, birthdate);
        log.info("Triggered updateAccount({}, {}, {}) for contract test", login, username, birthdate);
    }

}

