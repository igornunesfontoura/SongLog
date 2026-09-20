package br.com.songlog.view.componentes;

import static br.com.songlog.view.Preferencias.px;

import br.com.songlog.model.Avaliacao;
import br.com.songlog.view.Tema;
import br.com.songlog.view.Ui;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleValue;
import javax.swing.JComponent;

/**
 * Nota de uma faixa: 10 células, cada uma vale 0,5 (de 0,5 a 5,0).
 * Mouse: clicar numa célula define a nota. Teclado: setas ou +/- mudam 0,5; digitar 4 ou 4,5
 * define o valor; Enter segue para a próxima faixa; Delete limpa; Esc volta ao valor inicial.
 * Implementa AccessibleValue para leitores de tela.
 */
public class BarraDeNota extends JComponent implements Accessible {

    public interface Ouvinte {
        /** Chamado a cada mudança; {@code nota} 0 significa "sem nota". */
        void notaAlterada(double nota);
    }

    private static final int CELULAS = 10;

    private double nota;
    private double previa = -1;
    private double valorAoEntrar;
    private String digitado = "";
    private Ouvinte ouvinte;
    private Runnable aoConfirmar;
    private java.util.function.IntConsumer aoNavegar;

    public BarraDeNota(double notaInicial, String nomeAcessivel) {
        this.nota = notaInicial;
        setFocusable(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        getAccessibleContext().setAccessibleName(nomeAcessivel);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                int i = celulaEm(e.getX());
                if (i >= 0) {
                    definirPorUsuario((i + 1) * 0.5);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                previa = -1;
                repaint();
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int i = celulaEm(e.getX());
                previa = i >= 0 ? (i + 1) * 0.5 : -1;
                repaint();
            }
        });
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                valorAoEntrar = nota;
                digitado = "";
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                digitado = "";
                repaint();
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_RIGHT -> ajustar(0.5);
                    case KeyEvent.VK_LEFT -> ajustar(-0.5);
                    case KeyEvent.VK_UP -> navegar(-1);
                    case KeyEvent.VK_DOWN -> navegar(1);
                    case KeyEvent.VK_ENTER -> {
                        if (aoConfirmar != null) {
                            aoConfirmar.run();
                        } else {
                            transferFocus();
                        }
                    }
                    case KeyEvent.VK_DELETE, KeyEvent.VK_BACK_SPACE -> definirPorUsuario(0);
                    case KeyEvent.VK_ESCAPE -> {
                        if (nota != valorAoEntrar) {
                            definirPorUsuario(valorAoEntrar);
                        }
                    }
                    default -> {
                    }
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (c == '+') {
                    ajustar(0.5);
                } else if (c == '-') {
                    ajustar(-0.5);
                } else if (Character.isDigit(c) || c == ',' || c == '.') {
                    digitar(c);
                }
            }
        });
    }

    public void setOuvinte(Ouvinte ouvinte) {
        this.ouvinte = ouvinte;
    }

    /** Enter: a tela decide para onde vai o foco (normalmente a próxima faixa). */
    public void setAoConfirmar(Runnable aoConfirmar) {
        this.aoConfirmar = aoConfirmar;
    }

    /** Setas ↑ e ↓: -1 = faixa anterior, +1 = próxima. */
    public void setAoNavegar(java.util.function.IntConsumer aoNavegar) {
        this.aoNavegar = aoNavegar;
    }

    private void navegar(int delta) {
        if (aoNavegar != null) {
            aoNavegar.accept(delta);
        }
    }

    public double getNota() {
        return nota;
    }

    /** Define o valor sem avisar o ouvinte (uso da tela ao montar ou recarregar). */
    public void setNota(double nota) {
        this.nota = nota;
        repaint();
    }

    private void ajustar(double delta) {
        digitado = "";
        double base = nota <= 0 ? 0 : nota;
        double novo = Math.max(Avaliacao.NOTA_MIN, Math.min(Avaliacao.NOTA_MAX, base + delta));
        if (base == 0 && delta < 0) {
            return;
        }
        definirPorUsuario(novo);
    }

    private void digitar(char c) {
        digitado += c == ',' ? '.' : c;
        if (digitado.matches("[0-5]")) {
            double v = Double.parseDouble(digitado);
            if (v >= Avaliacao.NOTA_MIN) {
                definirPorUsuario(v);
            }
        } else if (digitado.matches("[0-5]\\.[05]")) {
            definirPorUsuario(Double.parseDouble(digitado));
            digitado = "";
        } else if (!digitado.matches("[0-5]\\.")) {
            digitado = "";
        }
    }

    private void definirPorUsuario(double novo) {
        if (novo != 0) {
            Avaliacao.validarNota(novo);
        }
        if (novo == nota) {
            return;
        }
        nota = novo;
        repaint();
        firePropertyChange("nota", -1.0, novo);
        if (ouvinte != null) {
            ouvinte.notaAlterada(novo);
        }
    }

    private int celulaEm(int x) {
        int pad = 2 * Tema.atual().foco;
        int passo = px(24) + px(2);
        int i = (x - pad) / passo;
        boolean dentro = (x - pad) >= 0 && ((x - pad) % passo) < px(24);
        return dentro && i >= 0 && i < CELULAS ? i : -1;
    }

    @Override
    public Dimension getPreferredSize() {
        int pad = 2 * Tema.atual().foco;
        return new Dimension(CELULAS * px(24) + (CELULAS - 1) * px(2) + 2 * pad, px(28) + 2 * pad);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Tema t = Tema.atual();
        int pad = 2 * t.foco;
        double mostrada = previa >= 0 ? previa : nota;
        for (int i = 0; i < CELULAS; i++) {
            int x = pad + i * (px(24) + px(2));
            if (i < Math.round(mostrada * 2)) {
                g.setColor(t.ink);
                g.fillRect(x, pad, px(24), px(28));
            } else {
                g.setColor(t.rule);
                Ui.borda(g, x, pad, px(24), px(28), 1);
            }
        }
        if (isFocusOwner()) {
            g.setColor(t.ink);
            Ui.borda(g, 0, 0, getWidth(), getHeight(), t.foco);
        }
        g.dispose();
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleBarra();
        }
        return accessibleContext;
    }

    protected class AccessibleBarra extends AccessibleJComponent implements AccessibleValue {
        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.SLIDER;
        }

        @Override
        public AccessibleValue getAccessibleValue() {
            return this;
        }

        @Override
        public Number getCurrentAccessibleValue() {
            return nota;
        }

        @Override
        public boolean setCurrentAccessibleValue(Number n) {
            try {
                definirPorUsuario(n.doubleValue());
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        @Override
        public Number getMinimumAccessibleValue() {
            return Avaliacao.NOTA_MIN;
        }

        @Override
        public Number getMaximumAccessibleValue() {
            return Avaliacao.NOTA_MAX;
        }
    }
}
