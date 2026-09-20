package br.com.songlog.service;

import br.com.songlog.model.Usuario;
import br.com.songlog.repositorio.UsuarioRepositorio;
import br.com.songlog.util.SenhaUtil;
import java.util.Optional;
import java.util.regex.Pattern;

/** Login e cadastro (RF01, RN11). A senha só existe em texto puro dentro deste método. */
public class AutenticacaoService {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    public static final int TAMANHO_MINIMO_SENHA = 8;

    private final UsuarioRepositorio usuarios;

    public AutenticacaoService(UsuarioRepositorio usuarios) {
        this.usuarios = usuarios;
    }

    /** Mensagem genérica de propósito: não revela se o e-mail existe. */
    public Usuario entrar(String email, String senha) {
        Optional<Usuario> u = usuarios.porEmail(email);
        if (email == null || email.isBlank()) {
            throw new ValidacaoException("email", "informe o e-mail.");
        }
        if (senha == null || senha.isEmpty()) {
            throw new ValidacaoException("senha", "informe a senha.");
        }
        if (u.isEmpty() || !SenhaUtil.verificar(senha, u.get().getSenhaHash())) {
            throw new ValidacaoException(null, "e-mail ou senha incorretos.");
        }
        return u.get();
    }

    public Usuario cadastrar(String nome, String email, String senha, String confirmacao) {
        if (nome == null || nome.isBlank()) {
            throw new ValidacaoException("nome", "informe seu nome.");
        }
        if (email == null || !EMAIL.matcher(email.strip()).matches()) {
            throw new ValidacaoException("email", "informe um e-mail válido.");
        }
        if (usuarios.porEmail(email).isPresent()) {
            throw new ValidacaoException("email", "este e-mail já está cadastrado.");
        }
        if (senha == null || senha.length() < TAMANHO_MINIMO_SENHA) {
            throw new ValidacaoException("senha", "use ao menos " + TAMANHO_MINIMO_SENHA + " caracteres.");
        }
        if (!senha.equals(confirmacao)) {
            throw new ValidacaoException("confirmacao", "as senhas não conferem.");
        }
        return usuarios.salvar(new Usuario(nome, email, SenhaUtil.gerarHash(senha)));
    }
}
