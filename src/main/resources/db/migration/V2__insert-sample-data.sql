-- ========================================
-- V2__insert_sample_data.sql
-- ========================================
-- Senhas: todas são "password123" (bcrypt)
-- IDs não são referenciados diretamente — todas as FKs usam subqueries
-- para evitar problemas com sequências BIGSERIAL após rollbacks.

-- ----------------------------------------
-- USERS
-- ----------------------------------------

INSERT INTO users (username, email, password_hash, name, role, active) VALUES
                                                                           ('joao.silva',   'joao.silva@uni.edu',   '$2a$10$7EqJtq98hPqEX7fNZaFWoOe2j1XnRSJiyz7VMF5yY5QZdlCNLKc1G', 'João Silva',      'STUDENT',    TRUE),
                                                                           ('maria.souza',  'maria.souza@uni.edu',  '$2a$10$7EqJtq98hPqEX7fNZaFWoOe2j1XnRSJiyz7VMF5yY5QZdlCNLKc1G', 'Maria Souza',     'STUDENT',    TRUE),
                                                                           ('carlos.lima',  'carlos.lima@uni.edu',  '$2a$10$7EqJtq98hPqEX7fNZaFWoOe2j1XnRSJiyz7VMF5yY5QZdlCNLKc1G', 'Carlos Lima',     'PROFESSOR',  TRUE),
                                                                           ('ana.rocha',    'ana.rocha@uni.edu',    '$2a$10$7EqJtq98hPqEX7fNZaFWoOe2j1XnRSJiyz7VMF5yY5QZdlCNLKc1G', 'Ana Rocha',       'PROFESSOR',  TRUE),
                                                                           ('paula.mendes', 'paula.mendes@uni.edu', '$2a$10$7EqJtq98hPqEX7fNZaFWoOe2j1XnRSJiyz7VMF5yY5QZdlCNLKc1G', 'Paula Mendes',    'SECRETARY',  TRUE),
                                                                           ('lucas.tech',   'lucas.tech@uni.edu',   '$2a$10$7EqJtq98hPqEX7fNZaFWoOe2j1XnRSJiyz7VMF5yY5QZdlCNLKc1G', 'Lucas Técnico',   'TECHNICIAN', TRUE),
                                                                           ('ativo.false',  'inativo@uni.edu',      '$2a$10$7EqJtq98hPqEX7fNZaFWoOe2j1XnRSJiyz7VMF5yY5QZdlCNLKc1G', 'Usuário Inativo', 'STUDENT',    FALSE);

-- ----------------------------------------
-- LABORATORIES
-- ----------------------------------------

INSERT INTO laboratories (name, code, description, computer_count, capacity, building, floor, active) VALUES
                                                                                                          ('Laboratório de Redes',          'LAB-REDES',   'Laboratório para aulas de redes e infraestrutura.',   30, 35, 'Bloco A', '1º',     TRUE),
                                                                                                          ('Laboratório de Software',       'LAB-SW',      'Laboratório para desenvolvimento de software.',        25, 30, 'Bloco A', '2º',     TRUE),
                                                                                                          ('Laboratório de Hardware',       'LAB-HW',      'Laboratório para montagem e manutenção de hardware.',  20, 25, 'Bloco B', 'Térreo', TRUE),
                                                                                                          ('Laboratório de Banco de Dados', 'LAB-BD',      'Laboratório para aulas de banco de dados.',            30, 30, 'Bloco B', '1º',     TRUE),
                                                                                                          ('Laboratório Inativo',           'LAB-INATIVO', 'Laboratório fora de operação.',                         0, 20, 'Bloco C', '2º',     FALSE);

-- ----------------------------------------
-- RESERVATION GROUPS
-- ----------------------------------------

