package ru.yandex.practicum.transfer.service;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.common.metrics.BusinessMetrics;
import ru.yandex.practicum.transfer.client.AccountClient;
import ru.yandex.practicum.transfer.dto.ServiceResultDto;
import ru.yandex.practicum.transfer.dto.TransferDto;

import java.util.List;

@Slf4j
@Service
public class TransferService {

    private final AccountClient accountClient;
    private final NotificationProducer notificationProducer;
    private final MeterRegistry meterRegistry;
    private final BusinessMetrics businessMetrics;


    public TransferService(AccountClient accountClient, NotificationProducer notificationProducer, MeterRegistry meterRegistry) {
        this.accountClient = accountClient;
        this.notificationProducer = notificationProducer;
        this.meterRegistry = meterRegistry;
        this.businessMetrics = new BusinessMetrics(meterRegistry);
    }

    public ServiceResultDto makeTransfer(TransferDto body) {
        ServiceResultDto result = businessMetrics.record(
                "bank_transfer_total",
                "bank_transfer_duration_seconds",
                "Операция перевода между счетами",
                "rubles",
                List.of(),
                () -> accountClient.transfer(body));

        DistributionSummary.builder("bank_transfer_amount")
                .description("Сумма переводов между счетами")
                .register(meterRegistry)
                .record(body.getSum().doubleValue());

        notificationProducer.sendNotification("Перевод выполнен: %.2f со счёта %s на счёт %s".formatted(body.getSum(), body.getFromAcc(), body.getToAcc()));
        return result;
    }
}
