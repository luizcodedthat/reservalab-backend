CREATE TYPE reservation_status AS ENUM (
    'PENDING',
    'APPROVED',
    'REJECTED',
    'CANCELLED',
    'COMPLETED'
);

CREATE TABLE reservations
(
    id                   BIGSERIAL PRIMARY KEY,
    laboratory_id        BIGINT             NOT NULL,
    requested_by_user_id BIGINT             NOT NULL,
    approved_by_user_id  BIGINT,
    start_date_time      TIMESTAMP          NOT NULL,
    end_date_time        TIMESTAMP          NOT NULL,
    purpose              VARCHAR(500)       NOT NULL,
    status               reservation_status NOT NULL DEFAULT 'PENDING',
    rejection_reason     VARCHAR(500),
    notes                TEXT,
    created_at           TIMESTAMP          NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP          NOT NULL DEFAULT NOW(),
    version              BIGINT             NOT NULL DEFAULT 0
);

CREATE INDEX idx_reservation_lab_dates
    ON reservations (laboratory_id, start_date_time, end_date_time);

CREATE INDEX idx_reservation_status
    ON reservations (status);

CREATE INDEX idx_reservation_requested_by
    ON reservations (requested_by_user_id);