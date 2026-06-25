package ru.yandex.practicum.accounts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.practicum.accounts.client.NotificationClient;
import ru.yandex.practicum.accounts.error.AccountException;
import ru.yandex.practicum.accounts.model.Account;
import ru.yandex.practicum.accounts.repository.AccountRepository;
import ru.yandex.practicum.accounts.services.AccountService;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
class AccountsServiceTests {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @MockitoBean
    private NotificationClient notificationClient;

    private static final String TEST_LOGIN = "solovev";

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();

        Account account = new Account(
                TEST_LOGIN,
                "Solovev",
                "Ilya",
                LocalDate.of(1990, 1, 1),
                1000L
        );
        accountRepository.save(account);

        doNothing().when(notificationClient).notify(anyString(), anyString(), anyString());
    }
    @Test
    void depositIncreasesBalance() {
        long before = accountService.getCurrentAccount("solovev").balance();

        long after = accountService.changeBalance("solovev", 50).balance();

        assertThat(after).isEqualTo(before + 50);
    }

    @Test
    void withdrawRejectsTooLargeAmount() {
        assertThatThrownBy(() -> accountService.changeBalance("solovev", -100000))
                .isInstanceOfSatisfying(AccountException.class, exception ->
                        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }
}