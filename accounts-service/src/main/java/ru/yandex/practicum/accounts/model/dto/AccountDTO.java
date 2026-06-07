package ru.yandex.practicum.accounts.model.dto;

import java.time.LocalDate;

public record AccountDTO(
        String login,
        String firstName,
        String lastName,
        LocalDate birth_date,
        long balance
) {}
