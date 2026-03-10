CREATE TABLE users (
                       id              BIGSERIAL       PRIMARY KEY,
                       username        VARCHAR(100)    NOT NULL,
                       email           VARCHAR(255)    NOT NULL,
                       password_hash   VARCHAR(255)    NOT NULL,
                       name            VARCHAR(255)    NOT NULL,
                       role            VARCHAR(20)     NOT NULL,
                       active          BOOLEAN         NOT NULL DEFAULT TRUE,
                       created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
                       updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
                       version         BIGINT          NOT NULL DEFAULT 0,

                       CONSTRAINT uq_users_username    UNIQUE (username),
                       CONSTRAINT uq_users_email       UNIQUE (email),
                       CONSTRAINT ck_users_role        CHECK  (role IN ('STUDENT', 'PROFESSOR', 'SECRETARY', 'TECHNICIAN'))
);

CREATE INDEX idx_user_role_active ON users (role, active);

CREATE TABLE laboratories (
                              id              BIGSERIAL       PRIMARY KEY,
                              name            VARCHAR(255)    NOT NULL,
                              code            VARCHAR(50)     NOT NULL,
                              description     TEXT,
                              computer_count  INTEGER         NOT NULL DEFAULT 0,
                              capacity        INTEGER         NOT NULL,
                              building        VARCHAR(100),
                              floor           VARCHAR(20),
                              active          BOOLEAN         NOT NULL DEFAULT TRUE,
                              created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
                              updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
                              version         BIGINT          NOT NULL DEFAULT 0,

                              CONSTRAINT uq_laboratories_code         UNIQUE (code),
                              CONSTRAINT ck_laboratories_capacity     CHECK  (capacity > 0),
                              CONSTRAINT ck_laboratories_computers    CHECK  (computer_count >= 0)
);

CREATE INDEX idx_laboratory_active ON laboratories (active);

CREATE TABLE reservation_groups (
                                    id                  BIGSERIAL   PRIMARY KEY,
                                    created_by_user_id  BIGINT      NOT NULL,
                                    laboratory_id       BIGINT      NOT NULL,
                                    total_occurrences   INTEGER     NOT NULL,
                                    active_occurrences  INTEGER     NOT NULL,
                                    created_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
                                    updated_at          TIMESTAMP   NOT NULL DEFAULT NOW(),

                                    CONSTRAINT fk_resgroup_user     FOREIGN KEY (created_by_user_id) REFERENCES users (id),
                                    CONSTRAINT fk_resgroup_lab      FOREIGN KEY (laboratory_id)      REFERENCES laboratories (id),
                                    CONSTRAINT ck_resgroup_total    CHECK (total_occurrences > 0),
                                    CONSTRAINT ck_resgroup_active   CHECK (active_occurrences >= 0 AND active_occurrences <= total_occurrences)
);

CREATE INDEX idx_reservation_group_user ON reservation_groups (created_by_user_id);
CREATE INDEX idx_reservation_group_lab  ON reservation_groups (laboratory_id);

CREATE TABLE reservations (
                              id                      BIGSERIAL       PRIMARY KEY,
                              laboratory_id           BIGINT          NOT NULL,
                              requested_by_user_id    BIGINT          NOT NULL,
                              approved_by_user_id     BIGINT,
                              reservation_group_id    BIGINT,
                              reservation_date        DATE            NOT NULL,
                              purpose                 VARCHAR(500)    NOT NULL,
                              status                  VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
                              rejection_reason        VARCHAR(500),
                              notes                   TEXT,
                              total_duration_minutes  INTEGER         NOT NULL,
                              occurrence_number       INTEGER,
                              created_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
                              updated_at              TIMESTAMP       NOT NULL DEFAULT NOW(),
                              version                 BIGINT          NOT NULL DEFAULT 0,

                              CONSTRAINT fk_reservation_lab           FOREIGN KEY (laboratory_id)         REFERENCES laboratories (id),
                              CONSTRAINT fk_reservation_requested_by  FOREIGN KEY (requested_by_user_id)  REFERENCES users (id),
                              CONSTRAINT fk_reservation_approved_by   FOREIGN KEY (approved_by_user_id)   REFERENCES users (id),
                              CONSTRAINT fk_reservation_group         FOREIGN KEY (reservation_group_id)  REFERENCES reservation_groups (id),
                              CONSTRAINT ck_reservation_status        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED', 'COMPLETED')),
                              CONSTRAINT ck_reservation_duration      CHECK (total_duration_minutes > 0)
);

CREATE INDEX idx_reservation_lab_date  ON reservations (laboratory_id, reservation_date);
CREATE INDEX idx_reservation_group     ON reservations (reservation_group_id);
CREATE INDEX idx_reservation_user      ON reservations (requested_by_user_id);
CREATE INDEX idx_reservation_status    ON reservations (status);

CREATE TABLE reservation_time_blocks (
                                         id              BIGSERIAL   PRIMARY KEY,
                                         reservation_id  BIGINT      NOT NULL,
                                         start_time      TIME        NOT NULL,
                                         end_time        TIME        NOT NULL,
                                         block_order     INTEGER     NOT NULL DEFAULT 1,
                                         duration_minutes INTEGER    NOT NULL,
                                         created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),

                                         CONSTRAINT fk_timeblock_reservation FOREIGN KEY (reservation_id) REFERENCES reservations (id) ON DELETE CASCADE,
                                         CONSTRAINT ck_timeblock_times       CHECK (end_time > start_time),
                                         CONSTRAINT ck_timeblock_duration    CHECK (duration_minutes > 0)
);

