package ru.yandex.practicum.notifications.messaging;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.kafka.models.dto.NotificationDTO;
import ru.yandex.practicum.notifications.model.Notification;
import ru.yandex.practicum.notifications.repository.NotificationRepository;

@Component
public class NotificationConsumer {

    private final NotificationRepository repository;
    private final MeterRegistry meterRegistry;
    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    public NotificationConsumer(NotificationRepository repository, MeterRegistry meterRegistry) {
        this.repository = repository;
        this.meterRegistry = meterRegistry;
    }

    @KafkaListener(topics = "${bank.notifications-topic}",
            groupId = "${spring.kafka.consumer.group-id:notifications-service}")
    @Transactional
    public void consume(NotificationDTO request) {
        try {
            Notification notify = repository.save(new Notification(request.login(), request.type(), request.message(), request.amount()));
            log.info("Notification for login={} type={} message={}",
                    notify.getLogin(), notify.getType(), notify.getMessage());

        } catch (RuntimeException exception) {
            meterRegistry.counter("bank.notification.failed", "login", request.login());
            log.error("Notification error login={} type={} message={}",
                    request.login(), request.type(), request.message());
            throw exception;
        }
    }
}
