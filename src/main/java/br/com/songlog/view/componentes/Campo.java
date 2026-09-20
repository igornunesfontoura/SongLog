package br.com.songlog.view.componentes;

import static br.com.songlog.view.Preferencias.px;

import br.com.songlog.view.Fontes.Estilo;
import br.com.songlog.view.Tema;
import br.com.songlog.view.Ui;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/** Campo de texto de 36 px: borda de 1 px, erro com borda de 2 px, dica e anel de foco. */
public class Campo extends JTextField {

    private String dica = "";
    private boolean erro;

    public Campo() {
        Tema t = Tema.atual();
        setOpaque(false);
        setBorder(new EmptyBorder(0, px(10), 0, px(10)));
        setFont(Estilo.BODY.fonte());
        setForeground(t.ink);
        setCaretColor(t.ink);
        setSelectionColor(t.ruleSoft);
        setSelectedTextColor(t.ink);
        setDisabledTextColor(t.disabled);
    }

    public Campo(String nomeAcessivel) {
        this();
        getAccessibleContext().setAccessibleName(nomeAcessivel);
    }

    public Campo dica(String dica) {
        this.dica = dica;
        repaint();
        return this;
    }

    public Campo nome(String nomeAcessivel) {
        getAccessibleContext().setAccessibleName(nomeAcessivel);
        return this;
    }

    public void setErro(boolean erro) {
        this.erro = erro;
        repaint();
    }

    public boolean isErro() {
        return erro;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(Math.max(super.getPreferredSize().width, px(120)), px(36));
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(px(40), px(36));
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, px(36));
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Ui.suavizar(g);
        PinturaCampo.fundo(g, this);
        super.paintComponent(g);
        PinturaCampo.frente(g, this, dica, erro);
        g.dispose();
    }
}
