package br.com.songlog.util;

/**
 * Formatação de durações. O banco guarda segundos; a interface mostra m:ss ou h:mm:ss.
 */
public final class Tempo {

    private Tempo() {
    }

    public static String formatar(int totalSegundos) {
        int horas = totalSegundos / 3600;
        int minutos = (totalSegundos % 3600) / 60;
        int segundos = totalSegundos % 60;
        if (horas > 0) {
            return String.format("%d:%02d:%02d", horas, minutos, segundos);
        }
        return String.format("%d:%02d", minutos, segundos);
    }
}