INSERT INTO reservation_groups (created_by_user_id, laboratory_id, total_occurrences, active_occurrences) VALUES
                                                                                                              ((SELECT id FROM users       WHERE username = 'carlos.lima'),
                                                                                                               (SELECT id FROM laboratories WHERE code     = 'LAB-SW'),
                                                                                                               3, 3),
                                                                                                              ((SELECT id FROM users       WHERE username = 'ana.rocha'),
                                                                                                               (SELECT id FROM laboratories WHERE code     = 'LAB-BD'),
                                                                                                               2, 1);

-- ----------------------------------------
-- RESERVATIONS — individuais
-- ----------------------------------------

INSERT INTO reservations
(laboratory_id, requested_by_user_id, approved_by_user_id, reservation_group_id,
 reservation_date, purpose, status, total_duration_minutes, occurrence_number)
VALUES
    -- Pendente: João / LAB-REDES
    ((SELECT id FROM laboratories WHERE code = 'LAB-REDES'),
     (SELECT id FROM users        WHERE username = 'joao.silva'),
     NULL, NULL,
     CURRENT_DATE + 3, 'Prática de configuração de switches', 'PENDING', 90, NULL),

    -- Aprovada: Maria / LAB-SW
    ((SELECT id FROM laboratories WHERE code = 'LAB-SW'),
     (SELECT id FROM users        WHERE username = 'maria.souza'),
     (SELECT id FROM users        WHERE username = 'paula.mendes'),
     NULL,
     CURRENT_DATE + 5, 'Projeto final de engenharia de software', 'APPROVED', 120, NULL),

    -- Rejeitada: João / LAB-HW
    ((SELECT id FROM laboratories WHERE code = 'LAB-HW'),
     (SELECT id FROM users        WHERE username = 'joao.silva'),
     (SELECT id FROM users        WHERE username = 'paula.mendes'),
     NULL,
     CURRENT_DATE + 1, 'Desmontagem de computadores', 'REJECTED', 60, NULL),

    -- Cancelada: Maria / LAB-BD
    ((SELECT id FROM laboratories WHERE code = 'LAB-BD'),
     (SELECT id FROM users        WHERE username = 'maria.souza'),
     NULL, NULL,
     CURRENT_DATE + 2, 'Estudo de indexação', 'CANCELLED', 60, NULL),

    -- Concluída: Carlos / LAB-REDES
    ((SELECT id FROM laboratories WHERE code = 'LAB-REDES'),
     (SELECT id FROM users        WHERE username = 'carlos.lima'),
     (SELECT id FROM users        WHERE username = 'paula.mendes'),
     NULL,
     CURRENT_DATE - 7, 'Aula de roteamento OSPF', 'COMPLETED', 100, NULL);

-- ----------------------------------------
-- RESERVATIONS — recorrentes grupo 1 (Carlos / LAB-SW)
-- ----------------------------------------

INSERT INTO reservations
(laboratory_id, requested_by_user_id, approved_by_user_id, reservation_group_id,
 reservation_date, purpose, status, total_duration_minutes, occurrence_number)
VALUES
    ((SELECT id FROM laboratories   WHERE code     = 'LAB-SW'),
     (SELECT id FROM users          WHERE username = 'carlos.lima'),
     (SELECT id FROM users          WHERE username = 'paula.mendes'),
     (SELECT id FROM reservation_groups WHERE created_by_user_id = (SELECT id FROM users WHERE username = 'carlos.lima')
                                          AND laboratory_id      = (SELECT id FROM laboratories WHERE code = 'LAB-SW')),
     CURRENT_DATE +  7, 'Aula semanal de POO', 'APPROVED', 110, 1),

    ((SELECT id FROM laboratories   WHERE code     = 'LAB-SW'),
     (SELECT id FROM users          WHERE username = 'carlos.lima'),
     (SELECT id FROM users          WHERE username = 'paula.mendes'),
     (SELECT id FROM reservation_groups WHERE created_by_user_id = (SELECT id FROM users WHERE username = 'carlos.lima')
                                          AND laboratory_id      = (SELECT id FROM laboratories WHERE code = 'LAB-SW')),
     CURRENT_DATE + 14, 'Aula semanal de POO', 'APPROVED', 110, 2),

    ((SELECT id FROM laboratories   WHERE code     = 'LAB-SW'),
     (SELECT id FROM users          WHERE username = 'carlos.lima'),
     (SELECT id FROM users          WHERE username = 'paula.mendes'),
     (SELECT id FROM reservation_groups WHERE created_by_user_id = (SELECT id FROM users WHERE username = 'carlos.lima')
                                          AND laboratory_id      = (SELECT id FROM laboratories WHERE code = 'LAB-SW')),
     CURRENT_DATE + 21, 'Aula semanal de POO', 'APPROVED', 110, 3);

