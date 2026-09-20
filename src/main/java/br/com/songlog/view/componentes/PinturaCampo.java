package br.com.songlog.view.componentes;

import br.com.songlog.view.Tema;
import br.com.songlog.view.Ui;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Insets;
import javax.swing.text.JTextComponent;

/** Pintura compartilhada por {@link Campo} e {@link CampoSenha}. */
final class PinturaCampo {

    private PinturaCampo() {
    }

    static void fundo(Graphics2D g, JTextComponent c) {
        g.setColor(Tema.atual().bg);
        g.fillRect(0, 0, c.getWidth(), c.getHeight());
    }

    /** Dica, borda (1 px; 2 px em erro) e anel de foco. */
    static void frente(Graphics2D g, JTextComponent c, String dica, boolean erro) {
        Tema t = Tema.atual();
        int w = c.getWidth();
        int h = c.getHeight();
        if (c.getDocument().getLength() == 0 && dica != null && !dica.isEmpty()) {
            g.setFont(c.getFont());
            g.setColor(t.mute);
            FontMetrics fm = g.getFontMetrics();
            Insets in = c.getInsets();
            g.drawString(dica, in.left, (h - fm.getHeight()) / 2 + fm.getAscent());
        }
        boolean foco = c.hasFocus();
        int recuo = foco ? 2 * t.foco : 0;
        g.setColor(erro ? t.ink : (c.isEnabled() ? t.rule : t.ruleSoft));
        Ui.borda(g, recuo, recuo, w - 2 * recuo, h - 2 * recuo, erro ? 2 : 1);
        if (foco) {
            g.setColor(t.ink);
            Ui.borda(g, 0, 0, w, h, t.foco);
        }
    }
}
