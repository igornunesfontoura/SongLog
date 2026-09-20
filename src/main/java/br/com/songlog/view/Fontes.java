package br.com.songlog.view;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Tipografia do projeto: Source Serif 4 (títulos e nota), Inter (interface) e
 * JetBrains Mono (números), embutidas em /fonts. Se uma fonte faltar, usa a genérica.
 */
public final class Fontes {

    private enum Familia {
        SERIF_SEMI("SourceSerif4-SemiBold.ttf", Font.SERIF, Font.BOLD),
        SERIF_ITALICO("SourceSerif4-Italic.ttf", Font.SERIF, Font.ITALIC),
        UI("Inter-Regular.ttf", Font.SANS_SERIF, Font.PLAIN),
        UI_SEMI("Inter-SemiBold.ttf", Font.SANS_SERIF, Font.BOLD),
        MONO("JetBrainsMono-Medium.ttf", Font.MONOSPACED, Font.PLAIN);

        final Font base;

        Familia(String arquivo, String reserva, int estiloReserva) {
            Font f;
            try (InputStream in = Fontes.class.getResourceAsStream("/fonts/" + arquivo)) {
                f = in == null ? null : Font.createFont(Font.TRUETYPE_FONT, in);
            } catch (IOException | java.awt.FontFormatException e) {
                f = null;
            }
            this.base = f != null ? f : new Font(reserva, estiloReserva, 12);
        }
    }

    /** Estilos de texto do documento de projeto (seção 4). */
    public enum Estilo {
        DISPLAY(Familia.SERIF_SEMI, 72, false),
        H1(Familia.SERIF_SEMI, 40, false),
        H2(Familia.SERIF_SEMI, 24, false),
        LABEL(Familia.UI_SEMI, 12, true),
        BODY(Familia.UI, 14, false),
        BODY_BOLD(Familia.UI_SEMI, 14, false),
        SMALL(Familia.UI, 13, false),
        MONO(Familia.MONO, 14, false),
        MONO_LG(Familia.MONO, 20, false),
        ITALICO(Familia.SERIF_ITALICO, 14, false);

        private final Familia familia;
        private final int tamanho;
        /** Rótulos: caixa alta com espaçamento de 6%. */
        public final boolean caixaAlta;

        Estilo(Familia familia, int tamanho, boolean caixaAlta) {
            this.familia = familia;
            this.tamanho = tamanho;
            this.caixaAlta = caixaAlta;
        }

        public Font fonte() {
            return criar(familia.base, escalar(tamanho), caixaAlta);
        }

        /** Mesma família, com outro tamanho em pixels (já escalado pela preferência). */
        public Font fonte(int tamanhoBase) {
            return criar(familia.base, escalar(tamanhoBase), caixaAlta);
        }

        /** O número grande (72 px) já é o maior elemento: cresce no máximo 25%, para não expulsar o resto da tela. */
        private int escalar(int base) {
            double e = this == DISPLAY ? Math.min(Preferencias.escala(), 1.25) : Preferencias.escala();
            return (int) Math.round(base * e);
        }

        public String texto(String s) {
            return caixaAlta ? s.toUpperCase(new Locale("pt", "BR")) : s;
        }
    }

    private Fontes() {
    }

    private static Font criar(Font base, int tamanho, boolean espacar) {
        Font f = base.deriveFont((float) tamanho);
        if (espacar) {
            Map<TextAttribute, Object> m = new HashMap<>();
            m.put(TextAttribute.TRACKING, 0.06f);
            f = f.deriveFont(m);
        }
        return f;
    }
}
