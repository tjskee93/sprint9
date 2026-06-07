package ru.yandex.practicum.accounts.services;

import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.accounts.client.NotificationClient;
import ru.yandex.practicum.accounts.error.AccountException;
import ru.yandex.practicum.accounts.model.Account;
import ru.yandex.practicum.accounts.model.dto.AccountDTO;
import ru.yandex.practicum.accounts.model.dto.AccountNameDTO;
import ru.yandex.practicum.accounts.model.dto.AccountUpdateDTO;
import ru.yandex.practicum.accounts.repository.AccountRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class AccountService {
    private final NotificationClient notificationClient;
    private final AccountRepository accountRepository;

    public AccountService(NotificationClient notificationClient, AccountRepository accountRepository) {
        this.notificationClient = notificationClient;
        this.accountRepository = accountRepository;
    }


    @Transactional
    public AccountDTO getCurrentAccount(String login){
        return toAccountDTO(getAccount(login));
    }

    @Transactional
    public AccountDTO createIfMissing(String login) {
        Account account = accountRepository.findById(login)
                .orElseGet(() -> accountRepository.save(new Account(
                        login,
                        "First Name " + login,
                        "Last Name " + login,
                        LocalDate.now().minusYears(18),
                        0
                )));
        return toAccountDTO(account);
    }

    @Transactional
    public AccountDTO updateCurrentAccount(String login, AccountUpdateDTO request) {
        Account account = getAccount(login);
        account.setFirstName(request.firstName());
        account.setLastName(request.lastName());
        account.setBirth_date(request.birth_date());
        notificationClient.notify(
                login,
                "ACCOUNT_UPDATED",
                "Данные аккаунта обновлены"
        );
        return toAccountDTO(accountRepository.save(account));
    }

    @Transactional
    public List<AccountNameDTO> getTransferRecipients(String login) {
        return accountRepository.findAll().stream()
                .filter(account -> !account.getLogin().equals(login))
                .map(account ->
                        new AccountNameDTO(account.getLogin(), account.getFirstName() + " " + account.getLastName()))
                .toList();
    }

    @Transactional
    public AccountDTO changeBalance(String login, long value) {
        Account account = getAccount(login);
        if (account.getBalance() + value < 0) {
            throw new AccountException(HttpStatus.CONFLICT, "Недостаточно средств на счёте");
        }
        account.changeBalance(value);
        accountRepository.save(account);
        return toAccountDTO(account);
    }



    @Transactional
    private Account getAccount(String login){
        return accountRepository.findById(login)
                .orElseThrow(() -> new RuntimeException("Аккаунт " + login + " не найден"));
    }
    private AccountDTO toAccountDTO(Account account){
        return new AccountDTO(account.getLogin()
                , account.getFirstName()
                , account.getLastName()
                , account.getBirth_date()
                , account.getBalance());
    }


}
