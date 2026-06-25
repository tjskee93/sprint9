package ru.yandex.practicum.notifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.kafka.models.dto.NotificationDTO;
import ru.yandex.practicum.notifications.model.Notification;
import ru.yandex.practicum.notifications.repository.NotificationRepository;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedKafka(
        partitions = 1,
        topics = {"notifications-service"},
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9092",
                "port=9092"
        }
)
@DirtiesContext
public class NotificationsConsumerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationRepository repository;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private ObjectMapper objectMapper;

    private KafkaTemplate<String, NotificationDTO> kafkaTemplate;

    private static final String TOPIC = "notifications-service";

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        producerProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        ProducerFactory<String, NotificationDTO> producerFactory =
                new DefaultKafkaProducerFactory<>(producerProps);
        kafkaTemplate = new KafkaTemplate<>(producerFactory);
    }

    @Test
    @DisplayName("Получение уведомлений через Kafka")
    void consumeKafkaMessage() throws Exception {
        NotificationDTO dto = new NotificationDTO(
                "test-user",
                "WITHDRAW",
                "Test message from Kafka",
                500L
        );

        kafkaTemplate.send(TOPIC, dto);

        Thread.sleep(2000);

        assertThat(repository.count()).isEqualTo(1);
        Notification saved = repository.findAll().get(0);
        assertThat(saved.getLogin()).isEqualTo("test-user");
        assertThat(saved.getType()).isEqualTo("WITHDRAW");
        assertThat(saved.getMessage()).isEqualTo("Test message from Kafka");
        assertThat(saved.getAmount()).isEqualTo(500L);
    }

    @Test
    @DisplayName("Обработка нескольких сообщений из Kafka")
    void consumeMultipleKafkaMessages() throws Exception {
        for (int i = 0; i < 3; i++) {
            NotificationDTO dto = new NotificationDTO(
                    "user-" + i,
                    "DEPOSIT",
                    "Message " + i,
                    100L * (i + 1)
            );
            kafkaTemplate.send(TOPIC, dto);
        }

        Thread.sleep(3000);

        assertThat(repository.count()).isEqualTo(3);
    }
}