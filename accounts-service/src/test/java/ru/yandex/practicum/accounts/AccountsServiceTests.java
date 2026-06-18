package ru.yandex.practicum.accounts;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import ru.yandex.practicum.accounts.error.AccountException;
import ru.yandex.practicum.accounts.services.AccountService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class AccountsServiceTests {
    @Autowired
    private AccountService accountService;

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