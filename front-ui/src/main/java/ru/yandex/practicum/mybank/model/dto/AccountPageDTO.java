package ru.yandex.practicum.mybank.model.dto;

import java.util.List;

public record AccountPageDTO(AccountDTO account, List<AccountNameDTO> accounts) {
}
