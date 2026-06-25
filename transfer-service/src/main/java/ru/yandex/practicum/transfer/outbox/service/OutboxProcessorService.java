package ru.yandex.practicum.transfer.outbox.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.models.dto.NotificationDTO;
import ru.yandex.practicum.transfer.client.AccountClient;
import ru.yandex.practicum.transfer.client.NotificationClient;
import ru.yandex.practicum.transfer.outbox.model.TransferOutbox;
import ru.yandex.practicum.transfer.outbox.repository.TransferOutboxRepository;

import java.util.List;

@Service
public class OutboxProcessorService {
    private static final Logger log = LoggerFactory.getLogger(OutboxProcessorService.class);

    private final TransferOutboxRepository outboxRepository;
    private final AccountClient accountClient;
    private final NotificationClient notificationClient;
    private final int maxRetryCount;

    public OutboxProcessorService(TransferOutboxRepository outboxRepository,
                                  AccountClient accountClient,
                                  NotificationClient notificationClient,
                                  @Value("${transfer.outbox.max-retry:10}") int maxRetryCount) {
        this.outboxRepository = outboxRepository;
        this.accountClient = accountClient;
        this.notificationClient = notificationClient;
        this.maxRetryCount = maxRetryCount;
    }

    @Scheduled(fixedDelayString = "${transfer.outbox.polling-interval-ms:5000}")
    @Transactional
    public void processPendingEvents() {
        List<TransferOutbox> pendingEvents = outboxRepository.findByStatusOrderByCreatedAtAsc("PENDING");

        for (TransferOutbox event : pendingEvents) {
            try {
                log.info("Processing outbox event {}: {} -> {} amount {}",
                        event.getId(), event.getFromLogin(), event.getToLogin(), event.getAmount());

                accountClient.deposit(event.getToLogin(), event.getAmount());

                event.setStatus("SENT");
                event.setProcessedAt(java.time.LocalDateTime.now());
                outboxRepository.save(event);

                String message = "Получен перевод %d руб. от клиента %s".formatted(
                        event.getAmount(), event.getFromLogin()
                );
                notificationClient.notify(new NotificationDTO(
                        event.getToLogin(),
                        "TRANSFER_RECEIVED",
                        message,
                        event.getAmount()
                ));

                log.info("Outbox event {} processed successfully", event.getId());

            } catch (Exception e) {
                log.error("Failed to process outbox event {}", event.getId(), e);

                int newRetryCount = event.getRetryCount() + 1;
                event.setRetryCount(newRetryCount);
                event.setLastError(e.getMessage());

                if (newRetryCount >= maxRetryCount) {
                    event.setStatus("FAILED");
                    log.error("Outbox event {} permanently failed after {} retries",
                            event.getId(), maxRetryCount);
                    compensateFailedEvent(event);
                } else {
                    event.setStatus("PENDING");
                }

                outboxRepository.save(event);
            }
        }
    }

    private void compensateFailedEvent(TransferOutbox event) {
        log.warn("Creating compensation for failed transfer event {}", event.getId());
        try {
            accountClient.deposit(event.getFromLogin(), event.getAmount());
            log.info("Compensation successful for event {}", event.getId());
        } catch (Exception compEx) {
            log.error("CRITICAL: Compensation failed for event {}! Manual intervention required",
                    event.getId(), compEx);
        }
    }
}