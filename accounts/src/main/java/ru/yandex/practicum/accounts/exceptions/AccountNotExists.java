package ru.yandex.practicum.accounts.exceptions;

public class AccountNotExists extends BusinessException {
    public AccountNotExists(String message) {
        super(message);
    }
}
