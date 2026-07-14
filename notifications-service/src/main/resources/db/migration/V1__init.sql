CREATE TABLE notification (
    id          bigserial PRIMARY KEY,
    login       VARCHAR(128)  NOT NULL,
    type        VARCHAR(128)  NOT NULL,
    message     text         NOT NULL,
    amount      BIGINT       NOT NULL DEFAULT 0,
    created_at  timestamptz  NOT NULL DEFAULT now()
);