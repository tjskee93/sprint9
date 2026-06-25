package ru.yandex.practicum.transfer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.yandex.practicum.kafka.config.NotificationKafkaAutoConfiguration;


@SpringBootApplication
@Import(NotificationKafkaAutoConfiguration.class)
@EnableScheduling
public class TransferServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TransferServiceApplication.class, args);
    }
}
