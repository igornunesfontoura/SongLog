package br.com.songlog.repositorio.jdbc;

import br.com.songlog.banco.Banco;
import br.com.songlog.model.Album;
import br.com.songlog.model.Avaliacao;
import br.com.songlog.model.EntradaDiario;
import br.com.songlog.model.Faixa;
import br.com.songlog.model.Usuario;
import br.com.songlog.repositorio.AlbumRepositorio;
import br.com.songlog.repositorio.UsuarioRepositorio;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UsuarioJdbc implements UsuarioRepositorio {

    private final Banco banco;
    private final AlbumRepositorio albuns;

    public UsuarioJdbc(Banco banco, AlbumRepositorio albuns) {
        this.banco = banco;
        this.albuns = albuns;
    }

    @Override
    public Optional<Usuario> porEmail(String email) {
        return buscar("email = ?", email == null ? "" : email.strip().toLowerCase());
    }

    @Override
    public Optional<Usuario> porId(int id) {
        return buscar("id = ?", id);
    }

    private Optional<Usuario> buscar(String condicao, Object valor) {
        try (Connection c = banco.abrir()) {
            Usuario u;
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT id, nome, email, senha_hash, data_cadastro FROM usuario WHERE " + condicao)) {
                ps.setObject(1, valor);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                    u = new Usuario(rs.getInt("id"), rs.getString("nome"), rs.getString("email"), rs.getString("senha_hash"),
                            rs.getObject("data_cadastro", LocalDate.class));
                }
            }
            carregarAvaliacoesEDiario(c, u);
            return Optional.of(u);
        } catch (SQLException e) {
            throw banco.erro("ao consultar o usuário", e);
        }
    }

    /** Liga as avaliações e o diário do usuário aos objetos Album e Faixa correspondentes. */
    private void carregarAvaliacoesEDiario(Connection c, Usuario u) throws SQLException {
        Map<Integer, Album> porAlbum = new HashMap<>();
        Map<Integer, Faixa> porFaixa = new HashMap<>();
        for (Album a : albuns.todos()) {
            porAlbum.put(a.getId(), a);
            for (Faixa f : a.getFaixas()) {
                porFaixa.put(f.getId(), f);
            }
        }
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id, faixa_id, nota, comentario, data_avaliacao FROM avaliacao WHERE usuario_id = ?")) {
            ps.setInt(1, u.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Faixa f = porFaixa.get(rs.getInt("faixa_id"));
                    if (f != null) {
                        u.carregarAvaliacao(new Avaliacao(rs.getInt("id"), u, f, rs.getBigDecimal("nota").doubleValue(),
                                rs.getString("comentario"), rs.getObject("data_avaliacao", LocalDateTime.class)));
                    }
                }
            }
        }
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id, album_id, data_audicao, comentario, reaudicao FROM entrada_diario WHERE usuario_id = ?")) {
            ps.setInt(1, u.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Album a = porAlbum.get(rs.getInt("album_id"));
                    if (a != null) {
                        u.carregarEntrada(new EntradaDiario(rs.getInt("id"), u, a, rs.getObject("data_audicao", LocalDate.class),
                                rs.getString("comentario"), rs.getBoolean("reaudicao")));
                    }
                }
            }
        }
    }

    @Override
    public Usuario salvar(Usuario u) {
        try (Connection c = banco.abrir(); PreparedStatement ps = c.prepareStatement(
                "INSERT INTO usuario (nome, email, senha_hash, data_cadastro) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNome());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getSenhaHash());
            ps.setDate(4, Date.valueOf(u.getDataCadastro()));
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                u.setId(chaves.getInt(1));
            }
            return u;
        } catch (SQLException e) {
            throw banco.erro("ao salvar o usuário", e);
        }
    }
}
