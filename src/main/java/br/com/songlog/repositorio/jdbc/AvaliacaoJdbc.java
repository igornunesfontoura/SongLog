package br.com.songlog.repositorio.jdbc;

import br.com.songlog.banco.Banco;
import br.com.songlog.model.Avaliacao;
import br.com.songlog.model.Faixa;
import br.com.songlog.model.Usuario;
import br.com.songlog.repositorio.AvaliacaoRepositorio;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AvaliacaoJdbc implements AvaliacaoRepositorio {

    private final Banco banco;

    public AvaliacaoJdbc(Banco banco) {
        this.banco = banco;
    }

    /** RN04: um upsert sobre o UNIQUE (usuario_id, faixa_id). A data vem do relógio do banco, igual à dos dados iniciais. */
    @Override
    public void salvar(Avaliacao a) {
        try (Connection c = banco.abrir()) {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO avaliacao (usuario_id, faixa_id, nota, comentario, data_avaliacao) VALUES (?, ?, ?, ?, NOW()) "
                    + "ON DUPLICATE KEY UPDATE nota = VALUES(nota), comentario = VALUES(comentario), data_avaliacao = NOW()")) {
                ps.setInt(1, a.getUsuario().getId());
                ps.setInt(2, a.getFaixa().getId());
                ps.setBigDecimal(3, BigDecimal.valueOf(a.getNota()));
                ps.setString(4, a.getComentario());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("SELECT id FROM avaliacao WHERE usuario_id = ? AND faixa_id = ?")) {
                ps.setInt(1, a.getUsuario().getId());
                ps.setInt(2, a.getFaixa().getId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        a.setId(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            throw banco.erro("ao salvar a avaliação", e);
        }
    }

    @Override
    public void remover(Usuario usuario, Faixa faixa) {
        try (Connection c = banco.abrir(); PreparedStatement ps = c.prepareStatement(
                "DELETE FROM avaliacao WHERE usuario_id = ? AND faixa_id = ?")) {
            ps.setInt(1, usuario.getId());
            ps.setInt(2, faixa.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw banco.erro("ao remover a avaliação", e);
        }
    }
}
