package br.com.songlog.view.componentes;

import static br.com.songlog.view.Preferencias.px;

import br.com.songlog.view.Tema;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.plaf.basic.BasicScrollBarUI;

/** Área com rolagem sem borda e com barra plana (tinta sobre fundo). */
public class Rolagem extends JScrollPane {

    public Rolagem(Component conteudo) {
        super(conteudo instanceof Scrollable ? conteudo : new Vista(conteudo), VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
        setBorder(null);
        setOpaque(false);
        getViewport().setOpaque(false);
        getVerticalScrollBar().setUI(new Plana());
        getVerticalScrollBar().setPreferredSize(new Dimension(px(12), 0));
        getVerticalScrollBar().setUnitIncrement(px(24));
    }

    /** Segue a largura da janela (nunca rola na horizontal) e deixa a altura livre para rolar. */
    private static final class Vista extends JPanel implements Scrollable {
        Vista(Component conteudo) {
            super(new BorderLayout());
            setOpaque(false);
            add(conteudo, BorderLayout.NORTH);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visivel, int orientacao, int direcao) {
            return px(24);
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visivel, int orientacao, int direcao) {
            return Math.max(px(24), (int) (visivel.height * 0.9));
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private static final class Plana extends BasicScrollBarUI {
        private static JButton nada() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return nada();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return nada();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(Tema.atual().bg);
            g.fillRect(r.x, r.y, r.width, r.height);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            g.setColor(Tema.atual().rule);
            g.fillRect(r.x + px(3), r.y, r.width - px(6), r.height);
        }
    }
}
