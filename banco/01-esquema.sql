-- =============================================================================
-- SongLog — esquema do banco de dados (MySQL 8.0 ou mais novo)
--
-- Como usar no MySQL Workbench:
--   File > Open SQL Script... > escolha este arquivo > execute tudo (ícone de raio).
-- Depois execute 02-dados-iniciais.sql. (O programa também faz isso sozinho na
-- primeira execução; use o Workbench se preferir ver ou alterar o banco.)
--
-- O script pode ser executado mais de uma vez sem apagar dados.
-- =============================================================================

CREATE DATABASE IF NOT EXISTS songlog
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE songlog;

-- -----------------------------------------------------------------------------
-- usuario: quem usa o sistema. A senha nunca é guardada em texto puro (RN11).
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario (
    id             INT          NOT NULL AUTO_INCREMENT,
    nome           VARCHAR(80)  NOT NULL,
    email          VARCHAR(120) NOT NULL,
    senha_hash     VARCHAR(255) NOT NULL COMMENT 'PBKDF2-HMAC-SHA256 com salt, gerado pelo programa',
    data_cadastro  DATE         NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuario_email UNIQUE (email)
) ENGINE = InnoDB COMMENT = 'Contas de usuário';

-- -----------------------------------------------------------------------------
-- album: um disco do catálogo. Título + artista + ano são únicos (RN02); a
-- collation utf8mb4_unicode_ci ignora maiúsculas e acentos nessa comparação.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS album (
    id              INT          NOT NULL AUTO_INCREMENT,
    titulo          VARCHAR(120) NOT NULL,
    artista         VARCHAR(120) NOT NULL,
    ano_lancamento  SMALLINT     NOT NULL,
    genero          VARCHAR(40)  NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_album UNIQUE (titulo, artista, ano_lancamento),
    CONSTRAINT ck_album_ano CHECK (ano_lancamento BETWEEN 1877 AND 2100)
) ENGINE = InnoDB COMMENT = 'Catálogo de álbuns';

-- -----------------------------------------------------------------------------
-- faixa: pertence a um álbum e é numerada em sequência (RN01). A duração é o
-- peso da faixa na nota ponderada (RN05). Excluir o álbum exclui as faixas (RN13).
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS faixa (
    id                INT          NOT NULL AUTO_INCREMENT,
    album_id          INT          NOT NULL,
    numero            SMALLINT     NOT NULL,
    titulo            VARCHAR(120) NOT NULL,
    duracao_segundos  INT          NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_faixa_numero UNIQUE (album_id, numero),
    CONSTRAINT ck_faixa_numero CHECK (numero >= 1),
    CONSTRAINT ck_faixa_duracao CHECK (duracao_segundos > 0),
    CONSTRAINT fk_faixa_album FOREIGN KEY (album_id) REFERENCES album (id) ON DELETE CASCADE
) ENGINE = InnoDB COMMENT = 'Faixas dos álbuns';

-- -----------------------------------------------------------------------------
-- avaliacao: a nota de um usuário para uma faixa. No máximo uma por par
-- usuário/faixa (RN04); nota de 0,5 a 5,0 em passos de 0,5 (RN03).
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS avaliacao (
    id              INT           NOT NULL AUTO_INCREMENT,
    usuario_id      INT           NOT NULL,
    faixa_id        INT           NOT NULL,
    nota            DECIMAL(2,1)  NOT NULL,
    comentario      VARCHAR(500)  NULL,
    data_avaliacao  DATETIME      NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_avaliacao_usuario_faixa UNIQUE (usuario_id, faixa_id),
    CONSTRAINT ck_avaliacao_nota CHECK (nota BETWEEN 0.5 AND 5.0),
    CONSTRAINT ck_avaliacao_passo CHECK (MOD(nota * 2, 1) = 0),
    CONSTRAINT fk_avaliacao_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    CONSTRAINT fk_avaliacao_faixa FOREIGN KEY (faixa_id) REFERENCES faixa (id) ON DELETE CASCADE
) ENGINE = InnoDB COMMENT = 'Nota de cada usuário para cada faixa';

-- -----------------------------------------------------------------------------
-- entrada_diario: "ouvi este álbum neste dia" (RN08). A partir da segunda
-- audição do mesmo álbum a entrada é marcada como reaudição (RN09).
-- A regra "a data não pode ser futura" (RN10) fica no programa: o MySQL não
-- aceita CURDATE() dentro de CHECK.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS entrada_diario (
    id            INT           NOT NULL AUTO_INCREMENT,
    usuario_id    INT           NOT NULL,
    album_id      INT           NOT NULL,
    data_audicao  DATE          NOT NULL,
    comentario    VARCHAR(500)  NULL,
    reaudicao     BOOLEAN       NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    INDEX ix_diario_usuario_data (usuario_id, data_audicao),
    CONSTRAINT fk_diario_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    CONSTRAINT fk_diario_album FOREIGN KEY (album_id) REFERENCES album (id) ON DELETE CASCADE
) ENGINE = InnoDB COMMENT = 'Diário de audição';

-- =============================================================================
-- Views: as notas calculadas no próprio banco.
-- =============================================================================

-- RN05: nota de cada usuário para cada álbum = média das notas das faixas que ele
-- avaliou, ponderada pela duração. "faixas_avaliadas < total_faixas" = avaliação parcial (RN06).
-- O "* 1e0" faz a divisão em ponto flutuante: entre colunas DECIMAL o MySQL arredondaria o
-- resultado em poucas casas (4,34858 em vez de 4,3485787...), e o programa calcula em double.
CREATE OR REPLACE VIEW vw_nota_usuario_album AS
SELECT a.usuario_id,
       f.album_id,
       (SUM(a.nota * f.duracao_segundos) * 1e0) / SUM(f.duracao_segundos) AS nota_ponderada,
       COUNT(*)                                                    AS faixas_avaliadas,
       (SELECT COUNT(*) FROM faixa t WHERE t.album_id = f.album_id) AS total_faixas
FROM avaliacao a
JOIN faixa f ON f.id = a.faixa_id
GROUP BY a.usuario_id, f.album_id;

-- RN07: nota geral do álbum = média simples das notas de quem avaliou TODAS as faixas.
CREATE OR REPLACE VIEW vw_nota_geral_album AS
SELECT album_id,
       AVG(nota_ponderada) AS nota_geral,
       COUNT(*)            AS avaliadores
FROM vw_nota_usuario_album
WHERE faixas_avaliadas = total_faixas
GROUP BY album_id;
