package br.com.songlog.banco;

/**
 * Falha ao falar com o banco de dados. A mensagem já está em português e pronta para o usuário
 * ("usuário ou senha do MySQL incorretos", "não foi possível conectar ao MySQL em localhost:3306"...).
 */
public class BancoException extends RuntimeException {

    public BancoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
