package br.com.songlog.view;

/** Preferências de exibição: tamanho do texto (100%, 125%, 150%). O tema fica em {@link Tema}. */
public final class Preferencias {

    private static double escala = 1.0;

    private Preferencias() {
    }

    public static double escala() {
        return escala;
    }

    public static void definirEscala(double e) {
        escala = e;
    }

    /** Escala uma medida em pixels conforme o tamanho de texto escolhido. */
    public static int px(int valor) {
        return (int) Math.round(valor * escala);
    }
}
