package br.com.songlog.service;

import br.com.songlog.model.Usuario;
import br.com.songlog.repositorio.AlbumMemoria;
import br.com.songlog.repositorio.AlbumRepositorio;
import br.com.songlog.repositorio.DadosIniciais;
import br.com.songlog.repositorio.UsuarioMemoria;
import br.com.songlog.repositorio.UsuarioRepositorio;

/**
 * Reúne repositórios, serviços e o usuário logado. As telas recebem um Contexto e nunca
 * conhecem a origem dos dados (memória agora, MySQL depois).
 */
public class Contexto {

    public final UsuarioRepositorio usuarios;
    public final AlbumRepositorio albuns;
    public final CatalogoService catalogo;
    public final AutenticacaoService autenticacao;
    public final PerfilService perfil = new PerfilService();
    private Usuario usuarioLogado;

    public Contexto(UsuarioRepositorio usuarios, AlbumRepositorio albuns) {
        this.usuarios = usuarios;
        this.albuns = albuns;
        this.catalogo = new CatalogoService(albuns, usuarios);
        this.autenticacao = new AutenticacaoService(usuarios);
    }

    /** Contexto com dados de demonstração em memória. */
    public static Contexto emMemoria() {
        Contexto ctx = new Contexto(new UsuarioMemoria(), new AlbumMemoria());
        DadosIniciais.carregar(ctx);
        return ctx;
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
}
