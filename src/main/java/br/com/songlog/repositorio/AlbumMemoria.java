package br.com.songlog.repositorio;

import br.com.songlog.model.Album;
import br.com.songlog.model.Faixa;
import br.com.songlog.util.Formatos;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AlbumMemoria implements AlbumRepositorio {

    private final List<Album> albuns = new ArrayList<>();
    private int proximoIdAlbum = 1;
    private int proximoIdFaixa = 1;

    @Override
    public List<Album> todos() {
        return Collections.unmodifiableList(albuns);
    }

    @Override
    public Optional<Album> porId(int id) {
        return albuns.stream().filter(a -> a.getId() == id).findFirst();
    }

    @Override
    public Album salvar(Album album) {
        if (album.getId() == 0) {
            album.setId(proximoIdAlbum++);
            albuns.add(album);
        }
        for (Faixa faixa : album.getFaixas()) {
            if (faixa.getId() == 0) {
                faixa.setId(proximoIdFaixa++);
            }
        }
        return album;
    }

    @Override
    public void excluir(Album album) {
        albuns.remove(album);
    }

    @Override
    public boolean existeDuplicado(String titulo, String artista, int ano, Album ignorar) {
        String t = Formatos.semAcentos(titulo.strip());
        String a = Formatos.semAcentos(artista.strip());
        return albuns.stream().anyMatch(o -> o != ignorar
                && o.getAnoLancamento() == ano
                && Formatos.semAcentos(o.getTitulo()).equals(t)
                && Formatos.semAcentos(o.getArtista()).equals(a));
    }
}
