package ru.yandex.practicum.cash.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.cash.client.AccountClient;
import ru.yandex.practicum.cash.dto.CashOpDto;
import ru.yandex.practicum.cash.exceptions.InvalidCashAction;

@Service
public class CashService {

    private final AccountClient accountClient;
    private final NotificationProducer notificationProducer;
    private final MeterRegistry meterRegistry;

    public CashService(AccountClient accountClient, NotificationProducer notificationProducer, MeterRegistry meterRegistry) {
        this.accountClient = accountClient;
        this.notificationProducer = notificationProducer;
        this.meterRegistry = meterRegistry;
    }

    public void chargeSum(CashOpDto body) {
        String action = body.getAction().name();
        Timer.Sample sample = Timer.start(meterRegistry);
        String outcome = "success";
        try {
            String msg;
            switch (body.getAction()) {
                case GET -> msg = "Снято %.2f руб".formatted(body.getSum());
                case PUT -> msg = "Положено %.2f руб".formatted(body.getSum());
                default -> throw new InvalidCashAction("Недопустимая операция");
            }

            accountClient.chargeBalance(body);

            DistributionSummary.builder("bank_cash_amount")
                    .description("Сумма операций пополнения/снятия наличных")
                    .tag("action", action)
                    .register(meterRegistry)
                    .record(body.getSum().doubleValue());

            notificationProducer.sendNotification(msg);
        } catch (Exception e) {
            outcome = "error";
            throw e;
        } finally {
            Counter.builder("bank_cash_operation_total")
                    .description("Количество кассовых операций (пополнение/снятие)")
                    .tag("action", action)
                    .tag("outcome", outcome)
                    .register(meterRegistry)
                    .increment();
            sample.stop(Timer.builder("bank_cash_operation_duration_seconds")
                    .description("Длительность кассовой операции")
                    .tag("action", action)
                    .tag("outcome", outcome)
                    .register(meterRegistry));
        }
    }
}
