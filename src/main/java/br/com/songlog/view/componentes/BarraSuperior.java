package br.com.songlog.view.componentes;

import static br.com.songlog.view.Preferencias.px;

import br.com.songlog.view.Fontes.Estilo;
import br.com.songlog.view.Navegador;
import br.com.songlog.view.Tema;
import br.com.songlog.view.Ui;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.font.TextAttribute;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/** Marca, abas (Catálogo, Perfil e Diário), nome do usuário (abre Preferências) e Sair. */
public class BarraSuperior extends JPanel {

    public enum Aba { CATALOGO, PERFIL }

    public BarraSuperior(Navegador nav, Aba ativa) {
        Tema t = Tema.atual();
        setLayout(new BorderLayout());
        setBackground(t.bg);
        setBorder(Ui.margem(0, 40, 0, 40));

        JPanel esquerda = Ui.painel();
        esquerda.setLayout(new BoxLayout(esquerda, BoxLayout.X_AXIS));
        JLabel marca = new JLabel("SONGLOG");
        Map<TextAttribute, Object> m = new HashMap<>();
        m.put(TextAttribute.TRACKING, 0.06f);
        marca.setFont(Estilo.H2.fonte(20).deriveFont(m));
        marca.setForeground(t.ink);
        marca.setBorder(Ui.margem(0, 0, 2, 48));
        esquerda.add(marca);
        esquerda.add(new AbaBotao("Catálogo", ativa == Aba.CATALOGO, nav::irParaCatalogo, KeyEvent.VK_C));
        esquerda.add(new AbaBotao("Perfil e Diário", ativa == Aba.PERFIL, nav::irParaPerfil, KeyEvent.VK_P));
        add(esquerda, BorderLayout.WEST);

        JPanel direita = Ui.painel();
        direita.setLayout(new BoxLayout(direita, BoxLayout.X_AXIS));
        Botao usuario = Botao.link(nav.contexto().usuario().getNome());
        usuario.getAccessibleContext().setAccessibleDescription("Abre as preferências de exibição");
        usuario.addActionListener(e -> nav.abrirPreferencias());
        Botao sair = Botao.link("Sair");
        sair.addActionListener(e -> nav.sair());
        direita.add(Ui.cola());
        direita.add(usuario);
        direita.add(Ui.espaco(16, 0));
        direita.add(sair);
        direita.setBorder(Ui.margem(0, 0, 2, 0));
        add(direita, BorderLayout.EAST);
        getAccessibleContext().setAccessibleName("Navegação principal");
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(1, px(56));
    }

    @Override
    protected void paintChildren(Graphics g) {
        Tema t = Tema.atual();
        g.setColor(t.ink);
        g.fillRect(0, getHeight() - 2, getWidth(), 2);
        super.paintChildren(g);
    }

    /** Aba de navegação: rótulo em caixa alta; a ativa leva sublinhado de 4 px. */
    private static final class AbaBotao extends JButton {
        private final boolean ativa;
        private boolean sobre;

        AbaBotao(String texto, boolean ativa, Runnable acao, int mnemonico) {
            super(texto);
            this.ativa = ativa;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setMargin(new Insets(0, 0, 0, 0));
            setFont(Estilo.LABEL.fonte());
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addActionListener(e -> acao.run());
            setMnemonic(mnemonico);
            getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), "pressed");
            getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), "released");
            getAccessibleContext().setAccessibleName(texto + (ativa ? " (aba atual)" : ""));
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

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(getFont());
            return new Dimension(fm.stringWidth(Estilo.LABEL.texto(getText())) + px(32), px(56));
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(getPreferredSize().width, Integer.MAX_VALUE);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Ui.suavizar(g);
            Tema t = Tema.atual();
            int w = getWidth();
            int h = getHeight();
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            String s = Estilo.LABEL.texto(getText());
            g.setColor(ativa || sobre ? t.ink : t.ink2);
            g.drawString(s, (w - fm.stringWidth(s)) / 2, (h - 2 - fm.getHeight()) / 2 + fm.getAscent());
            if (ativa) {
                g.setColor(t.ink);
                g.fillRect(0, h - 4, w, 4);
            }
            if (isFocusOwner()) {
                g.setColor(t.ink);
                Ui.borda(g, 0, 0, w, h - 2, t.foco);
            }
            g.dispose();
        }
    }
}
