package ru.yandex.practicum.cash.error;

import org.springframework.http.HttpStatus;

public class CashException extends RuntimeException {
    private final HttpStatus status;

    public CashException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
