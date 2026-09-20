package br.com.songlog.view;

import static br.com.songlog.view.Preferencias.px;

import br.com.songlog.view.Fontes.Estilo;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

/** Utilitários de montagem de tela: textos, caixas, espaços e filetes. */
public final class Ui {

    private Ui() {
    }

    public static void suavizar(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    /** Retângulo de borda por dentro do espaço dado, com espessura em pixels. */
    public static void borda(Graphics2D g, int x, int y, int w, int h, int esp) {
        g.fillRect(x, y, w, esp);
        g.fillRect(x, y + h - esp, w, esp);
        g.fillRect(x, y + esp, esp, h - 2 * esp);
        g.fillRect(x + w - esp, y + esp, esp, h - 2 * esp);
    }

    public static JLabel texto(String s, Estilo estilo, Color cor) {
        JLabel l = new JLabel(estilo.texto(s));
        l.setFont(estilo.fonte());
        l.setForeground(cor);
        folga(l, estilo);
        return l;
    }

    public static JLabel texto(String s, Estilo estilo, Color cor, int tamanhoBase) {
        JLabel l = new JLabel(estilo.texto(s));
        l.setFont(estilo.fonte(tamanhoBase));
        l.setForeground(cor);
        folga(l, estilo);
        return l;
    }

    /** A largura calculada pelo Swing pode ser ~1 px menor que a desenhada: sem folga o último caractere é cortado. */
    private static void folga(JLabel l, Estilo estilo) {
        l.setBorder(new EmptyBorder(0, 0, 0, 2));
    }

    /** Painel transparente que não estica na vertical dentro de um BoxLayout. */
    public static JPanel painel() {
        JPanel p = new JPanel() {
            @Override
            public Dimension getMaximumSize() {
                Dimension d = super.getMaximumSize();
                return new Dimension(d.width, getPreferredSize().height);
            }
        };
        p.setOpaque(false);
        return p;
    }

    /** Painel que ocupa toda a largura disponível (o BoxLayout, sozinho, limita a largura ao maior filho). */
    public static class Bloco extends JPanel {
        public Bloco() {
            setOpaque(false);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }
    }

    /** Coloca o componente à esquerda de uma faixa que ocupa toda a largura, com a borda dada (ex.: filete). */
    public static JPanel faixa(JComponent c, Border borda) {
        JPanel p = new JPanel(new java.awt.BorderLayout());
        p.setOpaque(false);
        p.setBorder(borda);
        p.add(c, java.awt.BorderLayout.WEST);
        return p;
    }

    /** Linha com os componentes encostados à direita, separados por {@code espacoPx}. */
    public static JPanel linhaADireita(int espacoPx, Component... filhos) {
        JPanel p = linha(cola());
        for (int i = 0; i < filhos.length; i++) {
            if (i > 0) {
                p.add(espaco(espacoPx, 0));
            }
            p.add(filhos[i]);
        }
        return p;
    }

    public static JPanel coluna(Component... filhos) {
        JPanel p = painel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        for (Component c : filhos) {
            if (c instanceof JComponent jc) {
                jc.setAlignmentX(Component.LEFT_ALIGNMENT);
            }
            p.add(c);
        }
        return p;
    }

    public static JPanel linha(Component... filhos) {
        JPanel p = painel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        for (Component c : filhos) {
            if (c instanceof JComponent jc) {
                jc.setAlignmentY(Component.CENTER_ALIGNMENT);
            }
            p.add(c);
        }
        return p;
    }

    public static Component espaco(int largura, int altura) {
        return Box.createRigidArea(new Dimension(px(largura), px(altura)));
    }

    public static Component cola() {
        return Box.createGlue();
    }

    public static Border filete(Color cor, int cima, int esq, int baixo, int dir) {
        return new MatteBorder(cima, esq, baixo, dir, cor);
    }

    public static Border margem(int cima, int esq, int baixo, int dir) {
        return new EmptyBorder(px(cima), px(esq), px(baixo), px(dir));
    }

    /** Impede que o BoxLayout estique o componente na vertical. */
    public static <T extends JComponent> T alturaFixa(T c) {
        Dimension d = c.getPreferredSize();
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, d.height));
        return c;
    }

    /** Impede que o BoxLayout estique o componente na horizontal e na vertical. */
    public static <T extends JComponent> T tamanhoFixo(T c) {
        c.setMaximumSize(c.getPreferredSize());
        return c;
    }
}
