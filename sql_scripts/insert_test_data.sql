
-- USER
insert into dbp.users (name, email, password, birth_date, iban, roles)
values
  ('João Silva', 'joao@example.com', '1234', '1985-05-10', 'PT50000201231234567890154', 'referee'),
  ('Maria Souza', 'maria@example.com', 'abcd', '1990-03-22', 'PT50000201231234567890155', 'admin'),
  ('Carlos Lima', 'carlos@example.com', 'pass1', '1988-07-11', 'PT50000201231234567890156', 'referee'),
  ('Ana Costa', 'ana@example.com', 'pass2', '1992-04-18', 'PT50000201231234567890157', 'referee'),
  ('Bruno Dias', 'bruno@example.com', 'pass3', '1983-12-30', 'PT50000201231234567890158', 'admin');

-- ADMIN
insert into dbp.admin (user_id)
values
    (2), (5);

-- REFEREE
insert into dbp.referee (user_id)
values
    (1), (3), (4);

-- ARBITRATION_COUNCIL
insert into dbp.arbitration_council (user_id)
values
    (2), (5);

-- CATEGORY
insert into dbp.category (id, name)
values
    (1, 'Senior'),
    (2, 'Junior'),
    (3, 'Veteran');

-- CATEGORY_DIR
insert into dbp.category_dir (referee_id, id, start_date, end_date, category_id)
values
     (1, 1, '2023-01-01', null, 1),
     (3, 2, '2023-03-01', '2024-01-01', 2);

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
    (1, 'Principal'), (2, 'Auxiliar'), (3, 'Reserva');

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
