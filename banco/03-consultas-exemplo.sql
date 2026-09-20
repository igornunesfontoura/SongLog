-- =============================================================================
-- SongLog — consultas de exemplo (para experimentar no MySQL Workbench)
-- =============================================================================
USE songlog;

-- 1) Catálogo com quantidade de faixas, duração total e nota geral (RN07)
SELECT al.titulo, al.artista, al.ano_lancamento, al.genero,
       COUNT(f.id)                                   AS faixas,
       SEC_TO_TIME(SUM(f.duracao_segundos))          AS duracao,
       ROUND(ng.nota_geral, 2)                       AS nota_geral,
       ng.avaliadores
FROM album al
JOIN faixa f ON f.album_id = al.id
LEFT JOIN vw_nota_geral_album ng ON ng.album_id = al.id
GROUP BY al.id, ng.nota_geral, ng.avaliadores
ORDER BY al.titulo;

-- 2) Nota de cada usuário para cada álbum, marcando as avaliações parciais (RN05 e RN06)
SELECT u.nome, al.titulo,
       ROUND(v.nota_ponderada, 2)                                   AS nota,
       CONCAT(v.faixas_avaliadas, ' de ', v.total_faixas, ' faixas') AS avaliacao,
       IF(v.faixas_avaliadas < v.total_faixas, 'PARCIAL', 'completa') AS situacao
FROM vw_nota_usuario_album v
JOIN usuario u ON u.id = v.usuario_id
JOIN album al ON al.id = v.album_id
ORDER BY al.titulo, u.nome;

-- 3) Faixa por faixa de "Kind of Blue" com a nota de cada usuário
SELECT f.numero, f.titulo, SEC_TO_TIME(f.duracao_segundos) AS duracao,
       MAX(IF(a.usuario_id = 1, a.nota, NULL)) AS ana,
       MAX(IF(a.usuario_id = 2, a.nota, NULL)) AS bruno,
       MAX(IF(a.usuario_id = 3, a.nota, NULL)) AS carla
FROM faixa f
LEFT JOIN avaliacao a ON a.faixa_id = f.id
WHERE f.album_id = 1
GROUP BY f.id
ORDER BY f.numero;

-- 4) Diário de audição da Ana, mais recente primeiro, com reaudições
SELECT d.data_audicao, al.titulo, al.artista,
       IF(d.reaudicao, 'REAUDIÇÃO', '') AS tag, d.comentario
FROM entrada_diario d
JOIN album al ON al.id = d.album_id
WHERE d.usuario_id = 1
ORDER BY d.data_audicao DESC;

-- 5) Estatísticas do perfil de cada usuário (RF09)
SELECT u.nome,
       (SELECT COUNT(DISTINCT d.album_id) FROM entrada_diario d WHERE d.usuario_id = u.id) AS albuns_ouvidos,
       (SELECT COUNT(*) FROM avaliacao a WHERE a.usuario_id = u.id)                        AS faixas_avaliadas,
       (SELECT ROUND(AVG(a.nota), 2) FROM avaliacao a WHERE a.usuario_id = u.id)           AS nota_media_dada,
       (SELECT COUNT(*) FROM entrada_diario d WHERE d.usuario_id = u.id AND d.reaudicao)   AS reaudicoes
FROM usuario u;

-- 6) O que acontece ao excluir um álbum (RN13): rode dentro de uma transação e desfaça
START TRANSACTION;
SELECT (SELECT COUNT(*) FROM faixa WHERE album_id = 1)          AS faixas_antes,
       (SELECT COUNT(*) FROM entrada_diario WHERE album_id = 1) AS entradas_antes;
DELETE FROM album WHERE id = 1;
SELECT (SELECT COUNT(*) FROM faixa WHERE album_id = 1)          AS faixas_depois,
       (SELECT COUNT(*) FROM entrada_diario WHERE album_id = 1) AS entradas_depois;
ROLLBACK;
