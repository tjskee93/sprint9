package ru.yandex.practicum.transfer.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
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
    public String transfer(@RequestHeader("X-User-Login") String login, @Valid @RequestBody TransferDTO request) {
        System.out.println(login + " переводит деньги " + request.toString());
        if (login.equals(request.login())) {
            throw new IllegalArgumentException("Cannot transfer to yourself");
        }
        return transferService.transfer(login, request);
    }
}
