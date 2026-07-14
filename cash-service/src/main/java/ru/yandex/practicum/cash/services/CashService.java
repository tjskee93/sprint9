package ru.yandex.practicum.cash.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.cash.client.NotificationClient;
import ru.yandex.practicum.cash.client.AccountClient;
import ru.yandex.practicum.cash.error.CashException;
import ru.yandex.practicum.cash.model.CashAction;
import ru.yandex.practicum.cash.model.dto.CashDTO;
import ru.yandex.practicum.kafka.models.dto.NotificationDTO;

@Service
public class CashService {
    private final AccountClient accountsClient;
    private final NotificationClient notificationClient;

    public CashService(AccountClient accountsClient, NotificationClient notificationClient) {
        this.accountsClient = accountsClient;
        this.notificationClient = notificationClient;
    }

    public String process(String login, CashDTO request) {
        if (request.value() <= 0) {
            throw new CashException(HttpStatus.BAD_REQUEST, "Сумма должна быть больше нуля");
        }

        if (request.action() == CashAction.GET) {
            accountsClient.withdraw(login, request.value());
            String message = "Снято %d руб.".formatted(request.value());
            notificationClient.notify(new NotificationDTO(login, "CASH_WITHDRAW", message, request.value()));
            return message;
        }

        accountsClient.deposit(login, request.value());
        String message = "Положено %d руб.".formatted(request.value());
        notificationClient.notify(new NotificationDTO(login, "CASH_DEPOSIT", message, request.value()));
        return message;
    }
}
