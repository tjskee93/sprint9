package ru.yandex.practicum.mybank.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(FrontClient.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public FrontClient(RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    public AccountPageDTO loadPage(String token) {
        log.info("Loading account page");
        AccountDTO account = getAccount(token);
        List<AccountNameDTO> accounts = getTransferOthers(token);
        log.info("Account page loaded successfully, user: {}", account.login());
        return new AccountPageDTO(account, accounts);
    }


    public AccountDTO getAccount(String token) {
        log.info("Fetching current user account");
        try {
            AccountDTO account = restClient.get()
                    .uri("/api/accounts/me")
                    .headers(bearer(token))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleError)
                    .body(AccountDTO.class);
            log.info("Account fetched successfully for user: {}", account.login());
            return account;
        } catch (ClientException e) {
            log.error("Failed to fetch account: status={}, errors={}", e.getStatus(), e.getErrors());
            throw e;
        }
    }


    public List<AccountNameDTO> getTransferOthers(String token) {
        log.info("Fetching other accounts for transfers");
        try {
            AccountNameDTO[] accounts = restClient.get()
                    .uri("/api/accounts/others")
                    .headers(bearer(token))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleError)
                    .body(AccountNameDTO[].class);
            List<AccountNameDTO> result = accounts == null ? List.of() : Arrays.asList(accounts);
            log.info("Fetched {} other accounts", result.size());
            return result;
        } catch (ClientException e) {
            log.error("Failed to fetch other accounts: status={}, errors={}", e.getStatus(), e.getErrors());
            throw e;
        }
    }


    public void updateAccount(String token, AccountUpdateDTO request) {
        log.info("Updating account for user");
        try {
            restClient.put()
                    .uri("/api/accounts/me")
                    .headers(bearer(token))
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleError);
            log.info("Account updated successfully");
        } catch (ClientException e) {
            log.error("Failed to update account: status={}, errors={}", e.getStatus(), e.getErrors());
            throw e;
        }
    }


    public String cash(String token, CashDTO request) {
        log.info("Processing cash operation, amount: {}", request.value());
        try {
            String result = restClient.post()
                    .uri("/api/cash")
                    .headers(bearer(token))
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleError)
                    .body(String.class);
            log.info("Cash operation completed successfully: {}", result);
            return result;
        } catch (ClientException e) {
            log.error("Cash operation failed: status={}, amount={}, errors={}",
                    e.getStatus(), request.value(), e.getErrors());
            throw e;
        }
    }

    public String transfer(String token, TransferDTO request) {
        log.info("Processing transfer, to: {}, amount: {}",
                 request.login(), request.value());
        try {
            String result = restClient.post()
                    .uri("/api/transfers")
                    .headers(bearer(token))
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleError)
                    .body(String.class);
            log.info("Transfer completed successfully: {}", result);
            return result;
        } catch (ClientException e) {
            log.error("Transfer failed: status={}, to={}, amount={}, errors={}",
                    e.getStatus(), request.login(),
                    request.value(), e.getErrors());
            throw e;
        }
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
