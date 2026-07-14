package ru.yandex.practicum.accounts.client;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.NotificationPublisher;
import ru.yandex.practicum.kafka.models.dto.NotificationDTO;


@Component
public class NotificationClient {

    private final NotificationPublisher notificationPublisher;

    public NotificationClient(NotificationPublisher notificationPublisher) {
        this.notificationPublisher = notificationPublisher;
    }

    public void notify(String login, String type, String message) {
        NotificationDTO request = new NotificationDTO(login, type, message, null);
        notificationPublisher.publish(request);
    }

}