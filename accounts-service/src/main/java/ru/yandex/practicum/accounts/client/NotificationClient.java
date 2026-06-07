package ru.yandex.practicum.accounts.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.yandex.practicum.accounts.model.dto.NotificationDTO;



@Component
public class NotificationClient {

    static final String SERVICE_NAME = "notifications-service";

    private final RestClient restClient;
    private final OAuth2AuthorizedClientManager authorizedClientManager;

    public NotificationClient(RestClient.Builder loadBalancedRestClientBuilder,
                              @Value("${bank.notifications-base-url}") String baseUrl,
                              OAuth2AuthorizedClientManager authorizedClientManager) {
        this.restClient = loadBalancedRestClientBuilder.baseUrl(baseUrl).build();
        this.authorizedClientManager = authorizedClientManager;
    }

    private String serviceToken(String registrationId) {
        OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest
                .withClientRegistrationId(registrationId)
                .principal(registrationId)
                .build();
        OAuth2AuthorizedClient client = authorizedClientManager.authorize(request);
        if (client == null) {
            throw new IllegalStateException(
                    "Could not obtain service token for registration '" + registrationId + "'");
        }
        return client.getAccessToken().getTokenValue();
    }

    @CircuitBreaker(name = "notifications")
    public void notify(String login, String type, String message) {
        String token = serviceToken(SERVICE_NAME);
        NotificationDTO request = new NotificationDTO(login, type, message, null);
        restClient.post()
                .uri("/api/notifications")
                .headers(h -> h.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

}