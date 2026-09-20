package br.com.songlog.util;

/**
 * Formatação de durações. O banco guarda segundos; a interface mostra m:ss ou h:mm:ss.
 */
public final class Tempo {

    private Tempo() {
    }

    /**
     * Interpreta "m:ss" ou "h:mm:ss" (segundos de 00 a 59).
     * @throws IllegalArgumentException com mensagem pronta para a interface
     */
    public static int interpretar(String texto) {
        String msg = "use o formato m:ss, com segundos de 00 a 59 (ex.: 9:22).";
        if (texto == null || !texto.strip().matches("\\d{1,3}:[0-5]\\d(:[0-5]\\d)?")) {
            throw new IllegalArgumentException(msg);
        }
        String[] p = texto.strip().split(":");
        int total = p.length == 2
                ? Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1])
                : Integer.parseInt(p[0]) * 3600 + Integer.parseInt(p[1]) * 60 + Integer.parseInt(p[2]);
        if (total < 1) {
            throw new IllegalArgumentException("a duração deve ser maior que zero.");
        }
        return total;
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
