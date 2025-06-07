-- PAPÉIS (ROLES)
INSERT INTO dbp.role (id, name)
VALUES
    (1, 'Administrador'),
    (2, 'Conselho de Arbitragem'),
    (3, 'Árbitro');

-- FUNÇÕES
INSERT INTO dbp.function(id, name)
VALUES
    (1, 'Juiz Árbitro'),
    (2, 'Delegado'),
    (3, 'Informador');

-- POSIÇÕES
INSERT INTO dbp.position (name)
VALUES
    ('Árbitro Central'),
    ('Assistente'),
    ('Quarto Árbitro');

-- CATEGORIAS
INSERT INTO dbp.category (id, name)
VALUES
    (1, 'Árbitro Internacional'),
    (2, 'Árbitro Nacional'),
    (3, 'Árbitro Regional'),
    (4, 'Juiz de Nível 1'),
    (5, 'Juiz de Nível 2'),
    (6, 'Juiz de Nível 3'),
    (7, 'Cronometrista');

-- EQUIPAMENTO
INSERT INTO dbp.equipment (name)
VALUES
    ('Bola Oficial'),
    ('Apito'),
    ('Cartões'),
    ('Uniformes');
