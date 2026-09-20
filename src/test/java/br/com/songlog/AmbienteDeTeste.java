package br.com.songlog;

import br.com.songlog.banco.Banco;
import br.com.songlog.banco.Configuracao;
import br.com.songlog.service.Contexto;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Banco de teste: recria do zero o banco {@code songlog_teste} com os scripts de banco/ e devolve um Contexto.
 * Nunca mexe no banco de verdade: se o nome configurado não terminar em "_teste", recusa.
 *
 * A conexão vem de -Dsonglog.db.host / porta / usuario / senha (ou SONGLOG_DB_*).
 */
final class AmbienteDeTeste {

    private AmbienteDeTeste() {
    }

    static Configuracao configuracao() {
        Configuracao c = Configuracao.carregar();
        String nome = System.getProperty("songlog.db.nome", "songlog_teste");
        if (!nome.endsWith("_teste")) {
            throw new IllegalStateException("Por segurança o banco de teste deve terminar em _teste (recebido: " + nome + ")");
        }
        return new Configuracao(c.host(), c.porta(), nome, c.usuario(), c.senha(), true);
    }

    /** Apaga e recria o banco de teste; a criação das tabelas e dos dados é feita pelo próprio {@link Banco}. */
    static Contexto novo() {
        Configuracao cfg = configuracao();
        try (Connection c = DriverManager.getConnection(cfg.urlServidor(), cfg.usuario(), cfg.senha());
                Statement st = c.createStatement()) {
            st.execute("DROP DATABASE IF EXISTS `" + cfg.nome() + "`");
        } catch (java.sql.SQLException e) {
            throw new IllegalStateException("Não foi possível preparar o banco de teste em " + cfg + ": " + e.getMessage(), e);
        }
        return new Contexto(Banco.conectar(cfg));
    }

    /** Outra conexão ao mesmo banco, sem recriar: simula fechar e abrir o programa de novo. */
    static Contexto reabrir() {
        return new Contexto(Banco.conectar(configuracao()));
    }
}
