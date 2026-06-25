package ru.yandex.practicum.transfer.client;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.NotificationPublisher;
import ru.yandex.practicum.kafka.models.dto.NotificationDTO;


@Component
public class NotificationClient {

    private final NotificationPublisher notificationPublisher;

    public NotificationClient(NotificationPublisher notificationPublisher) {
        this.notificationPublisher = notificationPublisher;
    }

    public void notify(NotificationDTO request) {
        notificationPublisher.publish(request);
    }

}