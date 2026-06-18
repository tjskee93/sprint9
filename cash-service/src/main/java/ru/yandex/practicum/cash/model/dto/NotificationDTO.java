package ru.yandex.practicum.cash.model.dto;

import jakarta.validation.constraints.NotBlank;

public record NotificationDTO(
        @NotBlank(message = "Need login")  String login,
        @NotBlank(message = "Need type") String type,
        @NotBlank(message = "Need message") String message,
        Long amount
) {
}
