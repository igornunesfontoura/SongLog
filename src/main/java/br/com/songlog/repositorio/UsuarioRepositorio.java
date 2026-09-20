package br.com.songlog.repositorio;

import br.com.songlog.model.Usuario;
import java.util.Optional;

/** Acesso a usuários; o usuário lido já traz as suas avaliações e o seu diário. */
public interface UsuarioRepositorio {

    Optional<Usuario> porEmail(String email);

    Optional<Usuario> porId(int id);

    /** Insere o usuário (id == 0) e atribui o id gerado. */
    Usuario salvar(Usuario usuario);
}
