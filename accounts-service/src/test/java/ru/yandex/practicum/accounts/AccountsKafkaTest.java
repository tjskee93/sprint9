package ru.yandex.practicum.accounts;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import ru.yandex.practicum.accounts.model.Account;
import ru.yandex.practicum.accounts.model.dto.AccountUpdateDTO;
import ru.yandex.practicum.accounts.repository.AccountRepository;
import ru.yandex.practicum.accounts.services.AccountService;
import ru.yandex.practicum.kafka.models.dto.NotificationDTO;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = {"notifications-service"},
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9092",
                "port=9092"
        }
)
public class AccountsKafkaTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private static final String TOPIC = "notifications-service";

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
    }

    private Consumer<String, NotificationDTO> createConsumer() {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                "test-group-" + UUID.randomUUID(), "false", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, NotificationDTO.class);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        consumerProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        consumerProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);

        DefaultKafkaConsumerFactory<String, NotificationDTO> consumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProps);
        Consumer<String, NotificationDTO> consumer = consumerFactory.createConsumer();
        consumer.subscribe(java.util.Collections.singletonList(TOPIC));
        consumer.poll(Duration.ofMillis(1000));
        return consumer;
    }

    @Test
    @DisplayName("Тест отправки сообщения через KafkaTemplate напрямую")
    void testDirectKafkaSend() throws Exception {
        Consumer<String, NotificationDTO> consumer = createConsumer();

        try {
            NotificationDTO dto = new NotificationDTO(
                    "test-user",
                    "TEST_TYPE",
                    "Test message",
                    100L
            );

            kafkaTemplate.send(TOPIC, "test-key", dto);

            ConsumerRecord<String, NotificationDTO> record = KafkaTestUtils.getSingleRecord(
                    consumer, TOPIC, Duration.ofSeconds(10));

            assertThat(record).isNotNull();
            assertThat(record.value()).isNotNull();
            assertThat(record.value().login()).isEqualTo("test-user");
            assertThat(record.value().type()).isEqualTo("TEST_TYPE");
            assertThat(record.value().message()).isEqualTo("Test message");
            assertThat(record.value().amount()).isEqualTo(100L);

        } finally {
            consumer.close();
        }
    }

    @Test
    @DisplayName("Обновление аккаунта отправляет уведомление в Kafka")
    void updateAccountSendsNotification() throws Exception {
        String login = "test-user";
        accountRepository.save(new Account(
                login,
                "Old First",
                "Old Last",
                LocalDate.now().minusYears(20),
                1000L
        ));

        Consumer<String, NotificationDTO> consumer = createConsumer();

        try {
            accountService.updateCurrentAccount(login,
                    new AccountUpdateDTO(
                            "New First",
                            "New Last",
                            LocalDate.now().minusYears(25)
                    )
            );

            Thread.sleep(2000);

            ConsumerRecord<String, NotificationDTO> record = KafkaTestUtils.getSingleRecord(
                    consumer, TOPIC, Duration.ofSeconds(10));

            assertThat(record).isNotNull();
            NotificationDTO dto = record.value();
            assertThat(dto.login()).isEqualTo(login);
            assertThat(dto.type()).isEqualTo("ACCOUNT_UPDATED");
            assertThat(dto.message()).isEqualTo("Данные аккаунта обновлены");

        } finally {
            consumer.close();
        }
    }
}