package ru.yandex.practicum.accounts.controllers;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.accounts.model.dto.AccountDTO;
import ru.yandex.practicum.accounts.services.AccountService;

@RestController
@RequestMapping("/api/internal/accounts")
public class InternalAccountController {
    private final AccountService accountService;

    public InternalAccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/{login}/deposit")
    public AccountDTO deposit(@PathVariable String login, @Valid @RequestBody long request) {
        return accountService.changeBalance(login, request);
    }

    @PostMapping("/{login}/withdraw")
    public AccountDTO withdraw(@PathVariable String login, @Valid @RequestBody long request) {
        return accountService.changeBalance(login, -1*request);
    }
}
