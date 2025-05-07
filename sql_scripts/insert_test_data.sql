
-- USER
INSERT INTO dbp.users (name, phone_number, address, email, password, birth_date, iban, roles)
VALUES
    ('João Silva', 932326731, 'Street A, 123', 'joao@example.com', '1234', '1985-05-10', 'PT50000201231234567890154', ARRAY['referee', 'player']),
    ('Maria Souza', 932326732, 'Street B, 456', 'maria@example.com', 'abcd', '1990-03-22', 'PT50000201231234567890155', ARRAY['admin', 'manager']),
    ('Carlos Lima', 932326733, 'Street C, 789', 'carlos@example.com', 'pass1', '1988-07-11', 'PT50000201231234567890156', ARRAY['referee', 'coach']),
    ('Ana Costa', 932326734, 'Street D, 101', 'ana@example.com', 'pass2', '1992-04-18', 'PT50000201231234567890157', ARRAY['referee', 'admin']),
    ('Bruno Dias', 932326735, 'Street E, 202', 'bruno@example.com', 'pass3', '1983-12-30', 'PT50000201231234567890158', ARRAY['admin', 'referee']),
    ('Sofia Martins', 932326736, 'Street F, 303', 'sofia@example.com', 'pass4', '1995-01-15', 'PT50000201231234567890159', ARRAY['player', 'captain']),
    ('Miguel Alves', 932326737, 'Street G, 404', 'miguel@example.com', 'pass5', '1987-09-07', 'PT50000201231234567890160', ARRAY['referee', 'player']),
    ('Laura Pinto', 932326738, 'Street H, 505', 'laura@example.com', 'pass6', '1993-11-23', 'PT50000201231234567890161', ARRAY['manager', 'admin']);

-- CATEGORY
insert into dbp.category (id, name)
values
    (1, 'Senior'),
    (2, 'Junior'),
    (3, 'Veteran');

-- CATEGORY_DIR

insert into dbp.category_dir (referee_id,start_date, end_date, category_id)
values
    (1,'2023/01/01', null, 1),
    (3,'2023-03-01', '2024-01-01', 2);


-- COMPETITION
insert into dbp.competition (id, competition_number, name, address, email, phone_number, location, association)
values
    (1, 1001, 'Torneio Nacional', 'Rua A, 123', 'torneio@example.com', '912345678', 'Lisboa', 'Federação A'),
    (2, 1002, 'Taça Regional', 'Rua B, 456', 'regional@example.com', '913456789', 'Porto', 'Federação B');

-- EQUIPMENT
insert into dbp.equipment (name)
values
    ('Bola Oficial'), ('Apito'), ('Cartões'), ('Uniformes');

-- COMPETITION_EQUIPMENT
insert into dbp.competition_equipment (competition_id, equipment_id)
values
     (1, 1), (1, 2), (2, 3), (2, 4);

-- CALL_LIST
insert into dbp.call_list (deadline, call_type, council_id, competition_id)
values
    ('2024-10-10', 'normal', 2, 1),
    ('2024-10-12', 'urgente', 5, 2);

-- POSITION
insert into dbp.position (name)
values
    ('Árbitro Central'), ('Assistente'), ('Quarto Árbitro');

-- ROLE
insert into dbp.role (id, name)
values
    (1, 'admin'), (2, 'Arbitration_Council'), (3, 'Referee');

-- FUNCTION
insert into dbp.function(id, name)
values
    (0, 'default'), (1, 'Juíz Árbitro'), (2, 'Delegado'), (3, 'Informático')
insert into dbp.function(id, name)
values
    (0, 'DEFAULT'), (1, 'JA'), (2, 'DEL'), (3, 'INF')

-- MATCH_DAY
insert into dbp.match_day (id, match_date, competition_id)
values

    (1, '2024-11-15', 1),
    (2, '2024-11-18', 2);

-- PARTICIPANT
insert into dbp.participant (call_list_id, match_day_id, referee_id, role_id, confirmation_status)
values
    (1, 1, 1, 1, 'waiting'),
    (1, 1, 3, 2, 'accepted'),
    (2, 2, 4, 1, 'declined');

-- SESSION
insert into dbp.session (id, start_time, end_time, match_day_id, competition_id_match_day)
values
    (1, '14:00', '15:30', 1, 1),
    (2, '16:00', '17:30', 2, 2);

-- SESSION_REFEREES
insert into dbp.session_referees (session_id, position_id, referee_id, match_day_id_session)
values
    (1, 1, 1, 1),
    (1, 2, 3, 1),
    (2, 1, 4, 2);

-- REPORT
insert into dbp.report (report_type, competition_id)
values
    ('final', 1),
    ('parcial', 2);
