package br.com.songlog.view.componentes;

import static br.com.songlog.view.Preferencias.px;

import br.com.songlog.view.Fontes.Estilo;
import br.com.songlog.view.Tema;
import br.com.songlog.view.Ui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Mensagem da última ação. Persiste até a próxima: nada some sozinho. */
public class BarraDeStatus extends JPanel {

    private final JLabel mensagem = Ui.texto(" ", Estilo.SMALL, Tema.atual().ink2);

    public BarraDeStatus() {
        setLayout(new BorderLayout());
        setBackground(Tema.atual().bg);
        setBorder(Ui.margem(0, 40, 0, 40));
        add(mensagem, BorderLayout.CENTER);
        getAccessibleContext().setAccessibleName("Barra de status");
    }

    public void mensagem(String texto) {
        mensagem.setText(texto == null || texto.isEmpty() ? " " : texto);
        getAccessibleContext().setAccessibleDescription(texto);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(1, px(32));
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        g.setColor(Tema.atual().rule);
        g.fillRect(0, 0, getWidth(), 1);
    }
}
