package ru.yandex.practicum.transfer;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.yandex.practicum.transfer.client.AccountClient;
import ru.yandex.practicum.transfer.client.NotificationClient;
import ru.yandex.practicum.transfer.model.dto.AccountDTO;
import ru.yandex.practicum.transfer.model.dto.NotificationDTO;
import ru.yandex.practicum.transfer.model.dto.TransferDTO;
import ru.yandex.practicum.transfer.service.TransferService;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransferServiceTest {

    @Test
    void transferWithdrawsFromSenderAndDepositsToRecipient() {
        AccountClient accountsClient = mock(AccountClient.class);
        NotificationClient notificationClient = mock(NotificationClient.class);
        when(accountsClient.withdraw("solovev", 25))
                .thenReturn(new AccountDTO("solovev", "Илья", "Соловьев", LocalDate.of(2001, 1, 1), 75));
        when(accountsClient.deposit("solovev2", 25))
                .thenReturn(new AccountDTO("solovev2", "Илья", "Соловьев", LocalDate.of(1997, 5, 20), 275));
        TransferService service = new TransferService(accountsClient, notificationClient);

        var result = service.transfer("solovev", new TransferDTO("solovev2", 25));
        ArgumentCaptor<NotificationDTO> notificationCaptor = ArgumentCaptor.forClass(NotificationDTO.class);

        assertThat(result).contains("25 руб.").contains("solovev2");
        verify(accountsClient).withdraw("solovev", 25);
        verify(accountsClient).deposit("solovev2", 25);
        verify(notificationClient, times(2)).notify(notificationCaptor.capture());
        assertThat(notificationCaptor.getAllValues())
                .extracting(NotificationDTO::type)
                .containsExactly("TRANSFER_SENT", "TRANSFER_RECEIVED");
    }

    @Test
    void transferRollsBackWhenDepositToRecipientFails() {
        AccountClient accountsClient = mock(AccountClient.class);
        NotificationClient notificationClient = mock(NotificationClient.class);
        RuntimeException failure = new RuntimeException("recipient deposit failed");
        when(accountsClient.withdraw("solovev", 25))
                .thenReturn(new AccountDTO("solovev", "Илья", "Соловьев", LocalDate.of(2001, 1, 1), 75));
        when(accountsClient.deposit("solovev2", 25)).thenThrow(failure);
        when(accountsClient.deposit("solovev", 25))
                .thenReturn(new AccountDTO("solovev", "Илья", "Соловьев2", LocalDate.of(2001, 1, 1), 100));
        TransferService service = new TransferService(accountsClient, notificationClient);

        assertThatThrownBy(() -> service.transfer("solovev", new TransferDTO("solovev2", 25)))
                .isSameAs(failure);

        verify(accountsClient).withdraw("solovev", 25);
        verify(accountsClient).deposit("solovev2", 25);
        verify(accountsClient).deposit("solovev", 25);
        verify(notificationClient, never()).notify(any());
    }
}