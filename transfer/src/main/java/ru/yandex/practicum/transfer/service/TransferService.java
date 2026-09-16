package ru.yandex.practicum.transfer.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.transfer.client.AccountClient;
import ru.yandex.practicum.transfer.dto.ServiceResultDto;
import ru.yandex.practicum.transfer.dto.TransferDto;

@Slf4j
@Service
public class TransferService {

    private final AccountClient accountClient;
    private final NotificationProducer notificationProducer;
    private final MeterRegistry meterRegistry;


    public TransferService(AccountClient accountClient, NotificationProducer notificationProducer, MeterRegistry meterRegistry) {
        this.accountClient = accountClient;
        this.notificationProducer = notificationProducer;
        this.meterRegistry = meterRegistry;
    }

    public ServiceResultDto makeTransfer(TransferDto body) {
        Timer.Sample sample = Timer.start(meterRegistry);
        String outcome = "success";
        try {
            ServiceResultDto result = accountClient.transfer(body);

            DistributionSummary.builder("bank_transfer_amount")
                    .description("Сумма переводов между счетами")
                    .register(meterRegistry)
                    .record(body.getSum().doubleValue());

            notificationProducer.sendNotification("Перевод выполнен: %.2f со счёта %s на счёт %s".formatted(body.getSum(), body.getFromAcc(), body.getToAcc()));
            return result;
        } catch (Exception e) {
            outcome = "error";
            throw e;
        } finally {
            Counter.builder("bank_transfer_total")
                    .description("Количество операций перевода")
                    .tag("outcome", outcome)
                    .register(meterRegistry)
                    .increment();
            sample.stop(Timer.builder("bank_transfer_duration_seconds")
                    .description("Длительность операции перевода")
                    .tag("outcome", outcome)
                    .register(meterRegistry));
        }
    }
}
