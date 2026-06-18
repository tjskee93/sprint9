package ru.yandex.practicum.mybank.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ru.yandex.practicum.mybank.model.CashAction;

public record CashDTO(
        @Positive(message = "value > 0")
        long value,
        @NotNull(message = "Operation type required")
        CashAction action
) {}
