package ru.yandex.practicum.transfer.service;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.models.dto.NotificationDTO;
import ru.yandex.practicum.transfer.client.AccountClient;
import ru.yandex.practicum.transfer.client.NotificationClient;
import ru.yandex.practicum.transfer.error.TransferException;
import ru.yandex.practicum.transfer.model.dto.TransferDTO;
import ru.yandex.practicum.transfer.outbox.model.TransferOutbox;
import ru.yandex.practicum.transfer.outbox.repository.TransferOutboxRepository;

@Service
public class TransferService {
    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final AccountClient accountClient;
    private final NotificationClient notificationClient;
    private final TransferOutboxRepository outboxRepository;
    private final MeterRegistry meterRegistry;


    public TransferService(AccountClient accountClient, NotificationClient notificationClient, TransferOutboxRepository outboxRepository, MeterRegistry meterRegistry) {
        this.accountClient = accountClient;
        this.notificationClient = notificationClient;
        this.outboxRepository = outboxRepository;
        this.meterRegistry = meterRegistry;
    }

    @Transactional
    public String transfer(String fromLogin, TransferDTO request) {
        log.info("Start transfer cash from account: fromLogin={} to account: toLogin={}", fromLogin, request.login());
        if (request.value() <= 0) {
            log.error("Error updated cash on account: login={}. Value equal or less than 0: {}", fromLogin, request.value());
            throw new TransferException(HttpStatus.BAD_REQUEST, "Сумма должна быть больше нуля");
        }
        if (fromLogin.equals(request.login())) {
            log.error("Error transfer cash from account: login={}. Logins is equal", fromLogin);
            throw new TransferException(HttpStatus.BAD_REQUEST, "Нельзя перевести деньги самому себе");
        }

        try{
            log.debug("Withdraw cash from account: login={}", fromLogin);
            accountClient.withdraw(fromLogin, request.value());
        } catch (RuntimeException exception){
            meterRegistry.counter("bank.transfer.failed", "from", fromLogin, "to", request.login());
            log.error("Error. Withdraw cash from account: login={} failed", fromLogin);
            throw exception;
        }

        TransferOutbox outbox = new TransferOutbox(
                fromLogin,
                request.login(),
                request.value()
        );
        outboxRepository.save(outbox);

        String message = "Успешно переведено %d руб. клиенту %s".formatted(request.value(), request.login());
        notificationClient.notify(new NotificationDTO(fromLogin, "TRANSFER_SENT", message, request.value()));
        log.info("Transfer cash from account: fromLogin={} to account: toLogin={} successfully", fromLogin, request.login());
        return "Перевод инициирован. Статус: PENDING";
    }
}
