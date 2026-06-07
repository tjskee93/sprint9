package ru.yandex.practicum.mybank.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.yandex.practicum.mybank.error.ClientException;
import ru.yandex.practicum.mybank.model.ErrorModel;
import ru.yandex.practicum.mybank.model.dto.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Component
public class FrontClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public FrontClient(RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    public AccountPageDTO loadPage(String token) {
        AccountDTO account = getAccount(token);
        List<AccountNameDTO> accounts = getTransferOthers(token);
        return new AccountPageDTO(account, accounts);
    }


    public AccountDTO getAccount(String token) {
        System.out.println("=== TOKEN: " + token);
        System.out.println("=== TOKEN length: " + (token != null ? token.length() : "null"));
        return restClient.get()
                .uri("/api/accounts/me")
                .headers(bearer(token))
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .body(AccountDTO.class);
    }


    public List<AccountNameDTO> getTransferOthers(String token) {
        AccountNameDTO[] accounts = restClient.get()
                .uri("/api/accounts/others")
                .headers(bearer(token))
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .body(AccountNameDTO[].class);
        return accounts == null ? List.of() : Arrays.asList(accounts);
    }


    public void updateAccount(String token, AccountUpdateDTO request) {
        restClient.put()
                .uri("/api/accounts/me")
                .headers(bearer(token))
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError);
    }


    public String cash(String token, CashDTO request) {
        return restClient.post()
                .uri("/api/cash")
                .headers(bearer(token))
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .body(String.class);
    }

    public String transfer(String token, TransferDTO request) {
        return restClient.post()
                .uri("/api/transfers")
                .headers(bearer(token))
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::handleError)
                .body(String.class);
    }
    private Consumer<HttpHeaders> bearer(String token) {
        return headers -> headers.setBearerAuth(token);
    }


    private void handleError(HttpRequest request, ClientHttpResponse response) throws IOException {
        String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
        ErrorModel error = readError(body);
        List<String> errors = error == null ? List.of(defaultErrorMessage(response.getStatusCode())) : error.errors();
        throw new ClientException(response.getStatusCode(), errors);
    }


    private ErrorModel readError(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(body, ErrorModel.class);
        } catch (IOException ignored) {
            return new ErrorModel(body);
        }
    }

    private String defaultErrorMessage(HttpStatusCode status) {
        return "Service is not available: " + status.value();
    }


}
