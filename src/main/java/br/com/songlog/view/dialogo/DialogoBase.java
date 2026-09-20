package br.com.songlog.view.dialogo;

import static br.com.songlog.view.Preferencias.px;

import br.com.songlog.view.Fontes.Estilo;
import br.com.songlog.view.Tema;
import br.com.songlog.view.Ui;
import br.com.songlog.view.componentes.Botao;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 * Janela modal do projeto de interfaces: borda de 2 px em tinta, título em H2, botão primário
 * à direita, Esc cancela e o foco inicial vai para o botão indicado (Cancelar nas ações destrutivas).
 */
public class DialogoBase extends JDialog {

    private boolean confirmado;

    /**
     * Monta o conteúdo visual do diálogo (título, corpo e botões). Fica separado da janela para
     * poder ser desenhado sem monitor nos testes de captura.
     */
    public static JPanel montar(String titulo, JComponent corpo, Botao... botoes) {
        Tema t = Tema.atual();
        JPanel raiz = new JPanel(new BorderLayout(0, px(12)));
        raiz.setBackground(t.bg);
        raiz.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(t.ink, 2),
                BorderFactory.createEmptyBorder(px(24), px(24), px(24), px(24))));
        raiz.add(Ui.texto(titulo, Estilo.H2, t.ink, 22), BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                return new Dimension(Math.max(d.width, px(480)), d.height);
            }
        };
        centro.setOpaque(false);
        centro.add(corpo, BorderLayout.CENTER);
        raiz.add(centro, BorderLayout.CENTER);

        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, px(16), 0));
        rodape.setOpaque(false);
        rodape.setBorder(BorderFactory.createEmptyBorder(px(12), 0, 0, 0));
        for (Botao b : botoes) {
            rodape.add(b);
        }
        raiz.add(rodape, BorderLayout.SOUTH);
        return raiz;
    }

    public DialogoBase(Window dono, String titulo, JComponent corpo, JComponent focoInicial, Botao... botoes) {
        super(dono, titulo, ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setContentPane(montar(titulo, corpo, botoes));
        getAccessibleContext().setAccessibleName(titulo);

        getRootPane().registerKeyboardAction((ActionEvent e) -> fechar(false),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                if (focoInicial != null) {
                    focoInicial.requestFocusInWindow();
                }
            }
        });
        pack();
        setLocationRelativeTo(dono);
    }

    /** Mostra o diálogo (bloqueia até fechar) e devolve se foi confirmado. */
    public boolean mostrar() {
        setVisible(true);
        return confirmado;
    }

    public void fechar(boolean confirmar) {
        this.confirmado = confirmar;
        dispose();
    }
}
