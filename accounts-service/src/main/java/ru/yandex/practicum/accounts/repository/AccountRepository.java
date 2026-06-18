package ru.yandex.practicum.accounts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.accounts.model.Account;

public interface AccountRepository extends JpaRepository<Account, String> {
}
