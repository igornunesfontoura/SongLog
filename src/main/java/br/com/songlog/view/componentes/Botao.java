package br.com.songlog.view.componentes;

import static br.com.songlog.view.Preferencias.px;

import br.com.songlog.view.Fontes.Estilo;
import br.com.songlog.view.Tema;
import br.com.songlog.view.Ui;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.KeyStroke;

/**
 * Botão do projeto de interfaces: primário (acento, uma ação por tela), secundário (borda)
 * e terciário (texto sublinhado). Cantos retos, hover, anel de foco de 2 px com folga de 2 px.
 */
public class Botao extends JButton {

    public enum Tipo { PRIMARIO, SECUNDARIO, TERCIARIO }

    private final Tipo tipo;
    private boolean sobre;

    public Botao(String texto, Tipo tipo) {
        super(texto);
        this.tipo = tipo;
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setRolloverEnabled(true);
        setMargin(new Insets(0, 0, 0, 0));
        setFont((tipo == Tipo.TERCIARIO ? Estilo.BODY : Estilo.BODY_BOLD).fonte());
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Enter também aciona o botão focado (padrão do Swing só aceita a barra de espaço)
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), "pressed");
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), "released");
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                sobre = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                sobre = false;
                repaint();
            }
        });
    }

    public static Botao primario(String texto) {
        return new Botao(texto, Tipo.PRIMARIO);
    }

    public static Botao secundario(String texto) {
        return new Botao(texto, Tipo.SECUNDARIO);
    }

    public static Botao link(String texto) {
        return new Botao(texto, Tipo.TERCIARIO);
    }

    /** Deixa o botão ocupar toda a largura disponível (ex.: "Entrar"). */
    public Botao larguraTotal() {
        setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
        return this;
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        int tw = fm.stringWidth(getText());
        if (tipo == Tipo.TERCIARIO) {
            return new Dimension(tw + 2 * (Tema.atual().foco + 1), fm.getHeight() + px(8));
        }
        return new Dimension(Math.max(tw + px(32), px(88)), px(36));
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return isMaximumSizeSet() ? super.getMaximumSize() : getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Ui.suavizar(g);
        Tema t = Tema.atual();
        int w = getWidth();
        int h = getHeight();
        boolean ativo = isEnabled();
        boolean foco = isFocusOwner();
        boolean destaque = ativo && (sobre || getModel().isArmed());
        int in = foco && tipo != Tipo.TERCIARIO ? 2 * t.foco : 0;
        int vw = w - 2 * in;
        int vh = h - 2 * in;

        switch (tipo) {
            case PRIMARIO -> {
                if (ativo) {
                    g.setColor(destaque ? t.accentHover : t.accent);
                    g.fillRect(in, in, vw, vh);
                } else {
                    g.setColor(t.ruleSoft);
                    Ui.borda(g, in, in, vw, vh, 1);
                }
            }
            case SECUNDARIO -> {
                if (destaque) {
                    g.setColor(t.surface);
                    g.fillRect(in, in, vw, vh);
                }
                g.setColor(ativo ? t.ink : t.ruleSoft);
                Ui.borda(g, in, in, vw, vh, 1);
            }
            case TERCIARIO -> {
            }
        }

        g.setFont(getFont());
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(getText());
        int x = (w - tw) / 2;
        int y = (h - fm.getHeight()) / 2 + fm.getAscent();
        g.setColor(!ativo ? t.disabled : (tipo == Tipo.PRIMARIO ? t.onAccent : t.ink));
        g.drawString(getText(), x, y);
        if (tipo == Tipo.TERCIARIO) {
            g.fillRect(x, y + px(2), tw, Math.max(1, px(1)));
        }
        if (foco) {
            g.setColor(t.ink);
            Ui.borda(g, 0, 0, w, h, t.foco);
        }
        g.dispose();
    }
}
