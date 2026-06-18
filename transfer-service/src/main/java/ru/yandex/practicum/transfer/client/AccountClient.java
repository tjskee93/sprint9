package ru.yandex.practicum.transfer.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import ru.yandex.practicum.transfer.error.TransferException;
import ru.yandex.practicum.transfer.model.dto.AccountDTO;

import java.util.Map;

@Component
public class AccountClient {

    static final String SERVICE_NAME = "accounts-service";

    private final RestClient restClient;
    private final OAuth2AuthorizedClientManager authorizedClientManager;

    public AccountClient(RestClient.Builder loadBalancedRestClientBuilder,
            @Value("${bank.accounts-base-url}") String baseUrl,
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

    @CircuitBreaker(name = "accounts")
    public AccountDTO withdraw(String login, long amount) {

        return changeBalance(login, amount, "withdraw");
    }
    @CircuitBreaker(name = "accounts")
    public AccountDTO deposit(String login, long amount) {
        return changeBalance(login, amount, "deposit");
    }

    private AccountDTO changeBalance(String login, long amount, String operation){

        String token = serviceToken(SERVICE_NAME);
        try {
            return restClient.post()
                    .uri("/api/internal/accounts/{login}/{operation}", login, operation)
                    .headers(h -> { h.setBearerAuth(token);
                        h.set("X-User-Login", login);})
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(amount)
                    .retrieve()
                    .body(AccountDTO.class);
        } catch (RestClientResponseException e) {
            throw new TransferException((HttpStatus) e.getStatusCode(), e.getResponseBodyAsString());
        }
    }

}