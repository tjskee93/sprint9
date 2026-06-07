CREATE TABLE notification (
    id          bigserial PRIMARY KEY,
    login       VARCHAR(128)  NOT NULL,
    type        VARCHAR(128)  NOT NULL,
    message     text         NOT NULL,
    created_at  timestamptz  NOT NULL DEFAULT now()
);