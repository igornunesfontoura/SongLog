package br.com.songlog.util;

/**
 * Validações de entrada compartilhadas pelas classes de domínio.
 */
public final class Validacao {

    private Validacao() {
    }

    /** Exige texto não vazio e devolve o valor sem espaços nas pontas. */
    public static String textoObrigatorio(String valor, String campo, int max) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " é obrigatório.");
        }
        String limpo = valor.strip();
        if (limpo.length() > max) {
            throw new IllegalArgumentException(campo + " excede " + max + " caracteres.");
        }
        return limpo;
    }

    /** Texto opcional: vazio vira {@code null}. */
    public static String textoOpcional(String valor, String campo, int max) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return textoObrigatorio(valor, campo, max);
    }
}
