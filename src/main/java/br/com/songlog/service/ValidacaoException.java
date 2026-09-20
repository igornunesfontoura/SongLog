package br.com.songlog.service;

/**
 * Erro de regra de negócio ou de entrada, com o campo afetado (pode ser {@code null})
 * e uma mensagem pronta para mostrar ao usuário.
 */
public class ValidacaoException extends RuntimeException {

    private final String campo;

    public ValidacaoException(String campo, String mensagem) {
        super(mensagem);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
