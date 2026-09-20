-- =============================================================================
-- SongLog — dados iniciais para teste
--
-- Execute depois de 01-esquema.sql. Todos os usuários têm a senha songlog123.
-- "Kind of Blue" reproduz o exemplo real do projeto (nota da Ana 4,56; nota geral
-- 4,34); os demais álbuns, artistas e faixas são fictícios.
--
-- Só carregue em um banco vazio: os identificadores são fixos.
-- =============================================================================

USE songlog;

-- usuários (senha songlog123, guardada como hash PBKDF2)
INSERT INTO usuario (id, nome, email, senha_hash, data_cadastro) VALUES
  (1, 'Ana Ribeiro', 'ana@songlog.dev', 'pbkdf2$600000$3IFSlAfoBeqeULf5jk1+qQ==$T016NcM1x9PRAM4t0DsVOLfC0EnPpRBxCbOmWLkyOp8=', '2026-03-12'),
  (2, 'Bruno Tavares', 'bruno@songlog.dev', 'pbkdf2$600000$3IFSlAfoBeqeULf5jk1+qQ==$T016NcM1x9PRAM4t0DsVOLfC0EnPpRBxCbOmWLkyOp8=', '2026-04-02'),
  (3, 'Carla Mendes', 'carla@songlog.dev', 'pbkdf2$600000$3IFSlAfoBeqeULf5jk1+qQ==$T016NcM1x9PRAM4t0DsVOLfC0EnPpRBxCbOmWLkyOp8=', '2026-05-20');

-- álbuns
INSERT INTO album (id, titulo, artista, ano_lancamento, genero) VALUES
  (1, 'Kind of Blue', 'Miles Davis', 1959, 'Jazz'),
  (2, 'Rio Sem Margem', 'Marina Costa', 2019, 'MPB'),
  (3, 'Estação Lisboa', 'Duo Atlântico', 2015, 'Jazz'),
  (4, 'Concreto e Flor', 'Os Vendavais', 2022, 'Rock'),
  (5, 'Sal e Ferro', 'Tomás Aragão', 2011, 'Folk'),
  (6, 'Luz de Vidro', 'Helena Prado', 2020, 'Pop'),
  (7, 'Madrugada Elétrica', 'Coletivo Norte', 2008, 'Eletrônica'),
  (8, 'Cartas ao Vento', 'Inês Falcão', 2017, 'Samba');

-- faixas (duração em segundos; a numeração é sequencial dentro de cada álbum)
INSERT INTO faixa (id, album_id, numero, titulo, duracao_segundos) VALUES
  (1, 1, 1, 'So What', 562),
  (2, 1, 2, 'Freddie Freeloader', 586),
  (3, 1, 3, 'Blue in Green', 337),
  (4, 1, 4, 'All Blues', 693),
  (5, 1, 5, 'Flamenco Sketches', 566),
  (6, 2, 1, 'Cais de Areia', 252),
  (7, 2, 2, 'Fio d''Água', 228),
  (8, 2, 3, 'Maré Vazante', 303),
  (9, 2, 4, 'Carta Sem Endereço', 211),
  (10, 2, 5, 'Rio Sem Margem', 380),
  (11, 2, 6, 'Ponte de Papel', 245),
  (12, 3, 1, 'Partida às Seis', 344),
  (13, 3, 2, 'Elétrico 28', 276),
  (14, 3, 3, 'Miradouro', 372),
  (15, 3, 4, 'Fado Curto', 200),
  (16, 3, 5, 'Noite no Cais', 425),
  (17, 4, 1, 'Rachadura', 202),
  (18, 4, 2, 'Flor de Asfalto', 241),
  (19, 4, 3, 'Voltagem', 227),
  (20, 4, 4, 'Ruído Branco', 178),
  (21, 4, 5, 'Cidade Dormindo', 319),
  (22, 4, 6, 'Concreto', 273),
  (23, 4, 7, 'Flor', 192),
  (24, 5, 1, 'Âncora', 260),
  (25, 5, 2, 'Sal na Pele', 235),
  (26, 5, 3, 'Ferro Velho', 288),
  (27, 5, 4, 'O Farol', 330),
  (28, 5, 5, 'Lenha Úmida', 206),
  (29, 5, 6, 'Estrada de Volta', 299),
  (30, 6, 1, 'Vidro', 194),
  (31, 6, 2, 'Prisma', 218),
  (32, 6, 3, 'Refletir', 182),
  (33, 6, 4, 'Luz Baixa', 250),
  (34, 6, 5, 'Estática', 171),
  (35, 6, 6, 'Ao Contrário', 224),
  (36, 7, 1, 'Abertura Fria', 405),
  (37, 7, 2, 'Fase 2', 320),
  (38, 7, 3, 'Circuito', 430),
  (39, 7, 4, 'Sol Artificial', 362),
  (40, 7, 5, 'Desligar', 495),
  (41, 8, 1, 'Recado', 220),
  (42, 8, 2, 'Tamborim de Papel', 192),
  (43, 8, 3, 'Roda de Domingo', 265),
  (44, 8, 4, 'Saudade Elétrica', 238),
  (45, 8, 5, 'Cartas ao Vento', 310),
  (46, 8, 6, 'Terreiro de Luz', 242);

