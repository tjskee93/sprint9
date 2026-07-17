package ru.yandex.practicum.transfer;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.yandex.practicum.kafka.models.dto.NotificationDTO;
import ru.yandex.practicum.transfer.client.AccountClient;
import ru.yandex.practicum.transfer.client.NotificationClient;
import ru.yandex.practicum.transfer.model.dto.AccountDTO;
import ru.yandex.practicum.transfer.model.dto.TransferDTO;
import ru.yandex.practicum.transfer.outbox.model.TransferOutbox;
import ru.yandex.practicum.transfer.outbox.repository.TransferOutboxRepository;
import ru.yandex.practicum.transfer.service.TransferService;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TransferServiceTest {

    @Test
    void transferWithdrawsFromSenderAndCreatesOutboxEvent() {
        AccountClient accountsClient = mock(AccountClient.class);
        NotificationClient notificationClient = mock(NotificationClient.class);
        TransferOutboxRepository outboxRepository = mock(TransferOutboxRepository.class);
        MeterRegistry meterRegistry = mock(MeterRegistry.class);

        when(accountsClient.withdraw(eq("solovev"), anyLong()))
                .thenReturn(new AccountDTO("solovev", "Илья", "Соловьев", LocalDate.of(2001, 1, 1), 75));

        TransferService service = new TransferService(accountsClient, notificationClient, outboxRepository, meterRegistry);

        String result = service.transfer("solovev", new TransferDTO("solovev2", 25));

        ArgumentCaptor<TransferOutbox> outboxCaptor = ArgumentCaptor.forClass(TransferOutbox.class);
        ArgumentCaptor<NotificationDTO> notificationCaptor = ArgumentCaptor.forClass(NotificationDTO.class);

        assertThat(result).contains("Перевод инициирован").contains("PENDING");

        verify(accountsClient).withdraw(eq("solovev"), eq(25L));

        verify(outboxRepository).save(outboxCaptor.capture());
        TransferOutbox savedOutbox = outboxCaptor.getValue();
        assertThat(savedOutbox.getFromLogin()).isEqualTo("solovev");
        assertThat(savedOutbox.getToLogin()).isEqualTo("solovev2");
        assertThat(savedOutbox.getAmount()).isEqualTo(25);
        assertThat(savedOutbox.getStatus()).isEqualTo("PENDING");

        verify(notificationClient).notify(notificationCaptor.capture());
        assertThat(notificationCaptor.getValue().type()).isEqualTo("TRANSFER_SENT");
        assertThat(notificationCaptor.getValue().login()).isEqualTo("solovev");

        verify(accountsClient, never()).deposit(anyString(), anyLong());
    }

    @Test
    void transferRollsBackWhenWithdrawFails() {
        AccountClient accountsClient = mock(AccountClient.class);
        NotificationClient notificationClient = mock(NotificationClient.class);
        TransferOutboxRepository outboxRepository = mock(TransferOutboxRepository.class);
        MeterRegistry meterRegistry = mock(MeterRegistry.class);

        RuntimeException failure = new RuntimeException("insufficient funds");
        when(accountsClient.withdraw(eq("solovev"), anyLong())).thenThrow(failure);

        TransferService service = new TransferService(accountsClient, notificationClient, outboxRepository, meterRegistry);

        assertThatThrownBy(() -> service.transfer("solovev", new TransferDTO("solovev2", 25)))
                .isSameAs(failure);

        verify(accountsClient).withdraw(eq("solovev"), eq(25L));
        verify(outboxRepository, never()).save(any());
        verify(accountsClient, never()).deposit(anyString(), anyLong());
        verify(notificationClient, never()).notify(any());
    }

    @Test
    void transferValidatesAmount() {
        AccountClient accountsClient = mock(AccountClient.class);
        NotificationClient notificationClient = mock(NotificationClient.class);
        TransferOutboxRepository outboxRepository = mock(TransferOutboxRepository.class);
        MeterRegistry meterRegistry = mock(MeterRegistry.class);

        TransferService service = new TransferService(accountsClient, notificationClient, outboxRepository, meterRegistry);

        assertThatThrownBy(() -> service.transfer("solovev", new TransferDTO("solovev2", -10)))
                .hasMessage("Сумма должна быть больше нуля");

        assertThatThrownBy(() -> service.transfer("solovev", new TransferDTO("solovev2", 0)))
                .hasMessage("Сумма должна быть больше нуля");

        verify(accountsClient, never()).withdraw(anyString(), anyLong());
        verify(outboxRepository, never()).save(any());
    }

    @Test
    void transferValidatesSelfTransfer() {
        AccountClient accountsClient = mock(AccountClient.class);
        NotificationClient notificationClient = mock(NotificationClient.class);
        TransferOutboxRepository outboxRepository = mock(TransferOutboxRepository.class);
        MeterRegistry meterRegistry = mock(MeterRegistry.class);

        TransferService service = new TransferService(accountsClient, notificationClient, outboxRepository, meterRegistry);

        assertThatThrownBy(() -> service.transfer("solovev", new TransferDTO("solovev", 100)))
                .hasMessage("Нельзя перевести деньги самому себе");

        verify(accountsClient, never()).withdraw(anyString(), anyLong());
        verify(outboxRepository, never()).save(any());
    }
}