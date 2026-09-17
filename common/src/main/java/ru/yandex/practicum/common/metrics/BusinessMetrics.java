package ru.yandex.practicum.common.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


public class BusinessMetrics {

    private final MeterRegistry meterRegistry;

    public BusinessMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public <T> T record(
            String counterName,
            String timerName,
            String description,
            String baseUnit,
            Iterable<Tag> tags,
            Supplier<T> businessCall) {
        Timer.Sample sample = Timer.start(meterRegistry);
        String outcome = "success";
        try {
            return businessCall.get();
        } catch (RuntimeException e) {
            outcome = "error";
            throw e;
        } finally {
            List<Tag> allTags = new ArrayList<>();
            tags.forEach(allTags::add);
            allTags.add(Tag.of("outcome", outcome));

            Counter.builder(counterName)
                    .description(description)
                    .baseUnit(baseUnit)
                    .tags(allTags)
                    .register(meterRegistry)
                    .increment();

            sample.stop(Timer.builder(timerName)
                    .description(description)
                    .tags(allTags)
                    .register(meterRegistry));
        }
    }

    public void record(String counterName, String timerName, String description,
                       String baseUnit, Iterable<Tag> tags, Runnable businessCall) {
        record(counterName, timerName, description, baseUnit, tags, () -> {
            businessCall.run();
            return null;
        });
    }
}


