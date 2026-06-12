-- V11__add_pgvector.sql
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE ticket_embeddings (
                                   id         BIGSERIAL PRIMARY KEY,
                                   ticket_id  BIGINT NOT NULL UNIQUE REFERENCES tickets(id) ON DELETE CASCADE,
                                   embedding  vector(384) NOT NULL,
                                   created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Índice para busca por similaridade cosseno
CREATE INDEX idx_ticket_embeddings_cosine
    ON ticket_embeddings USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);