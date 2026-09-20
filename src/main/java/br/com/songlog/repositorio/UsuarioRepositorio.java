package br.com.songlog.repositorio;

import br.com.songlog.model.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepositorio {

    List<Usuario> todos();

    Optional<Usuario> porEmail(String email);

    /** Insere (id == 0) ou atualiza. */
    Usuario salvar(Usuario usuario);
}
