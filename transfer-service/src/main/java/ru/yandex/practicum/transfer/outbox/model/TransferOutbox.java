package ru.yandex.practicum.transfer.outbox.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfer_outbox")
public class TransferOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fromLogin;

    @Column(nullable = false)
    private String toLogin;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    private Integer retryCount = 0;

    private String lastError;

    public TransferOutbox() {}

    public TransferOutbox(String fromLogin, String toLogin, Long amount) {
        this.fromLogin = fromLogin;
        this.toLogin = toLogin;
        this.amount = amount;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
        this.retryCount = 0;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFromLogin() { return fromLogin; }
    public void setFromLogin(String fromLogin) { this.fromLogin = fromLogin; }

    public String getToLogin() { return toLogin; }
    public void setToLogin(String toLogin) { this.toLogin = toLogin; }

    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }

    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
}
