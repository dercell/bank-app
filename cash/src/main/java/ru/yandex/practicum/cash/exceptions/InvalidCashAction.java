package ru.yandex.practicum.cash.exceptions;

public class InvalidCashAction extends RuntimeException {
    public InvalidCashAction(String message) {
        super(message);
    }
}
