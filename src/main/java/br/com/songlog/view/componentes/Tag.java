package br.com.songlog.view.componentes;

import static br.com.songlog.view.Preferencias.px;

import br.com.songlog.view.Fontes.Estilo;
import br.com.songlog.view.Tema;
import br.com.songlog.view.Ui;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

/** Estado em texto (REAUDIÇÃO, PARCIAL): borda de 1 px, sem preenchimento. */
public class Tag extends JLabel {

    public Tag(String texto) {
        super(Estilo.LABEL.texto(texto));
        setFont(Estilo.LABEL.fonte());
        setForeground(Tema.atual().ink);
        setBorder(new EmptyBorder(px(2), px(8), px(2), px(8)));
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setColor(Tema.atual().ink);
        Ui.borda(g, 0, 0, getWidth(), getHeight(), 1);
        g.dispose();
    }
}
