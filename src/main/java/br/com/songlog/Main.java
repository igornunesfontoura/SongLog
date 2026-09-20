package br.com.songlog;

import br.com.songlog.banco.Banco;
import br.com.songlog.banco.BancoException;
import br.com.songlog.banco.Configuracao;
import br.com.songlog.service.Contexto;
import br.com.songlog.view.Janela;
import br.com.songlog.view.dialogo.Dialogos;
import br.com.songlog.view.dialogo.DialogoConexao;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Ponto de entrada do SongLog (desktop, Java Swing + MySQL).
 *
 * Conecta ao MySQL com a configuração de config/ (ou variáveis de ambiente); se falhar, abre a janela de
 * conexão. Com o servidor acessível, cria o banco e carrega os dados de teste (pasta banco/) quando ainda não existem.
 * Usuário de demonstração: ana@songlog.dev, senha songlog123.
 */
public final class Main {

    private static boolean mostrandoErro;

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        Thread.setDefaultUncaughtExceptionHandler((thread, erro) -> {
            erro.printStackTrace();
            SwingUtilities.invokeLater(() -> mostrarErro(erro));
        });
        SwingUtilities.invokeAndWait(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e) {
                // segue com o visual padrão do Swing
            }
        });
        Banco banco = conectar(Configuracao.carregar());
        if (banco == null) {
            return; // o usuário escolheu sair na janela de conexão
        }
        Contexto ctx = new Contexto(banco);
        SwingUtilities.invokeLater(() -> new Janela(ctx).setVisible(true));
    }

    /** Tenta conectar; se falhar, mostra o motivo e pede usuário e senha do MySQL até conseguir ou o usuário sair. */
    private static Banco conectar(Configuracao configuracao) throws Exception {
        Configuracao atual = configuracao;
        boolean digitada = false;
        while (true) {
            try {
                Banco banco = Banco.conectar(atual);
                if (digitada) {
                    atual.salvarLocal();
                }
                return banco;
            } catch (BancoException e) {
                Configuracao base = atual;
                String motivo = e.getMessage();
                Configuracao[] nova = new Configuracao[1];
                SwingUtilities.invokeAndWait(() -> nova[0] = DialogoConexao.pedir(null, base, motivo));
                if (nova[0] == null) {
                    return null;
                }
                atual = nova[0];
                digitada = true;
            }
        }
    }

    /** Falhas do banco durante o uso viram um aviso claro em vez de o programa parecer travado. */
    private static void mostrarErro(Throwable erro) {
        if (mostrandoErro) {
            return;
        }
        mostrandoErro = true;
        try {
            Throwable causa = erro;
            while (causa != null && !(causa instanceof BancoException)) {
                causa = causa.getCause();
            }
            Window ativa = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
            if (causa != null) {
                Dialogos.erro(ativa, "Não foi possível acessar o banco de dados",
                        causa.getMessage() + " Sua última ação não foi concluída.");
            } else {
                Dialogos.erro(ativa, "Erro inesperado", "Algo deu errado: " + erro + ". Os detalhes estão no terminal.");
            }
        } finally {
            mostrandoErro = false;
        }
    }
}
