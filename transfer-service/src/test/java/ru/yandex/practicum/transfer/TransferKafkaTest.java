package ru.yandex.practicum.transfer;

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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.practicum.kafka.models.dto.NotificationDTO;
import ru.yandex.practicum.transfer.client.AccountClient;
import ru.yandex.practicum.transfer.model.dto.TransferDTO;
import ru.yandex.practicum.transfer.outbox.repository.TransferOutboxRepository;
import ru.yandex.practicum.transfer.service.TransferService;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = {"notifications-service"},
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9092",
                "port=9092"
        }
)
public class TransferKafkaTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    private TransferOutboxRepository outboxRepository;

    @MockitoBean
    private AccountClient accountClient;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private static final String TOPIC = "notifications-service";

    @BeforeEach
    void setUp() {
        outboxRepository.deleteAll();
        when(accountClient.withdraw(anyString(), anyLong())).thenReturn(null);
        when(accountClient.deposit(anyString(), anyLong())).thenReturn(null);
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
    @DisplayName("Тест прямой отправки через KafkaTemplate")
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
    @DisplayName("Перевод отправляет уведомление в Kafka")
    void transferSendsNotification() throws Exception {
        String fromLogin = "sender";
        String toLogin = "receiver";
        long amount = 1000L;

        Consumer<String, NotificationDTO> consumer = createConsumer();

        try {
            transferService.transfer(fromLogin, new TransferDTO(toLogin, amount));

            Thread.sleep(2000);

            ConsumerRecord<String, NotificationDTO> record = KafkaTestUtils.getSingleRecord(
                    consumer, TOPIC, Duration.ofSeconds(10));

            assertThat(record).isNotNull();
            NotificationDTO dto = record.value();
            assertThat(dto.login()).isEqualTo(fromLogin);
            assertThat(dto.type()).isEqualTo("TRANSFER_SENT");
            assertThat(dto.message()).isEqualTo("Успешно переведено %d руб. клиенту %s".formatted(amount, toLogin));
            assertThat(dto.amount()).isEqualTo(amount);

            assertThat(outboxRepository.count()).isEqualTo(1);
            var outbox = outboxRepository.findAll().get(0);
            assertThat(outbox.getFromLogin()).isEqualTo(fromLogin);
            assertThat(outbox.getToLogin()).isEqualTo(toLogin);
            assertThat(outbox.getAmount()).isEqualTo(amount);

        } finally {
            consumer.close();
        }
    }
}