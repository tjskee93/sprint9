package ru.yandex.practicum.notifications.model.dto;

import jakarta.validation.constraints.NotBlank;

public record NotificationDTO(
        @NotBlank String login,
        @NotBlank String type,
        @NotBlank String message
) {}