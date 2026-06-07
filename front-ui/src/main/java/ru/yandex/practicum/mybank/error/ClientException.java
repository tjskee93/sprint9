package ru.yandex.practicum.mybank.error;

import org.springframework.http.HttpStatusCode;

import java.util.List;

public class ClientException extends RuntimeException {
    private final HttpStatusCode status;
    private final List<String> errors;

    public ClientException(HttpStatusCode status, List<String> errors) {
        super(errors == null || errors.isEmpty() ? "Error: " : errors.getFirst());
        this.status = status;
        this.errors = errors == null ? List.of(getMessage()) : errors;
    }

    public HttpStatusCode getStatus() {
        return status;
    }

    public List<String> getErrors() {
        return errors;
    }
}