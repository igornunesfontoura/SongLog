package br.com.songlog;

import br.com.songlog.service.Contexto;
import br.com.songlog.view.Janela;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Ponto de entrada do SongLog (desktop, Java Swing). Nesta etapa os dados vivem em memória
 * (ver {@code repositorio}); a integração com o MySQL troca só a implementação dos repositórios.
 *
 * Usuário de demonstração: ana@songlog.dev, senha songlog123.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception e) {
                // segue com o visual padrão do Swing
            }
            new Janela(Contexto.emMemoria()).setVisible(true);
        });
    }
}
