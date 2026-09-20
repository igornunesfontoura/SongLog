package br.com.songlog.service;

import br.com.songlog.banco.Banco;
import br.com.songlog.model.Usuario;
import br.com.songlog.repositorio.AlbumRepositorio;
import br.com.songlog.repositorio.AvaliacaoRepositorio;
import br.com.songlog.repositorio.DiarioRepositorio;
import br.com.songlog.repositorio.UsuarioRepositorio;
import br.com.songlog.repositorio.jdbc.AlbumJdbc;
import br.com.songlog.repositorio.jdbc.AvaliacaoJdbc;
import br.com.songlog.repositorio.jdbc.DiarioJdbc;
import br.com.songlog.repositorio.jdbc.UsuarioJdbc;

/**
 * Reúne repositórios, serviços e o usuário logado. As telas recebem um Contexto e só usam os
 * serviços e as interfaces de repositório; nunca veem JDBC nem SQL.
 */
public class Contexto {

    public final Banco banco;
    public final UsuarioRepositorio usuarios;
    public final AlbumRepositorio albuns;
    public final CatalogoService catalogo;
    public final AutenticacaoService autenticacao;
    public final AvaliacaoService avaliacoes;
    public final PerfilService perfil;
    private Usuario usuarioLogado;

    public Contexto(Banco banco) {
        this.banco = banco;
        this.albuns = new AlbumJdbc(banco);
        this.usuarios = new UsuarioJdbc(banco, albuns);
        AvaliacaoRepositorio avaliacaoRepositorio = new AvaliacaoJdbc(banco);
        DiarioRepositorio diarioRepositorio = new DiarioJdbc(banco);
        this.catalogo = new CatalogoService(albuns);
        this.autenticacao = new AutenticacaoService(usuarios);
        this.avaliacoes = new AvaliacaoService(avaliacaoRepositorio);
        this.perfil = new PerfilService(diarioRepositorio);
    }

    public Usuario usuario() {
        return usuarioLogado;
    }

    public void entrar(Usuario u) {
        this.usuarioLogado = u;
    }

    public void sair() {
        this.usuarioLogado = null;
    }

    /**
     * Relê o usuário logado do banco (notas e diário). As telas que mostram esses dados chamam isto ao abrir,
     * para refletir exclusões em cascata (ex.: álbum excluído) e qualquer mudança feita fora do programa.
     */
    public void recarregarUsuario() {
        if (usuarioLogado != null) {
            usuarioLogado = usuarios.porId(usuarioLogado.getId()).orElse(usuarioLogado);
        }
    }
}
