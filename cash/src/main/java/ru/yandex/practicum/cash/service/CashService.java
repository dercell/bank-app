package ru.yandex.practicum.cash.service;

import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.cash.client.AccountClient;
import ru.yandex.practicum.cash.dto.CashOpDto;
import ru.yandex.practicum.cash.exceptions.InvalidCashAction;
import ru.yandex.practicum.common.metrics.BusinessMetrics;

import java.util.List;

@Service
public class CashService {

    private final AccountClient accountClient;
    private final NotificationProducer notificationProducer;
    private final MeterRegistry meterRegistry;
    private final BusinessMetrics businessMetrics;

    public CashService(AccountClient accountClient, NotificationProducer notificationProducer, MeterRegistry meterRegistry) {
        this.accountClient = accountClient;
        this.notificationProducer = notificationProducer;
        this.meterRegistry = meterRegistry;
        this.businessMetrics = new BusinessMetrics(meterRegistry);
    }

    public void chargeSum(CashOpDto body) {
        String action = body.getAction().name();
        String msg;
        switch (body.getAction()) {
            case GET -> msg = "Снято %.2f руб".formatted(body.getSum());
            case PUT -> msg = "Положено %.2f руб".formatted(body.getSum());
            default -> throw new InvalidCashAction("Недопустимая операция");
        }

        businessMetrics.record(
                "bank_cash_operation_total",
                "bank_cash_operation_duration_seconds",
                "Кассовая операция (пополнение/снятие)",
                "rubles",
                List.of(Tag.of("action", action)),
                () -> accountClient.chargeBalance(body));

        DistributionSummary.builder("bank_cash_amount")
                .description("Сумма операций пополнения/снятия наличных")
                .tag("action", action)
                .register(meterRegistry)
                .record(body.getSum().doubleValue());

        notificationProducer.sendNotification(msg);
    }

}
