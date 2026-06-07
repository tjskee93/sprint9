package ru.yandex.practicum.mybank.model;

import java.util.List;

public record ErrorModel (String message, List<String> errors) {
    public ErrorModel(String message) {
        this(message, List.of(message));
    }
    public static ErrorModel of(String message) {
        return new ErrorModel(message);
    }
    public static ErrorModel of(List<String> errors) {
        String message = errors == null || errors.isEmpty() ? "Ошибка выполнения операции" : errors.getFirst();
        return new ErrorModel(message, errors == null ? List.of(message) : errors);
    }
}
