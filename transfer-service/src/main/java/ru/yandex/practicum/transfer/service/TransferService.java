package ru.yandex.practicum.transfer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.transfer.client.AccountClient;
import ru.yandex.practicum.transfer.client.NotificationClient;
import ru.yandex.practicum.transfer.error.TransferException;
import ru.yandex.practicum.transfer.model.dto.NotificationDTO;
import ru.yandex.practicum.transfer.model.dto.TransferDTO;

@Service
public class TransferService {
    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final AccountClient accountClient;
    private final NotificationClient notificationClient;

    public TransferService(AccountClient accountClient, NotificationClient notificationClient) {
        this.accountClient = accountClient;
        this.notificationClient = notificationClient;
    }

    public String transfer(String fromLogin, TransferDTO request) {
        if (request.value() <= 0) {
            throw new TransferException(HttpStatus.BAD_REQUEST, "Сумма должна быть больше нуля");
        }
        if (fromLogin.equals(request.login())) {
            throw new TransferException(HttpStatus.BAD_REQUEST, "Нельзя перевести деньги самому себе");
        }

        accountClient.withdraw(fromLogin, request.value());
        try {
            accountClient.deposit(request.login(), request.value());
        } catch (RuntimeException exception) {
            compensate(fromLogin, request.value(), exception);
            throw exception;
        }

        String message = "Успешно переведено %d руб. клиенту %s".formatted(request.value(), request.login());
        notificationClient.notify(new NotificationDTO(fromLogin, "TRANSFER_SENT", message, request.value()));
        notificationClient.notify(new NotificationDTO(
                request.login(),
                "TRANSFER_RECEIVED",
                "Получен перевод %d руб. от клиента %s".formatted(request.value(), fromLogin),
                request.value()
        ));
        return message;
    }

    private void compensate(String fromLogin, long value, RuntimeException originalException) {
        try {
            accountClient.deposit(fromLogin, value);
        } catch (RuntimeException compensationException) {
            log.error("Transfer compensation failed for account {}", fromLogin, compensationException);
            originalException.addSuppressed(compensationException);
        }
    }
}
