-- USERS
INSERT INTO dbp.users (name, phone_number, address, email, password_validation, birth_date, iban)
VALUES
    ('João Silva', '932326731', 'Street A, 123', 'joao@example.com', '1234', '1985-05-10', 'PT50000201231234567890154'),
    ('Maria Souza', '932326732', 'Street B, 456', 'maria@example.com', 'abcd', '1990-03-22', 'PT50000201231234567890155'),
    ('Carlos Lima', '932326733', 'Street C, 789', 'carlos@example.com', 'pass1', '1988-07-11', 'PT50000201231234567890156'),
    ('Ana Costa', '932326734', 'Street D, 101', 'ana@example.com', 'pass2', '1992-04-18', 'PT50000201231234567890157'),
    ('Bruno Dias', '932326735', 'Street E, 202', 'bruno@example.com', 'pass3', '1983-12-30', 'PT50000201231234567890158'),
    ('Sofia Martins', '932326736', 'Street F, 303', 'sofia@example.com', 'pass4', '1995-01-15', 'PT50000201231234567890159'),
    ('Miguel Alves', '932326737', 'Street G, 404', 'miguel@example.com', 'pass5', '1987-09-07', 'PT50000201231234567890160'),
    ('Laura Pinto', '932326738', 'Street H, 505', 'laura@example.com', 'pass6', '1993-11-23', 'PT50000201231234567890161');

-- USERS_ROLES
INSERT INTO dbp.role (id, name) VALUES (1, 'Admin'), (2, 'Arbitration_Council'), (3, 'Referee');

INSERT INTO dbp.users_roles (user_id, role_id) VALUES
                                                   (1, 2), (1, 3),
                                                   (2, 1), (2, 3),
                                                   (3, 2), (3, 3),
                                                   (4, 2), (4, 3),
                                                   (5, 2), (5, 3),
                                                   (6, 1), (6, 3),
                                                   (7, 2), (7, 3),
                                                   (8, 2), (8, 3);

INSERT INTO dbp.tokens (token_validation, user_id, created_at, last_used_at)
VALUES
    ('token-abc123', 1, 1748096441, 1748100041),
    ('token-def456', 2, 1748096441, 1748103641),
    ('token-ghi789', 3, 1748096441, 1748100041),
    ('token-jkl012', 4, 1748096441, 1748100041);

-- CATEGORY
INSERT INTO dbp.category (id, name)
VALUES (1, 'AI'), (2, 'AN'), (3, 'AR'), (4, 'J1'), (5, 'J2'), (6, 'J3'), (7, 'C');

-- CATEGORY_DIR
INSERT INTO dbp.category_dir (user_id, start_date, end_date, category_id)
VALUES
    (1,'2023-01-01', null, 1),
    (3,'2023-03-01', null, 2),
    (4,'2023-01-01', null, 1);

-- COMPETITION
INSERT INTO dbp.competition (competition_number, name, address, email, phone_number, location, association)
VALUES
    (1001, 'Torneio Nacional', 'Rua A, 123', 'torneio@example.com', '912345678', 'Lisboa', 'Federação A'),
    (1002, 'Taça Regional', 'Rua B, 456', 'regional@example.com', '913456789', 'Porto', 'Federação B');

-- EQUIPMENT
INSERT INTO dbp.equipment (name)
VALUES ('Pólo Azul ANL ou Preto Meeting Lisboa'), ('Calças Brancas e Ténis Brancos'),
       ('Pólo / T-Shirt Azul Escuro'), ('Calças Brancas e Ténis Brancos');

-- COMPETITION_EQUIPMENT
INSERT INTO dbp.competition_equipment (competition_id, equipment_id)
VALUES (1001, 1), (1001, 2), (1002, 3), (1002, 4);

-- CALL_LIST
INSERT INTO dbp.call_list (deadline, call_type, user_id, competition_id)
VALUES
    ('2024-10-10', 'callList', 2, 1001),
    ('2024-10-12', 'callList', 5, 1002);

-- POSITION
INSERT INTO dbp.position (name)
VALUES ('Árbitro Central'), ('Assistente'), ('Quarto Árbitro');

-- FUNCTION
INSERT INTO dbp.function(id, name)
VALUES (0, 'DEFAULT'), (1, 'JA'), (2, 'DEL'), (3, 'INF');

-- MATCH_DAY
INSERT INTO dbp.match_day (id, match_date, competition_id)
VALUES
    (1, '2024-11-15', 1001),
    (2, '2024-11-18', 1002);

-- PARTICIPANT
INSERT INTO dbp.participant (call_list_id, match_day_id, competition_id_match_day, user_id, function_id, confirmation_status)
VALUES
    (1, 1, 1001, 1, 1, 'waiting'),
    (1, 1, 1001, 3, 2, 'accepted'),
    (2, 2, 1002, 4, 1, 'declined');

-- SESSION
INSERT INTO dbp.session (id, start_time, end_time, match_day_id, competition_id_match_day)
VALUES
    (1, '14:00', '15:30', 1, 1001),
    (2, '16:00', '17:30', 2, 1002);

-- SESSION_REFEREES
INSERT INTO dbp.session_referees (session_id, position_id, user_id, match_day_id_session, competition_id_match_day)
VALUES
    (1, 1, 1, 1, 1001),
    (1, 2, 3, 1, 1001),
    (2, 1, 4, 2, 1002);

-- REPORT
INSERT INTO dbp.report (id, report_type, competition_id)
VALUES
    ('rep1', 'final', 1001),
    ('rep2', 'parcial', 1002);

insert into dbp.payment_values (name, value)
values
    ('presence', 25.00),
    ('weekday', 15.50),
    ('transportation', 8.75),
    ('meals', 12.30),
    ('jury-ref', 20.00);

