package ru.yandex.practicum.accounts.exceptions;

public class NotEnoughMoneyException extends BusinessException {
    public NotEnoughMoneyException(String message) {
        super(message);
    }
}
