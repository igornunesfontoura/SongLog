package br.com.songlog.view;

import java.awt.Color;

/**
 * Tokens de cor do projeto de interfaces (docs/design-tokens.json). O tema ativo é lido
 * na hora de pintar; trocar de tema reconstrói a janela.
 */
public enum Tema {
    CLARO("Claro", "F4F1EA", "E9E4D8", "141414", "4A463E", "645F55", "7F796D", "CFC9BA", "B92D19", "951F0E", "F4F1EA", "8C8678", 2),
    ESCURO("Escuro", "121110", "1D1B18", "F1EEE6", "BDB7AA", "9A9486", "7D7869", "3A3833", "FF7A5C", "FF9478", "121110", "6A665B", 2),
    ALTO_CONTRASTE("Alto contraste", "FFFFFF", "F0F0F0", "000000", "1A1A1A", "333333", "000000", "767676", "B00020", "800016", "FFFFFF", "595959", 3);

    public final String nome;
    public final Color bg, surface, ink, ink2, mute, rule, ruleSoft, accent, accentHover, onAccent, disabled;
    /** Espessura do anel de foco, em pixels. */
    public final int foco;

    private static Tema atual = CLARO;

    Tema(String nome, String bg, String surface, String ink, String ink2, String mute, String rule, String ruleSoft,
            String accent, String accentHover, String onAccent, String disabled, int foco) {
        this.nome = nome;
        this.bg = cor(bg);
        this.surface = cor(surface);
        this.ink = cor(ink);
        this.ink2 = cor(ink2);
        this.mute = cor(mute);
        this.rule = cor(rule);
        this.ruleSoft = cor(ruleSoft);
        this.accent = cor(accent);
        this.accentHover = cor(accentHover);
        this.onAccent = cor(onAccent);
        this.disabled = cor(disabled);
        this.foco = foco;
    }

    private static Color cor(String hex) {
        return new Color(Integer.parseInt(hex, 16));
    }

    public static Tema atual() {
        return atual;
    }

    public static void definir(Tema tema) {
        atual = tema;
    }

    @Override
    public String toString() {
        return nome;
    }
}
