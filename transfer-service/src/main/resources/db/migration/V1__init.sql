CREATE TABLE IF NOT EXISTS transfer_outbox (
    id BIGSERIAL PRIMARY KEY,
    from_login VARCHAR(100) NOT NULL,
    to_login VARCHAR(100) NOT NULL,
    amount BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    retry_count INT DEFAULT 0,
    last_error TEXT
);


CREATE INDEX idx_outbox_status_created ON transfer_outbox(status, created_at);