-- avaliações (a data tem segundos diferentes para a ordem de "últimas avaliações" ser estável)
INSERT INTO avaliacao (usuario_id, faixa_id, nota, comentario, data_avaliacao) VALUES
  (1, 1, 4.5, NULL, NOW() - INTERVAL 48 SECOND),
  (1, 2, 4.0, NULL, NOW() - INTERVAL 47 SECOND),
  (1, 3, 5.0, NULL, NOW() - INTERVAL 46 SECOND),
  (1, 4, 4.5, NULL, NOW() - INTERVAL 45 SECOND),
  (1, 5, 5.0, NULL, NOW() - INTERVAL 44 SECOND),
  (2, 1, 4.0, NULL, NOW() - INTERVAL 43 SECOND),
  (2, 2, 3.5, NULL, NOW() - INTERVAL 42 SECOND),
  (2, 3, 5.0, NULL, NOW() - INTERVAL 41 SECOND),
  (2, 4, 4.0, NULL, NOW() - INTERVAL 40 SECOND),
  (2, 5, 4.5, NULL, NOW() - INTERVAL 39 SECOND),
  (3, 1, 5.0, 'Perfeita.', NOW() - INTERVAL 38 SECOND),
  (3, 3, 4.5, NULL, NOW() - INTERVAL 37 SECOND),
  (2, 6, 4.0, NULL, NOW() - INTERVAL 36 SECOND),
  (2, 7, 4.5, NULL, NOW() - INTERVAL 35 SECOND),
  (2, 8, 3.5, NULL, NOW() - INTERVAL 34 SECOND),
  (2, 9, 4.0, NULL, NOW() - INTERVAL 33 SECOND),
  (2, 10, 5.0, NULL, NOW() - INTERVAL 32 SECOND),
  (2, 11, 4.0, NULL, NOW() - INTERVAL 31 SECOND),
  (3, 6, 4.5, NULL, NOW() - INTERVAL 30 SECOND),
  (3, 7, 4.0, NULL, NOW() - INTERVAL 29 SECOND),
  (3, 8, 4.0, NULL, NOW() - INTERVAL 28 SECOND),
  (3, 9, 3.5, NULL, NOW() - INTERVAL 27 SECOND),
  (3, 10, 5.0, NULL, NOW() - INTERVAL 26 SECOND),
  (3, 11, 4.5, NULL, NOW() - INTERVAL 25 SECOND),
  (1, 6, 4.0, NULL, NOW() - INTERVAL 24 SECOND),
  (1, 10, 5.0, NULL, NOW() - INTERVAL 23 SECOND),
  (1, 12, 4.5, NULL, NOW() - INTERVAL 22 SECOND),
  (1, 13, 4.0, NULL, NOW() - INTERVAL 21 SECOND),
  (1, 14, 5.0, NULL, NOW() - INTERVAL 20 SECOND),
  (1, 15, 3.5, NULL, NOW() - INTERVAL 19 SECOND),
  (1, 16, 4.5, NULL, NOW() - INTERVAL 18 SECOND),
  (2, 12, 4.0, NULL, NOW() - INTERVAL 17 SECOND),
  (2, 13, 4.0, NULL, NOW() - INTERVAL 16 SECOND),
  (2, 14, 4.5, NULL, NOW() - INTERVAL 15 SECOND),
  (2, 15, 3.0, NULL, NOW() - INTERVAL 14 SECOND),
  (2, 16, 4.0, NULL, NOW() - INTERVAL 13 SECOND),
  (2, 17, 3.5, NULL, NOW() - INTERVAL 12 SECOND),
  (2, 18, 4.0, NULL, NOW() - INTERVAL 11 SECOND),
  (2, 19, 3.0, NULL, NOW() - INTERVAL 10 SECOND),
  (2, 20, 3.0, NULL, NOW() - INTERVAL 9 SECOND),
  (2, 21, 4.0, NULL, NOW() - INTERVAL 8 SECOND),
  (2, 22, 3.5, NULL, NOW() - INTERVAL 7 SECOND),
  (2, 23, 3.0, NULL, NOW() - INTERVAL 6 SECOND),
  (3, 30, 3.0, NULL, NOW() - INTERVAL 5 SECOND),
  (3, 31, 3.5, NULL, NOW() - INTERVAL 4 SECOND),
  (3, 32, 3.0, NULL, NOW() - INTERVAL 3 SECOND),
  (3, 33, 4.0, NULL, NOW() - INTERVAL 2 SECOND),
  (3, 34, 3.0, NULL, NOW() - INTERVAL 1 SECOND),
  (3, 35, 3.5, NULL, NOW() - INTERVAL 0 SECOND);

-- diário de audição (datas relativas a hoje; reaudicao = 1 a partir da segunda audição do mesmo álbum)
INSERT INTO entrada_diario (usuario_id, album_id, data_audicao, comentario, reaudicao) VALUES
  (1, 2, CURDATE() - INTERVAL 45 DAY, 'Recomendação da Carla.', 0),
  (1, 1, CURDATE() - INTERVAL 30 DAY, 'Primeira vez, de ponta a ponta.', 0),
  (1, 3, CURDATE() - INTERVAL 9 DAY, 'O disco certo para a viagem de volta.', 0),
  (1, 1, CURDATE() - INTERVAL 2 DAY, 'Voltei só por Blue in Green.', 1),
  (2, 1, CURDATE() - INTERVAL 12 DAY, 'Clássico que eu ainda não tinha ouvido inteiro.', 0),
  (3, 6, CURDATE() - INTERVAL 5 DAY, NULL, 0);
