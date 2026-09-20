package br.com.songlog.repositorio;

import br.com.songlog.model.Usuario;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class UsuarioMemoria implements UsuarioRepositorio {

    private final List<Usuario> usuarios = new ArrayList<>();
    private int proximoId = 1;

    @Override
    public List<Usuario> todos() {
        return Collections.unmodifiableList(usuarios);
    }

    @Override
    public Optional<Usuario> porEmail(String email) {
        String e = email == null ? "" : email.strip().toLowerCase();
        return usuarios.stream().filter(u -> u.getEmail().equals(e)).findFirst();
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        if (usuario.getId() == 0) {
            usuario.setId(proximoId++);
            usuarios.add(usuario);
        }
        return usuario;
    }
}