-- ----------------------------------------
-- RESERVATIONS — recorrentes grupo 2 (Ana / LAB-BD)
-- ----------------------------------------

INSERT INTO reservations
(laboratory_id, requested_by_user_id, approved_by_user_id, reservation_group_id,
 reservation_date, purpose, status, total_duration_minutes, occurrence_number)
VALUES
    ((SELECT id FROM laboratories   WHERE code     = 'LAB-BD'),
     (SELECT id FROM users          WHERE username = 'ana.rocha'),
     (SELECT id FROM users          WHERE username = 'paula.mendes'),
     (SELECT id FROM reservation_groups WHERE created_by_user_id = (SELECT id FROM users WHERE username = 'ana.rocha')
                                          AND laboratory_id      = (SELECT id FROM laboratories WHERE code = 'LAB-BD')),
     CURRENT_DATE +  7, 'Aula de modelagem relacional', 'APPROVED',  90, 1),

    ((SELECT id FROM laboratories   WHERE code     = 'LAB-BD'),
     (SELECT id FROM users          WHERE username = 'ana.rocha'),
     (SELECT id FROM users          WHERE username = 'paula.mendes'),
     (SELECT id FROM reservation_groups WHERE created_by_user_id = (SELECT id FROM users WHERE username = 'ana.rocha')
                                          AND laboratory_id      = (SELECT id FROM laboratories WHERE code = 'LAB-BD')),
     CURRENT_DATE + 14, 'Aula de modelagem relacional', 'CANCELLED', 90, 2);

-- ----------------------------------------
-- RESERVATION TIME BLOCKS
-- ----------------------------------------
-- As reservas são identificadas pela combinação (requested_by, date, purpose) — única por design dos dados.

-- Reserva 1 — João / LAB-REDES / PENDING (1 bloco, 90 min)
INSERT INTO reservation_time_blocks (reservation_id, start_time, end_time, block_order, duration_minutes)
SELECT id, '14:00', '15:30', 1, 90 FROM reservations
WHERE requested_by_user_id = (SELECT id FROM users WHERE username = 'joao.silva')
  AND status = 'PENDING';

-- Reserva 2 — Maria / LAB-SW / APPROVED (2 blocos não-contíguos, 120 min)
INSERT INTO reservation_time_blocks (reservation_id, start_time, end_time, block_order, duration_minutes)
SELECT id, '08:00', '09:00', 1, 60 FROM reservations
WHERE requested_by_user_id = (SELECT id FROM users WHERE username = 'maria.souza')
  AND status = 'APPROVED' AND reservation_group_id IS NULL;

INSERT INTO reservation_time_blocks (reservation_id, start_time, end_time, block_order, duration_minutes)
SELECT id, '10:00', '11:00', 2, 60 FROM reservations
WHERE requested_by_user_id = (SELECT id FROM users WHERE username = 'maria.souza')
  AND status = 'APPROVED' AND reservation_group_id IS NULL;

-- Reserva 3 — João / LAB-HW / REJECTED (1 bloco, 60 min)
INSERT INTO reservation_time_blocks (reservation_id, start_time, end_time, block_order, duration_minutes)
SELECT id, '13:00', '14:00', 1, 60 FROM reservations
WHERE requested_by_user_id = (SELECT id FROM users WHERE username = 'joao.silva')
  AND status = 'REJECTED';

