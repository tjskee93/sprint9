package ru.yandex.practicum.cash;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.yandex.practicum.cash.client.AccountClient;
import ru.yandex.practicum.cash.client.NotificationClient;
import ru.yandex.practicum.cash.model.dto.CashDTO;
import ru.yandex.practicum.cash.services.CashService;
import ru.yandex.practicum.cash.model.dto.AccountDTO;
import ru.yandex.practicum.cash.model.CashAction;
import ru.yandex.practicum.kafka.models.dto.NotificationDTO;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CashServiceTest {


    @Test
    void depositCallsAccountsServiceAndNotificationService() {
        AccountClient accountsClient = mock(AccountClient.class);
        NotificationClient notificationClient = mock(NotificationClient.class);
        MeterRegistry meterRegistry = mock(MeterRegistry.class);
        when(accountsClient.deposit("solovev", 150))
                .thenReturn(new AccountDTO("solovev", "Илья", "Соловьев",  LocalDate.of(2001, 1, 1), 150));
        CashService cashService = new CashService(accountsClient, notificationClient, meterRegistry);

        var result = cashService.process("solovev", new CashDTO(150, CashAction.PUT));
        ArgumentCaptor<NotificationDTO> notificationCaptor = ArgumentCaptor.forClass(NotificationDTO.class);

        assertThat(result).isEqualTo("Положено 150 руб.");
        verify(accountsClient).deposit("solovev", 150);
        verify(notificationClient).notify(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue().type()).isEqualTo("CASH_DEPOSIT");
        assertThat(notificationCaptor.getValue().amount()).isEqualTo(150);
    }

    @Test
    void withdrawalCallsAccountsServiceAndNotificationService() {
        AccountClient accountsClient = mock(AccountClient.class);
        NotificationClient notificationClient = mock(NotificationClient.class);
        MeterRegistry meterRegistry = mock(MeterRegistry.class);
        when(accountsClient.withdraw("solovev", 40))
                .thenReturn(new AccountDTO("solovev", "Илья", "Соловьев", LocalDate.of(2001, 1, 1), 60));
        CashService cashService = new CashService(accountsClient, notificationClient, meterRegistry);

        var result = cashService.process("solovev", new CashDTO(40, CashAction.GET));
        ArgumentCaptor<NotificationDTO> notificationCaptor = ArgumentCaptor.forClass(NotificationDTO.class);

        assertThat(result).isEqualTo("Снято 40 руб.");
        verify(accountsClient).withdraw("solovev", 40);
        verify(notificationClient).notify(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue().type()).isEqualTo("CASH_WITHDRAW");
        assertThat(notificationCaptor.getValue().amount()).isEqualTo(40);
    }
}