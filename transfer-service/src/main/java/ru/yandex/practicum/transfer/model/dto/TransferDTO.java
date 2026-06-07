package ru.yandex.practicum.transfer.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record TransferDTO(
        @NotBlank(message = "Login need not clear")
        String login,
        @Positive(message = "value > 0")
        long value
) { }
