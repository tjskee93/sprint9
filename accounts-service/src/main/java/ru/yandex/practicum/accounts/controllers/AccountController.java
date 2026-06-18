package ru.yandex.practicum.accounts.controllers;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.accounts.model.dto.AccountDTO;
import ru.yandex.practicum.accounts.model.dto.AccountNameDTO;
import ru.yandex.practicum.accounts.model.dto.AccountUpdateDTO;
import ru.yandex.practicum.accounts.services.AccountService;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public AccountDTO me(@RequestHeader("X-User-Login") String login) {
        service.createIfMissing(login);
        return service.getCurrentAccount(login);
    }

    @PutMapping("/me")
    public AccountDTO update(@RequestHeader("X-User-Login") String login
            , @Valid @RequestBody AccountUpdateDTO request) {
        service.createIfMissing(login);
        return service.updateCurrentAccount(login, request);
    }

    @GetMapping("/others")
    public List<AccountNameDTO> others(@RequestHeader("X-User-Login") String login) {
        service.createIfMissing(login);
        return service.getTransferRecipients(login);
    }
}
