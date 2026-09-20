package br.com.songlog.view.componentes;

import static br.com.songlog.view.Preferencias.px;

import br.com.songlog.view.Fontes.Estilo;
import br.com.songlog.view.Tema;
import br.com.songlog.view.Ui;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;

/** Campo de senha com a mesma aparência de {@link Campo}; "Mostrar senha" alterna o eco. */
public class CampoSenha extends JPasswordField {

    private boolean erro;
    private final char ecoPadrao;

    public CampoSenha(String nomeAcessivel) {
        Tema t = Tema.atual();
        setOpaque(false);
        setBorder(new EmptyBorder(0, px(10), 0, px(10)));
        setFont(Estilo.MONO.fonte());
        setForeground(t.ink);
        setCaretColor(t.ink);
        setSelectionColor(t.ruleSoft);
        setSelectedTextColor(t.ink);
        this.ecoPadrao = '•';
        setEchoChar(ecoPadrao);
        getAccessibleContext().setAccessibleName(nomeAcessivel);
    }

    public void mostrar(boolean visivel) {
        setEchoChar(visivel ? (char) 0 : ecoPadrao);
    }

    public void setErro(boolean erro) {
        this.erro = erro;
        repaint();
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
        PinturaCampo.frente(g, this, "", erro);
        g.dispose();
    }
}
