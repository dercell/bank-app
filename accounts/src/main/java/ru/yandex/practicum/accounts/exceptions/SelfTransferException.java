package ru.yandex.practicum.accounts.exceptions;

public class SelfTransferException extends BusinessException {
    public SelfTransferException(String message) {
        super(message);
    }
}
