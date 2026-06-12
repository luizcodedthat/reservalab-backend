CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE computers (
                           id               BIGSERIAL PRIMARY KEY,
                           patrimonio       VARCHAR(50)  NOT NULL UNIQUE,
                           laboratory_id    BIGINT       NOT NULL REFERENCES laboratories(id),
                           ip               VARCHAR(15),
                           processador      VARCHAR(100),
                           ram              VARCHAR(20),
                           so               VARCHAR(100),
                           active           BOOLEAN      NOT NULL DEFAULT TRUE,
                           version          BIGINT       NOT NULL DEFAULT 0,
                           created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
                           updated_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

ALTER TABLE tickets
    ADD COLUMN computer_id      BIGINT REFERENCES computers(id),
    ADD COLUMN prazo_resolucao  TIMESTAMP;