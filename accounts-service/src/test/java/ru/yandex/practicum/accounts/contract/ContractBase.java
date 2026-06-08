package ru.yandex.practicum.accounts.contract;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.accounts.client.NotificationClient;
import ru.yandex.practicum.accounts.model.Account;
import ru.yandex.practicum.accounts.repository.AccountRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
@SpringBootTest
@AutoConfigureMockMvc
public abstract class ContractBase {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private NotificationClient notificationClient;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void setUpContract() {
        accountRepository.deleteAll();

        // Создаём тестового пользователя с балансом 1000
        Account account = new Account();
        account.setLogin("solovev");
        account.setFirstName("Илья");
        account.setLastName("Соловьев");
        account.setBirth_date(LocalDate.of(1993, 12, 21));
        account.setBalance(1000L);
        accountRepository.save(account);
        when(jwtDecoder.decode(anyString())).thenReturn(Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject("solovev")
                .claim("preferred_username", "solovev")
                .claim("realm_access", Map.of("roles", List.of("accounts.read", "accounts.write")))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build());
        io.restassured.module.mockmvc.RestAssuredMockMvc.mockMvc(mockMvc);
    }
}
