package ru.yandex.practicum.gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureWebTestClient
class GatewayApplicationTests {
    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("Health доступен без токена")
    void actuatorHealthIsPublic() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("Another actuator endpoint требует токен")
    void actuatorEnvRequiresAuthentication() {
        webTestClient.get()
                .uri("/actuator/env")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}