package br.com.songlog.repositorio.jdbc;

import br.com.songlog.banco.Banco;
import br.com.songlog.model.Album;
import br.com.songlog.model.Faixa;
import br.com.songlog.model.NotaGeral;
import br.com.songlog.repositorio.AlbumRepositorio;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AlbumJdbc implements AlbumRepositorio {

    private final Banco banco;

    public AlbumJdbc(Banco banco) {
        this.banco = banco;
    }

    @Override
    public List<Album> todos() {
        return consultar("", List.of());
    }

    @Override
    public List<Album> buscar(String texto, String genero) {
        StringBuilder filtro = new StringBuilder();
        List<Object> params = new ArrayList<>();
        if (texto != null && !texto.isBlank()) {
            filtro.append(" AND (titulo LIKE ? OR artista LIKE ? OR genero LIKE ?)");
            String padrao = "%" + escaparLike(texto.strip()) + "%";
            params.add(padrao);
            params.add(padrao);
            params.add(padrao);
        }
        if (genero != null && !genero.isBlank()) {
            filtro.append(" AND genero = ?");
            params.add(genero);
        }
        return consultar(filtro.toString(), params);
    }

    /** O que o usuário digita não pode virar curinga do LIKE. */
    private static String escaparLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private List<Album> consultar(String filtro, List<Object> params) {
        String de = " FROM album WHERE 1 = 1" + filtro;
        try (Connection c = banco.abrir()) {
            Map<Integer, Album> mapa = new LinkedHashMap<>();
            try (PreparedStatement ps = c.prepareStatement("SELECT id, titulo, artista, ano_lancamento, genero" + de + " ORDER BY titulo")) {
                preencher(ps, params);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        mapa.put(rs.getInt("id"), new Album(rs.getInt("id"), rs.getString("titulo"), rs.getString("artista"),
                                rs.getInt("ano_lancamento"), rs.getString("genero")));
                    }
                }
            }
            if (!mapa.isEmpty()) {
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT id, album_id, numero, titulo, duracao_segundos FROM faixa WHERE album_id IN (SELECT id" + de
                        + ") ORDER BY album_id, numero")) {
                    preencher(ps, params);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            mapa.get(rs.getInt("album_id")).carregarFaixa(new Faixa(rs.getInt("id"), rs.getInt("numero"),
                                    rs.getString("titulo"), rs.getInt("duracao_segundos")));
                        }
                    }
                }
            }
            return new ArrayList<>(mapa.values());
        } catch (SQLException e) {
            throw banco.erro("ao consultar os álbuns", e);
        }
    }

    @Override
    public List<String> generos() {
        try (Connection c = banco.abrir(); Statement st = c.createStatement();
                ResultSet rs = st.executeQuery("SELECT DISTINCT genero FROM album WHERE genero IS NOT NULL ORDER BY genero")) {
            List<String> generos = new ArrayList<>();
            while (rs.next()) {
                generos.add(rs.getString(1));
            }
            return generos;
        } catch (SQLException e) {
            throw banco.erro("ao consultar os gêneros", e);
        }
    }

    @Override
    public Optional<Album> porId(int id) {
        return consultar(" AND id = ?", List.of(id)).stream().findFirst();
    }

    @Override
    public Album salvar(Album album) {
        try (Connection c = banco.abrir()) {
            c.setAutoCommit(false);
            try {
                gravarAlbum(c, album);
                sincronizarFaixas(c, album);
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
            return album;
        } catch (SQLException e) {
            throw banco.erro("ao salvar o álbum", e);
        }
    }

    private void gravarAlbum(Connection c, Album a) throws SQLException {
        if (a.getId() == 0) {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO album (titulo, artista, ano_lancamento, genero) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, a.getTitulo());
                ps.setString(2, a.getArtista());
                ps.setInt(3, a.getAnoLancamento());
                ps.setString(4, a.getGenero());
                ps.executeUpdate();
                try (ResultSet chaves = ps.getGeneratedKeys()) {
                    chaves.next();
                    a.setId(chaves.getInt(1));
                }
            }
        } else {
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE album SET titulo = ?, artista = ?, ano_lancamento = ?, genero = ? WHERE id = ?")) {
                ps.setString(1, a.getTitulo());
                ps.setString(2, a.getArtista());
                ps.setInt(3, a.getAnoLancamento());
                ps.setString(4, a.getGenero());
                ps.setInt(5, a.getId());
                ps.executeUpdate();
            }
        }
    }

    /**
     * Deixa no banco exatamente as faixas do objeto: apaga as que saíram, atualiza (e renumera) as que ficaram e
     * insere as novas. A ordem importa por causa do UNIQUE (album_id, numero): primeiro sai quem saiu, depois
     * as faixas seguem em ordem crescente de número.
     */
    private void sincronizarFaixas(Connection c, Album a) throws SQLException {
        Set<Integer> manter = new HashSet<>();
        for (Faixa f : a.getFaixas()) {
            if (f.getId() != 0) {
                manter.add(f.getId());
            }
        }
        StringBuilder sql = new StringBuilder("DELETE FROM faixa WHERE album_id = ?");
        if (!manter.isEmpty()) {
            sql.append(" AND id NOT IN (").append("?,".repeat(manter.size()), 0, manter.size() * 2 - 1).append(")");
        }
        try (PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int i = 1;
            ps.setInt(i++, a.getId());
            for (int id : manter) {
                ps.setInt(i++, id);
            }
            ps.executeUpdate();
        }
        for (Faixa f : a.getFaixas()) {
            if (f.getId() == 0) {
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO faixa (album_id, numero, titulo, duracao_segundos) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, a.getId());
                    ps.setInt(2, f.getNumero());
                    ps.setString(3, f.getTitulo());
                    ps.setInt(4, f.getDuracaoSegundos());
                    ps.executeUpdate();
                    try (ResultSet chaves = ps.getGeneratedKeys()) {
                        chaves.next();
                        f.setId(chaves.getInt(1));
                    }
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE faixa SET numero = ?, titulo = ?, duracao_segundos = ? WHERE id = ?")) {
                    ps.setInt(1, f.getNumero());
                    ps.setString(2, f.getTitulo());
                    ps.setInt(3, f.getDuracaoSegundos());
                    ps.setInt(4, f.getId());
                    ps.executeUpdate();
                }
            }
        }
    }

    @Override
    public void excluir(Album album) {
        try (Connection c = banco.abrir(); PreparedStatement ps = c.prepareStatement("DELETE FROM album WHERE id = ?")) {
            ps.setInt(1, album.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw banco.erro("ao excluir o álbum", e);
        }
    }

    @Override
    public boolean existeDuplicado(String titulo, String artista, int ano, Album ignorar) {
        // a collation utf8mb4_unicode_ci já ignora maiúsculas e acentos (RN02)
        try (Connection c = banco.abrir(); PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM album WHERE titulo = ? AND artista = ? AND ano_lancamento = ? AND id <> ?")) {
            ps.setString(1, titulo.strip());
            ps.setString(2, artista.strip());
            ps.setInt(3, ano);
            ps.setInt(4, ignorar == null ? 0 : ignorar.getId());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw banco.erro("ao verificar duplicidade do álbum", e);
        }
    }

    @Override
    public Map<Integer, NotaGeral> notasGerais() {
        try (Connection c = banco.abrir(); Statement st = c.createStatement();
                ResultSet rs = st.executeQuery("SELECT album_id, nota_geral, avaliadores FROM vw_nota_geral_album")) {
            Map<Integer, NotaGeral> mapa = new LinkedHashMap<>();
            while (rs.next()) {
                mapa.put(rs.getInt("album_id"), new NotaGeral(rs.getDouble("nota_geral"), rs.getInt("avaliadores")));
            }
            return mapa;
        } catch (SQLException e) {
            throw banco.erro("ao calcular as notas gerais", e);
        }
    }

    @Override
    public Optional<NotaGeral> notaGeral(Album album) {
        try (Connection c = banco.abrir(); PreparedStatement ps = c.prepareStatement(
                "SELECT nota_geral, avaliadores FROM vw_nota_geral_album WHERE album_id = ?")) {
            ps.setInt(1, album.getId());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(new NotaGeral(rs.getDouble(1), rs.getInt(2))) : Optional.empty();
            }
        } catch (SQLException e) {
            throw banco.erro("ao calcular a nota geral", e);
        }
    }

    @Override
    public int contarAvaliacoes(Album album) {
        return contar("SELECT COUNT(*) FROM avaliacao a JOIN faixa f ON f.id = a.faixa_id WHERE f.album_id = ?", album.getId());
    }

    @Override
    public int contarEntradas(Album album) {
        return contar("SELECT COUNT(*) FROM entrada_diario WHERE album_id = ?", album.getId());
    }

    private int contar(String sql, int id) {
        try (Connection c = banco.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw banco.erro("ao contar registros", e);
        }
    }

    private static void preencher(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }
}