-- Reserva 4 — Maria / LAB-BD / CANCELLED (1 bloco, 60 min)
INSERT INTO reservation_time_blocks (reservation_id, start_time, end_time, block_order, duration_minutes)
SELECT id, '16:00', '17:00', 1, 60 FROM reservations
WHERE requested_by_user_id = (SELECT id FROM users WHERE username = 'maria.souza')
  AND status = 'CANCELLED';

-- Reserva 5 — Carlos / LAB-REDES / COMPLETED (2 blocos, 100 min)
INSERT INTO reservation_time_blocks (reservation_id, start_time, end_time, block_order, duration_minutes)
SELECT id, '09:00', '10:00', 1, 60 FROM reservations
WHERE requested_by_user_id = (SELECT id FROM users WHERE username = 'carlos.lima')
  AND status = 'COMPLETED';

INSERT INTO reservation_time_blocks (reservation_id, start_time, end_time, block_order, duration_minutes)
SELECT id, '10:30', '11:10', 2, 40 FROM reservations
WHERE requested_by_user_id = (SELECT id FROM users WHERE username = 'carlos.lima')
  AND status = 'COMPLETED';

-- Reservas recorrentes grupo 1 — Carlos / LAB-SW (3 ocorrências, 2 blocos cada)
INSERT INTO reservation_time_blocks (reservation_id, start_time, end_time, block_order, duration_minutes)
SELECT id, '08:00', '09:00', 1, 60 FROM reservations
WHERE requested_by_user_id = (SELECT id FROM users WHERE username = 'carlos.lima')
  AND reservation_group_id IS NOT NULL AND status = 'APPROVED';

INSERT INTO reservation_time_blocks (reservation_id, start_time, end_time, block_order, duration_minutes)
SELECT id, '09:30', '10:20', 2, 50 FROM reservations
WHERE requested_by_user_id = (SELECT id FROM users WHERE username = 'carlos.lima')
  AND reservation_group_id IS NOT NULL AND status = 'APPROVED';

-- Reservas recorrentes grupo 2 — Ana / LAB-BD (2 ocorrências, 1 bloco cada)
INSERT INTO reservation_time_blocks (reservation_id, start_time, end_time, block_order, duration_minutes)
SELECT id, '14:00', '15:30', 1, 90 FROM reservations
WHERE requested_by_user_id = (SELECT id FROM users WHERE username = 'ana.rocha')
  AND reservation_group_id IS NOT NULL;

-- ----------------------------------------
-- TICKETS
-- ----------------------------------------

INSERT INTO tickets (laboratory_id, created_by_user_id, assigned_to_user_id, title, description, status, priority) VALUES
                                                                                                                       ((SELECT id FROM laboratories WHERE code = 'LAB-REDES'),
                                                                                                                        (SELECT id FROM users WHERE username = 'joao.silva'),
                                                                                                                        (SELECT id FROM users WHERE username = 'lucas.tech'),
                                                                                                                        'Computador 05 não liga', 'O computador 05 do LAB-REDES não inicia após tentativas.', 'IN_PROGRESS', 'HIGH'),

                                                                                                                       ((SELECT id FROM laboratories WHERE code = 'LAB-SW'),
                                                                                                                        (SELECT id FROM users WHERE username = 'maria.souza'),
                                                                                                                        NULL,
                                                                                                                        'Falta de mouse em 3 estações', 'Três estações do LAB-SW estão sem mouse.', 'OPEN', 'MEDIUM'),

                                                                                                                       ((SELECT id FROM laboratories WHERE code = 'LAB-HW'),
                                                                                                                        (SELECT id FROM users WHERE username = 'carlos.lima'),
                                                                                                                        (SELECT id FROM users WHERE username = 'lucas.tech'),
                                                                                                                        'Ar-condicionado com defeito', 'O ar-condicionado do LAB-HW está desligando sozinho.', 'PENDING', 'URGENT'),

                                                                                                                       ((SELECT id FROM laboratories WHERE code = 'LAB-BD'),
                                                                                                                        (SELECT id FROM users WHERE username = 'ana.rocha'),
                                                                                                                        (SELECT id FROM users WHERE username = 'lucas.tech'),
                                                                                                                        'Atualização do PostgreSQL', 'Solicito atualização do PostgreSQL para versão 16.', 'RESOLVED', 'LOW'),

                                                                                                                       ((SELECT id FROM laboratories WHERE code = 'LAB-REDES'),
                                                                                                                        (SELECT id FROM users WHERE username = 'maria.souza'),
                                                                                                                        NULL,
                                                                                                                        'Projetor sem sinal HDMI', 'O projetor do LAB-REDES não detecta sinal HDMI.', 'OPEN', 'HIGH');

