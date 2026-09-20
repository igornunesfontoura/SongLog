package br.com.songlog.view.componentes;

import static br.com.songlog.view.Preferencias.px;

import br.com.songlog.view.Fontes.Estilo;
import br.com.songlog.view.Tema;
import br.com.songlog.view.Ui;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

/** Lista suspensa no estilo do projeto: borda de 1 px, seta em "v", lista com filetes. */
public class Seletor<T> extends JComboBox<T> {

    private String dica = "";

    public Seletor(String nomeAcessivel) {
        setUI(new Visual());
        setOpaque(false);
        setFont(Estilo.BODY.fonte());
        setRenderer(new Item<>());
        setMaximumRowCount(8);
        getAccessibleContext().setAccessibleName(nomeAcessivel);
    }

    /** Texto mostrado quando nada está selecionado. */
    public Seletor<T> dica(String dica) {
        this.dica = dica;
        repaint();
        return this;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(px(200), px(36));
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, px(36));
    }

    private static final class Item<E> extends JLabel implements ListCellRenderer<E> {
        Item() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends E> lista, E valor, int indice, boolean selecionado, boolean foco) {
            Tema t = Tema.atual();
            setText(valor == null ? "" : valor.toString());
            setFont(Estilo.BODY.fonte());
            setForeground(t.ink);
            setBackground(selecionado ? t.surface : t.bg);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, t.ruleSoft),
                    BorderFactory.createEmptyBorder(px(8), px(10), px(8), px(10))));
            return this;
        }
    }

    private static final class Visual extends BasicComboBoxUI {

        @Override
        protected JButton createArrowButton() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setVisible(false);
            return b;
        }

        @Override
        protected ComboPopup createPopup() {
            return new BasicComboPopup(comboBox) {
                @Override
                protected void configurePopup() {
                    super.configurePopup();
                    setBorder(BorderFactory.createLineBorder(Tema.atual().ink, 1));
                    getList().setBackground(Tema.atual().bg);
                    getList().setSelectionBackground(Tema.atual().surface);
                }
            };
        }

        @Override
        public void paint(Graphics g0, JComponent c) {
            Graphics2D g = (Graphics2D) g0.create();
            Ui.suavizar(g);
            Tema t = Tema.atual();
            int w = c.getWidth();
            int h = c.getHeight();
            g.setColor(t.bg);
            g.fillRect(0, 0, w, h);
            boolean foco = comboBox.hasFocus();
            int recuo = foco ? 2 * t.foco : 0;
            g.setColor(comboBox.isEnabled() ? t.rule : t.ruleSoft);
            Ui.borda(g, recuo, recuo, w - 2 * recuo, h - 2 * recuo, 1);

            Object sel = comboBox.getSelectedItem();
            boolean vazio = sel == null;
            String texto = vazio ? ((Seletor<?>) comboBox).dica : sel.toString();
            g.setFont(comboBox.getFont());
            FontMetrics fm = g.getFontMetrics();
            g.setColor(vazio ? t.mute : t.ink);
            int maxLargura = w - px(10) * 2 - px(20);
            while (fm.stringWidth(texto) > maxLargura && texto.length() > 1) {
                texto = texto.substring(0, texto.length() - 2) + "…";
            }
            g.drawString(texto, px(10), (h - fm.getHeight()) / 2 + fm.getAscent());

            // seta "v"
            int cx = w - px(18);
            int cy = h / 2;
            int a = px(5);
            g.setColor(t.ink);
            g.setStroke(new java.awt.BasicStroke(Math.max(1.5f, px(2))));
            g.drawPolyline(new int[] { cx - a, cx, cx + a }, new int[] { cy - a / 2, cy + a / 2, cy - a / 2 }, 3);
            if (foco) {
                g.setColor(t.ink);
                g.setStroke(new java.awt.BasicStroke(1f));
                Ui.borda(g, 0, 0, w, h, t.foco);
            }
            g.dispose();
        }
    }
}
