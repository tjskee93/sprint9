package ru.yandex.practicum.notifications.controllers;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.notifications.model.Notification;
import ru.yandex.practicum.notifications.model.dto.NotificationDTO;
import ru.yandex.practicum.notifications.repository.NotificationRepository;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository repository;
    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    public NotificationController(NotificationRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    @Transactional
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void send(@Valid @RequestBody NotificationDTO notification) {
        Notification notify = repository.save(new Notification(notification.login(), notification.type(), notification.message()));
        log.info("Notification for login={} type={} message={}",
                notify.getLogin(), notify.getType(), notify.getMessage());
    }
}
