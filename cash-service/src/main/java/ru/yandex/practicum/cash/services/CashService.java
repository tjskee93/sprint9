package ru.yandex.practicum.cash.services;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(CashService.class);

    private final AccountClient accountsClient;
    private final NotificationClient notificationClient;
    private final MeterRegistry meterRegistry;

    public CashService(AccountClient accountsClient, NotificationClient notificationClient, MeterRegistry meterRegistry) {
        this.accountsClient = accountsClient;
        this.notificationClient = notificationClient;
        this.meterRegistry = meterRegistry;
    }

    public String process(String login, CashDTO request) {
        log.info("Updated cash on account: login={}", login);
        if (request.value() <= 0) {
            log.error("Error updated cash on account: login={}. Value equal or less than 0: {}", login, request.value());
            throw new CashException(HttpStatus.BAD_REQUEST, "Сумма должна быть больше нуля");
        }

        if (request.action() == CashAction.GET) {
            try {
                log.debug("Withdraw cash from account: login={}", login);
                accountsClient.withdraw(login, request.value());
            } catch (RuntimeException exception) {
                meterRegistry.counter("bank.cash.withdrawal.failed", "login", login).increment();
                log.error("Error. Withdraw cash from account: login={} failed", login);
                throw exception;
            }
            String message = "Снято %d руб.".formatted(request.value());
            notificationClient.notify(new NotificationDTO(login, "CASH_WITHDRAW", message, request.value()));
            log.info("Updated cash on account: login={} successfully", login);
            return message;
        }

        accountsClient.deposit(login, request.value());
        String message = "Положено %d руб.".formatted(request.value());
        notificationClient.notify(new NotificationDTO(login, "CASH_DEPOSIT", message, request.value()));
        log.info("Updated cash on account: login={} successfully", login);
        return message;
    }
}
