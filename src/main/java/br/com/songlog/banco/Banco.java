package br.com.songlog.banco;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Conexão com o MySQL. {@link #conectar} confere as credenciais e, na primeira execução, cria o banco,
 * as tabelas e os dados iniciais a partir dos scripts de banco/ (os mesmos que o Workbench executa).
 * Cada operação abre e fecha a sua própria conexão.
 */
public final class Banco {

    private final Configuracao configuracao;

    private Banco(Configuracao configuracao) {
        this.configuracao = configuracao;
    }

    /** @throws BancoException com mensagem pronta para o usuário se não for possível usar o banco */
    public static Banco conectar(Configuracao configuracao) {
        if (!configuracao.nome().matches("[A-Za-z0-9_]+")) {
            throw new BancoException("Erro na configuração: o nome do banco só pode ter letras, números e _.", null);
        }
        Banco banco = new Banco(configuracao);
        banco.prepararBanco();
        return banco;
    }

    public Configuracao configuracao() {
        return configuracao;
    }

    public Connection abrir() throws SQLException {
        return DriverManager.getConnection(configuracao.url(), configuracao.usuario(), configuracao.senha());
    }

    private void prepararBanco() {
        try (Connection c = DriverManager.getConnection(configuracao.urlServidor(), configuracao.usuario(), configuracao.senha());
                Statement st = c.createStatement()) {
            st.execute("CREATE DATABASE IF NOT EXISTS `" + configuracao.nome() + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        } catch (SQLException e) {
            throw erro("ao conectar", e);
        }
        try (Connection c = abrir()) {
            if (!tabelaExiste(c, "usuario")) {
                ScriptSql.executar(c, recurso("/banco/01-esquema.sql"));
            }
            if (configuracao.dadosIniciais() && contar(c, "usuario") == 0 && contar(c, "album") == 0) {
                ScriptSql.executar(c, recurso("/banco/02-dados-iniciais.sql"));
            }
        } catch (SQLException e) {
            throw erro("ao preparar o banco", e);
        }
    }

    private static boolean tabelaExiste(Connection c, String tabela) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?")) {
            ps.setString(1, tabela);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    private static int contar(Connection c, String tabela) throws SQLException {
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + tabela)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static String recurso(String caminho) {
        try (InputStream in = Banco.class.getResourceAsStream(caminho)) {
            if (in == null) {
                throw new BancoException("Erro: o script " + caminho + " não está no programa.", null);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BancoException("Erro ao ler o script " + caminho + ".", e);
        }
    }

    /** Traduz o erro do MySQL para uma mensagem que o usuário entende. */
    public BancoException erro(String contexto, SQLException e) {
        int codigo = e.getErrorCode();
        String estado = e.getSQLState() == null ? "" : e.getSQLState();
        String motivo;
        if (codigo == 1045 || codigo == 1044) {
            motivo = "usuário ou senha do MySQL incorretos (usuário “" + configuracao.usuario() + "”)";
        } else if (estado.startsWith("08")) {
            motivo = "não foi possível conectar ao MySQL em " + configuracao.host() + ":" + configuracao.porta()
                    + ". Verifique se o serviço está ativo";
        } else if (codigo == 1049) {
            motivo = "o banco “" + configuracao.nome() + "” não existe";
        } else if (codigo == 1062) {
            motivo = "já existe um registro igual";
        } else if (codigo == 3819 || codigo == 4025) {
            motivo = "o valor informado viola uma regra do banco";
        } else if (codigo == 1452 || codigo == 1451) {
            motivo = "o registro depende de outro que não existe (ou é usado por outro)";
        } else {
            motivo = e.getMessage();
        }
        return new BancoException("Erro " + contexto + ": " + motivo + ".", e);
    }
}
