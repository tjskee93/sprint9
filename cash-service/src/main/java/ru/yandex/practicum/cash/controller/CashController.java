package ru.yandex.practicum.cash.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.cash.model.dto.CashDTO;
import ru.yandex.practicum.cash.services.CashService;

@RestController
@RequestMapping("/api/cash")
public class CashController {
    private final CashService cashService;

    public CashController(CashService cashService) {
        this.cashService = cashService;
    }

    @PostMapping
    public String process(@RequestHeader("X-User-Login") String login, @Valid @RequestBody CashDTO request) {
        return cashService.process(login, request);
    }

}
