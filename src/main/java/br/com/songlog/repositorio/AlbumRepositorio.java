package br.com.songlog.repositorio;

import br.com.songlog.model.Album;
import java.util.List;
import java.util.Optional;

/**
 * Acesso a álbuns (e às suas faixas). Nesta etapa a implementação é em memória;
 * a implementação com MySQL/JDBC substituirá a atual sem mudar as telas.
 */
public interface AlbumRepositorio {

    List<Album> todos();

    Optional<Album> porId(int id);

    /** Insere (id == 0) ou atualiza; atribui ids ao álbum e às faixas novas. */
    Album salvar(Album album);

    void excluir(Album album);

    /** RN02: título + artista + ano únicos, ignorando o próprio álbum. */
    boolean existeDuplicado(String titulo, String artista, int ano, Album ignorar);
}
