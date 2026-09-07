package ru.yandex.practicum.accounts.exceptions;

public class InvalidCashAction extends BusinessException {
    public InvalidCashAction(String message) {
        super(message);
    }
}
