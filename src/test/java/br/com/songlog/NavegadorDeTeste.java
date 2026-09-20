package br.com.songlog;

import br.com.songlog.model.Album;
import br.com.songlog.service.Contexto;
import br.com.songlog.view.Navegador;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

/** Navegador falso para exercitar as telas sem abrir janela: só registra os pedidos de navegação. */
public class NavegadorDeTeste implements Navegador {

    public final Contexto ctx;
    public final List<String> destinos = new ArrayList<>();
    public String status = "";
    public Album ultimoAlbum;

    public NavegadorDeTeste(Contexto ctx) {
        this.ctx = ctx;
    }

    @Override
    public void irParaCatalogo() {
        destinos.add("catalogo");
    }

    @Override
    public void irParaAlbum(Album album) {
        ultimoAlbum = album;
        destinos.add(album == null ? "album:novo" : "album:editar");
    }

    @Override
    public void irParaAvaliacao(Album album) {
        ultimoAlbum = album;
        destinos.add("avaliacao");
    }

    @Override
    public void irParaPerfil() {
        destinos.add("perfil");
    }

    @Override
    public void sair() {
        destinos.add("sair");
    }

    @Override
    public void status(String mensagem) {
        this.status = mensagem;
    }

    @Override
    public void abrirPreferencias() {
        destinos.add("preferencias");
    }

    @Override
    public void mostrarAtalhos() {
        destinos.add("atalhos");
    }

    @Override
    public Window janela() {
        return null;
    }

    @Override
    public Contexto contexto() {
        return ctx;
    }
}
