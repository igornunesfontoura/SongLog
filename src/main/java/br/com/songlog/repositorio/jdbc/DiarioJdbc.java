package br.com.songlog.repositorio.jdbc;

import br.com.songlog.banco.Banco;
import br.com.songlog.model.EntradaDiario;
import br.com.songlog.repositorio.DiarioRepositorio;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class DiarioJdbc implements DiarioRepositorio {

    private final Banco banco;

    public DiarioJdbc(Banco banco) {
        this.banco = banco;
    }

    @Override
    public void inserir(EntradaDiario e) {
        try (Connection c = banco.abrir(); PreparedStatement ps = c.prepareStatement(
                "INSERT INTO entrada_diario (usuario_id, album_id, data_audicao, comentario, reaudicao) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, e.getUsuario().getId());
            ps.setInt(2, e.getAlbum().getId());
            ps.setDate(3, Date.valueOf(e.getDataAudicao()));
            ps.setString(4, e.getComentario());
            ps.setBoolean(5, e.isReaudicao());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                chaves.next();
                e.setId(chaves.getInt(1));
            }
        } catch (SQLException ex) {
            throw banco.erro("ao registrar a audição", ex);
        }
    }

    @Override
    public void atualizar(EntradaDiario e, LocalDate novaData, String novoComentario) {
        try (Connection c = banco.abrir(); PreparedStatement ps = c.prepareStatement(
                "UPDATE entrada_diario SET data_audicao = ?, comentario = ? WHERE id = ?")) {
            ps.setDate(1, Date.valueOf(novaData));
            ps.setString(2, novoComentario == null || novoComentario.isBlank() ? null : novoComentario.strip());
            ps.setInt(3, e.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw banco.erro("ao atualizar a audição", ex);
        }
    }

    @Override
    public void excluir(EntradaDiario e) {
        try (Connection c = banco.abrir(); PreparedStatement ps = c.prepareStatement("DELETE FROM entrada_diario WHERE id = ?")) {
            ps.setInt(1, e.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw banco.erro("ao excluir a audição", ex);
        }
    }
}