CREATE INDEX idx_timeblock_reservation ON reservation_time_blocks (reservation_id, block_order);

CREATE TABLE tickets (
                         id                  BIGSERIAL       PRIMARY KEY,
                         laboratory_id       BIGINT          NOT NULL,
                         created_by_user_id  BIGINT          NOT NULL,
                         assigned_to_user_id BIGINT,
                         title               VARCHAR(255)    NOT NULL,
                         description         TEXT            NOT NULL,
                         status              VARCHAR(20)     NOT NULL DEFAULT 'OPEN',
                         priority            VARCHAR(20)     NOT NULL DEFAULT 'MEDIUM',
                         resolution_comment  TEXT,
                         resolved_at         TIMESTAMP,
                         created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
                         updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
                         version             BIGINT          NOT NULL DEFAULT 0,

                         CONSTRAINT fk_ticket_lab            FOREIGN KEY (laboratory_id)         REFERENCES laboratories (id),
                         CONSTRAINT fk_ticket_created_by     FOREIGN KEY (created_by_user_id)    REFERENCES users (id),
                         CONSTRAINT fk_ticket_assigned_to    FOREIGN KEY (assigned_to_user_id)   REFERENCES users (id),
                         CONSTRAINT ck_ticket_status         CHECK (status   IN ('OPEN', 'IN_PROGRESS', 'PENDING', 'RESOLVED', 'CLOSED')),
                         CONSTRAINT ck_ticket_priority       CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT'))
);

CREATE INDEX idx_ticket_status_lab  ON tickets (status, laboratory_id);
CREATE INDEX idx_ticket_created_by  ON tickets (created_by_user_id);
CREATE INDEX idx_ticket_assigned_to ON tickets (assigned_to_user_id);
CREATE INDEX idx_ticket_priority    ON tickets (priority);

CREATE TABLE ticket_comments (
                                 id              BIGSERIAL   PRIMARY KEY,
                                 ticket_id       BIGINT      NOT NULL,
                                 author_user_id  BIGINT      NOT NULL,
                                 content         TEXT        NOT NULL,
                                 is_internal     BOOLEAN     NOT NULL DEFAULT FALSE,
                                 created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
                                 updated_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
                                 version         BIGINT      NOT NULL DEFAULT 0,

                                 CONSTRAINT fk_ticket_comment_ticket FOREIGN KEY (ticket_id)      REFERENCES tickets (id) ON DELETE CASCADE,
                                 CONSTRAINT fk_ticket_comment_author FOREIGN KEY (author_user_id) REFERENCES users (id)
);

CREATE INDEX idx_ticket_comment_ticket ON ticket_comments (ticket_id);
CREATE INDEX idx_ticket_comment_author ON ticket_comments (author_user_id);

CREATE TABLE laboratory_comments (
                                     id              BIGSERIAL   PRIMARY KEY,
                                     laboratory_id   BIGINT      NOT NULL,
                                     author_user_id  BIGINT      NOT NULL,
                                     content         TEXT        NOT NULL,
                                     rating          INTEGER,
                                     deleted         BOOLEAN     NOT NULL DEFAULT FALSE,
                                     edited_at       TIMESTAMP,
                                     created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
                                     updated_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
                                     version         BIGINT      NOT NULL DEFAULT 0,

                                     CONSTRAINT fk_lab_comment_lab       FOREIGN KEY (laboratory_id)   REFERENCES laboratories (id) ON DELETE CASCADE,
                                     CONSTRAINT fk_lab_comment_author    FOREIGN KEY (author_user_id)  REFERENCES users (id),
                                     CONSTRAINT ck_lab_comment_rating    CHECK (rating IS NULL OR rating BETWEEN 1 AND 5)
);

CREATE INDEX idx_lab_comment_lab     ON laboratory_comments (laboratory_id);
CREATE INDEX idx_lab_comment_author  ON laboratory_comments (author_user_id);
CREATE INDEX idx_lab_comment_deleted ON laboratory_comments (deleted);

CREATE TABLE comment_votes (
                               id          BIGSERIAL   PRIMARY KEY,
                               comment_id  BIGINT      NOT NULL,
                               user_id     BIGINT      NOT NULL,
                               vote_type   VARCHAR(10) NOT NULL DEFAULT 'UPVOTE',
                               created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),

                               CONSTRAINT fk_vote_comment  FOREIGN KEY (comment_id)  REFERENCES laboratory_comments (id) ON DELETE CASCADE,
                               CONSTRAINT fk_vote_user     FOREIGN KEY (user_id)     REFERENCES users (id) ON DELETE CASCADE,
                               CONSTRAINT uq_vote_user_comment UNIQUE (user_id, comment_id),
                               CONSTRAINT ck_vote_type     CHECK (vote_type IN ('UPVOTE', 'DOWNVOTE'))
);

CREATE INDEX idx_vote_comment ON comment_votes (comment_id);

CREATE TABLE audit_logs (
                            id              BIGSERIAL       PRIMARY KEY,
                            actor_user_id   BIGINT,
                            action          VARCHAR(100)    NOT NULL,
                            entity_type     VARCHAR(100)    NOT NULL,
                            entity_id       BIGINT          NOT NULL,
                            old_value       JSONB,
                            new_value       JSONB,
                            ip_address      VARCHAR(45),
                            timestamp       TIMESTAMP       NOT NULL DEFAULT NOW(),

                            CONSTRAINT fk_audit_actor FOREIGN KEY (actor_user_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE INDEX idx_audit_entity    ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_actor     ON audit_logs (actor_user_id);
CREATE INDEX idx_audit_timestamp ON audit_logs (timestamp);