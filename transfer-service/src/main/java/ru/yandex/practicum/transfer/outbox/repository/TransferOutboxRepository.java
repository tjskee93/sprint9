package ru.yandex.practicum.transfer.outbox.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.transfer.outbox.model.TransferOutbox;

import java.util.List;

@Repository
public interface TransferOutboxRepository extends JpaRepository<TransferOutbox, Long> {

    List<TransferOutbox> findByStatusOrderByCreatedAtAsc(String status);

    @Modifying
    @Query("UPDATE TransferOutbox o SET o.status = :status, o.processedAt = CURRENT_TIMESTAMP WHERE o.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Modifying
    @Query("UPDATE TransferOutbox o SET o.status = :status, o.retryCount = o.retryCount + 1, " +
            "o.lastError = :error WHERE o.id = :id")
    int markFailed(@Param("id") Long id, @Param("status") String status, @Param("error") String error);
}