-- ----------------------------------------
-- TICKET COMMENTS
-- ----------------------------------------

INSERT INTO ticket_comments (ticket_id, author_user_id, content, is_internal)
SELECT t.id,
       (SELECT id FROM users WHERE username = 'joao.silva'),
       'O problema ocorre desde segunda-feira.',
       FALSE
FROM tickets t WHERE t.title = 'Computador 05 não liga';

INSERT INTO ticket_comments (ticket_id, author_user_id, content, is_internal)
SELECT t.id,
       (SELECT id FROM users WHERE username = 'lucas.tech'),
       'Identificado problema na fonte de alimentação.',
       FALSE
FROM tickets t WHERE t.title = 'Computador 05 não liga';

INSERT INTO ticket_comments (ticket_id, author_user_id, content, is_internal)
SELECT t.id,
       (SELECT id FROM users WHERE username = 'lucas.tech'),
       'Peça solicitada ao almoxarifado. Aguardando.',
       TRUE
FROM tickets t WHERE t.title = 'Computador 05 não liga';

INSERT INTO ticket_comments (ticket_id, author_user_id, content, is_internal)
SELECT t.id,
       (SELECT id FROM users WHERE username = 'carlos.lima'),
       'O problema piora à tarde com o calor.',
       FALSE
FROM tickets t WHERE t.title = 'Ar-condicionado com defeito';

INSERT INTO ticket_comments (ticket_id, author_user_id, content, is_internal)
SELECT t.id,
       (SELECT id FROM users WHERE username = 'lucas.tech'),
       'Técnico agendado para amanhã às 09h.',
       TRUE
FROM tickets t WHERE t.title = 'Ar-condicionado com defeito';

INSERT INTO ticket_comments (ticket_id, author_user_id, content, is_internal)
SELECT t.id,
       (SELECT id FROM users WHERE username = 'ana.rocha'),
       'Atualização concluída com sucesso em todos os PCs.',
       FALSE
FROM tickets t WHERE t.title = 'Atualização do PostgreSQL';

-- ----------------------------------------
-- LABORATORY COMMENTS
-- ----------------------------------------

INSERT INTO laboratory_comments (laboratory_id, author_user_id, content) VALUES
                                                                             ((SELECT id FROM laboratories WHERE code = 'LAB-REDES'),
                                                                              (SELECT id FROM users        WHERE username = 'joao.silva'),
                                                                              'Ótimo laboratório, computadores rápidos e bem configurados.'),

                                                                             ((SELECT id FROM laboratories WHERE code = 'LAB-REDES'),
                                                                              (SELECT id FROM users        WHERE username = 'maria.souza'),
                                                                              'Ambiente agradável, mas poderia ter mais tomadas.'),

                                                                             ((SELECT id FROM laboratories WHERE code = 'LAB-SW'),
                                                                              (SELECT id FROM users        WHERE username = 'joao.silva'),
                                                                              'Muito bom para desenvolvimento, IDEs sempre atualizadas.'),

                                                                             ((SELECT id FROM laboratories WHERE code = 'LAB-SW'),
                                                                              (SELECT id FROM users        WHERE username = 'carlos.lima'),
                                                                              'Excelente estrutura para as aulas.'),

                                                                             ((SELECT id FROM laboratories WHERE code = 'LAB-HW'),
                                                                              (SELECT id FROM users        WHERE username = 'maria.souza'),
                                                                              'Ferramentas básicas disponíveis, mas faltam algumas peças.'),

                                                                             ((SELECT id FROM laboratories WHERE code = 'LAB-BD'),
                                                                              (SELECT id FROM users        WHERE username = 'ana.rocha'),
                                                                              'Banco de dados bem configurado. Ideal para as aulas.');
