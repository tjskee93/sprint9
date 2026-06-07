package ru.yandex.practicum.cash.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    public String process(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CashDTO request) {
        String login = requireLogin(jwt);
        return cashService.process(login, request);
    }


    private static String requireLogin(Jwt jwt) {
        String login = jwt.getClaimAsString("preferred_username");
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("JWT has no preferred_username claim");
        }
        return login;
    }
}
