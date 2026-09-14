package ru.yandex.practicum.accounts.exceptions;

public class NegativeSum extends BusinessException {
    public NegativeSum(String message) {
        super(message);
    }
}
