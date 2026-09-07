package ru.yandex.practicum.accounts.repository;

import org.springframework.data.repository.CrudRepository;
import ru.yandex.practicum.accounts.model.entity.OperationLog;

public interface OperationLogRepository extends CrudRepository<OperationLog, Long> {
}
