package ru.yandex.practicum.transfer.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.transfer.model.dto.TransferDTO;
import ru.yandex.practicum.transfer.service.TransferService;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {
    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public String transfer(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TransferDTO request) {
        String fromLogin = jwt.getClaimAsString("preferred_username");
        System.out.println(fromLogin + " переводит деньги " + request.toString());
        if (fromLogin.equals(request.login())) {
            throw new IllegalArgumentException("Cannot transfer to yourself");
        }
        return transferService.transfer(fromLogin, request);
    }
}
