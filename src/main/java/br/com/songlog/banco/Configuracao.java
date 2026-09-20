package br.com.songlog.banco;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Dados de conexão com o MySQL. Ordem de leitura (o último vence):
 * padrões do programa, config/songlog.properties, config/songlog.local.properties (criado pela
 * janela de conexão e fora do Git), variáveis de ambiente SONGLOG_DB_* e propriedades -Dsonglog.db.*.
 */
public final class Configuracao {

    public static final Path ARQUIVO = Paths.get("config", "songlog.properties");
    public static final Path ARQUIVO_LOCAL = Paths.get("config", "songlog.local.properties");

    private final String host;
    private final int porta;
    private final String nome;
    private final String usuario;
    private final String senha;
    private final boolean dadosIniciais;

    public Configuracao(String host, int porta, String nome, String usuario, String senha, boolean dadosIniciais) {
        this.host = host;
        this.porta = porta;
        this.nome = nome;
        this.usuario = usuario;
        this.senha = senha;
        this.dadosIniciais = dadosIniciais;
    }

    public static Configuracao carregar() {
        Properties p = new Properties();
        p.setProperty("banco.host", "localhost");
        p.setProperty("banco.porta", "3306");
        p.setProperty("banco.nome", "songlog");
        p.setProperty("banco.usuario", "root");
        p.setProperty("banco.senha", "");
        p.setProperty("banco.dados-iniciais", "true");
        ler(ARQUIVO, p);
        ler(ARQUIVO_LOCAL, p);
        for (String chave : new String[] { "host", "porta", "nome", "usuario", "senha", "dados-iniciais" }) {
            String env = System.getenv("SONGLOG_DB_" + chave.toUpperCase().replace('-', '_'));
            if (env != null) {
                p.setProperty("banco." + chave, env);
            }
            String prop = System.getProperty("songlog.db." + chave);
            if (prop != null) {
                p.setProperty("banco." + chave, prop);
            }
        }
        int porta;
        try {
            porta = Integer.parseInt(p.getProperty("banco.porta").strip());
        } catch (NumberFormatException e) {
            porta = 3306;
        }
        return new Configuracao(p.getProperty("banco.host").strip(), porta, p.getProperty("banco.nome").strip(),
                p.getProperty("banco.usuario").strip(), p.getProperty("banco.senha"),
                Boolean.parseBoolean(p.getProperty("banco.dados-iniciais").strip()));
    }

    private static void ler(Path arquivo, Properties p) {
        if (Files.isRegularFile(arquivo)) {
            try (InputStream in = Files.newInputStream(arquivo)) {
                p.load(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
            } catch (IOException e) {
                // arquivo ilegível: segue com os valores anteriores
            }
        }
    }

    /** Guarda a conexão que funcionou em config/songlog.local.properties (arquivo fora do Git). */
    public void salvarLocal() {
        Properties p = new Properties();
        p.setProperty("banco.host", host);
        p.setProperty("banco.porta", String.valueOf(porta));
        p.setProperty("banco.nome", nome);
        p.setProperty("banco.usuario", usuario);
        p.setProperty("banco.senha", senha);
        try {
            Files.createDirectories(ARQUIVO_LOCAL.getParent());
            try (OutputStream out = Files.newOutputStream(ARQUIVO_LOCAL)) {
                p.store(new java.io.OutputStreamWriter(out, java.nio.charset.StandardCharsets.UTF_8),
                        "Conexão local com o MySQL (gerado pelo SongLog; não versionar)");
            }
        } catch (IOException e) {
            // sem permissão de escrita: a conexão continua valendo nesta execução
        }
    }

    public Configuracao com(String host, int porta, String usuario, String senha) {
        return new Configuracao(host, porta, nome, usuario, senha, dadosIniciais);
    }

    /** URL do banco do SongLog. */
    public String url() {
        return "jdbc:mysql://" + host + ":" + porta + "/" + nome + parametros();
    }

    /** URL do servidor, sem escolher banco (usada para criar o banco na primeira execução). */
    public String urlServidor() {
        return "jdbc:mysql://" + host + ":" + porta + "/" + parametros();
    }

    private static String parametros() {
        return "?useUnicode=true&characterEncoding=UTF-8&connectTimeout=5000&socketTimeout=30000"
                + "&allowPublicKeyRetrieval=true&useSSL=false";
    }

    public String host() {
        return host;
    }

    public int porta() {
        return porta;
    }

    public String nome() {
        return nome;
    }

    public String usuario() {
        return usuario;
    }

    public String senha() {
        return senha;
    }

    public boolean dadosIniciais() {
        return dadosIniciais;
    }

    @Override
    public String toString() {
        return usuario + "@" + host + ":" + porta + "/" + nome;
    }
}
