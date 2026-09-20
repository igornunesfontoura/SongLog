package br.com.songlog;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/** Utilitários de teste: localizar componentes, disparar teclas e esperar a interface. */
final class Achar {

    private Achar() {
    }

    static <T extends Component> List<T> todos(Component raiz, Class<T> tipo) {
        List<T> saida = new ArrayList<>();
        varrer(raiz, tipo, saida);
        return saida;
    }

    private static <T extends Component> void varrer(Component c, Class<T> tipo, List<T> saida) {
        if (tipo.isInstance(c)) {
            saida.add(tipo.cast(c));
        }
        if (c instanceof Container k) {
            for (Component f : k.getComponents()) {
                varrer(f, tipo, saida);
            }
        }
    }

    static AbstractButton botao(Component raiz, String texto) {
        return todos(raiz, AbstractButton.class).stream().filter(b -> texto.equals(b.getText())).findFirst()
                .orElseThrow(() -> new AssertionError("botão não encontrado: " + texto));
    }

    static boolean temTexto(Component raiz, String trecho) {
        return todos(raiz, JLabel.class).stream().anyMatch(l -> l.isShowing() || true ? l.getText() != null && l.getText().contains(trecho) : false)
                || todos(raiz, AbstractButton.class).stream().anyMatch(b -> b.getText() != null && b.getText().contains(trecho));
    }

    static void naInterface(Runnable r) {
        try {
            SwingUtilities.invokeAndWait(r);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Sem monitor não há foco, e o Swing descarta eventos de teclado; entregamos direto aos ouvintes. */
    static void pressionar(Component c, int codigo) {
        naInterface(() -> {
            KeyEvent e = new KeyEvent(c, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, codigo, KeyEvent.CHAR_UNDEFINED);
            for (KeyListener l : c.getKeyListeners()) {
                l.keyPressed(e);
            }
        });
    }

    static void digitar(Component c, char ch) {
        naInterface(() -> {
            KeyEvent e = new KeyEvent(c, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, ch);
            for (KeyListener l : c.getKeyListeners()) {
                l.keyTyped(e);
            }
        });
    }

    static void esperar(BooleanSupplier condicao, long ms) {
        long fim = System.currentTimeMillis() + ms;
        while (System.currentTimeMillis() < fim) {
            naInterface(() -> { });
            if (condicao.getAsBoolean()) {
                return;
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
