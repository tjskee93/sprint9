package ru.yandex.practicum.notifications.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.notifications.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
