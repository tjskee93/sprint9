package ru.yandex.practicum.cash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.kafka.config.NotificationKafkaAutoConfiguration;


@SpringBootApplication
@Import(NotificationKafkaAutoConfiguration.class)
public class CashServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CashServiceApplication.class, args);
    }
}