-- ----------------------------------------
-- COMMENT VOTES
-- ----------------------------------------

INSERT INTO comment_votes (comment_id, user_id, vote_type)
SELECT c.id, (SELECT id FROM users WHERE username = 'maria.souza'), 'UPVOTE'
FROM laboratory_comments c
WHERE c.author_user_id = (SELECT id FROM users WHERE username = 'joao.silva')
  AND c.laboratory_id  = (SELECT id FROM laboratories WHERE code = 'LAB-REDES');

INSERT INTO comment_votes (comment_id, user_id, vote_type)
SELECT c.id, (SELECT id FROM users WHERE username = 'carlos.lima'), 'UPVOTE'
FROM laboratory_comments c
WHERE c.author_user_id = (SELECT id FROM users WHERE username = 'joao.silva')
  AND c.laboratory_id  = (SELECT id FROM laboratories WHERE code = 'LAB-REDES');

INSERT INTO comment_votes (comment_id, user_id, vote_type)
SELECT c.id, (SELECT id FROM users WHERE username = 'joao.silva'), 'UPVOTE'
FROM laboratory_comments c
WHERE c.author_user_id = (SELECT id FROM users WHERE username = 'maria.souza')
  AND c.laboratory_id  = (SELECT id FROM laboratories WHERE code = 'LAB-REDES');

INSERT INTO comment_votes (comment_id, user_id, vote_type)
SELECT c.id, (SELECT id FROM users WHERE username = 'maria.souza'), 'UPVOTE'
FROM laboratory_comments c
WHERE c.author_user_id = (SELECT id FROM users WHERE username = 'joao.silva')
  AND c.laboratory_id  = (SELECT id FROM laboratories WHERE code = 'LAB-SW');

INSERT INTO comment_votes (comment_id, user_id, vote_type)
SELECT c.id, (SELECT id FROM users WHERE username = 'ana.rocha'), 'UPVOTE'
FROM laboratory_comments c
WHERE c.author_user_id = (SELECT id FROM users WHERE username = 'joao.silva')
  AND c.laboratory_id  = (SELECT id FROM laboratories WHERE code = 'LAB-SW');

INSERT INTO comment_votes (comment_id, user_id, vote_type)
SELECT c.id, (SELECT id FROM users WHERE username = 'joao.silva'), 'UPVOTE'
FROM laboratory_comments c
WHERE c.author_user_id = (SELECT id FROM users WHERE username = 'carlos.lima')
  AND c.laboratory_id  = (SELECT id FROM laboratories WHERE code = 'LAB-SW');

INSERT INTO comment_votes (comment_id, user_id, vote_type)
SELECT c.id, (SELECT id FROM users WHERE username = 'joao.silva'), 'DOWNVOTE'
FROM laboratory_comments c
WHERE c.author_user_id = (SELECT id FROM users WHERE username = 'maria.souza')
  AND c.laboratory_id  = (SELECT id FROM laboratories WHERE code = 'LAB-HW');

INSERT INTO comment_votes (comment_id, user_id, vote_type)
SELECT c.id, (SELECT id FROM users WHERE username = 'maria.souza'), 'UPVOTE'
FROM laboratory_comments c
WHERE c.author_user_id = (SELECT id FROM users WHERE username = 'ana.rocha')
  AND c.laboratory_id  = (SELECT id FROM laboratories WHERE code = 'LAB-BD');