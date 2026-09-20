package br.com.songlog.repositorio;

import br.com.songlog.model.Album;
import br.com.songlog.model.NotaGeral;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Acesso a álbuns e às suas faixas. A implementação de produção é {@code jdbc.AlbumJdbc}. */
public interface AlbumRepositorio {

    /** Todos os álbuns, com as faixas, por ordem de título. */
    List<Album> todos();

    /** Busca por título, artista ou gênero (ignora maiúsculas e acentos); {@code genero} vazio = todos. */
    List<Album> buscar(String texto, String genero);

    List<String> generos();

    Optional<Album> porId(int id);

    /** Insere (id == 0) ou atualiza o álbum e sincroniza as faixas, numa única transação. */
    Album salvar(Album album);

    /** Exclui o álbum; o banco leva junto faixas, avaliações e entradas de diário (RN13). */
    void excluir(Album album);

    /** RN02: título + artista + ano únicos, ignorando o próprio álbum. */
    boolean existeDuplicado(String titulo, String artista, int ano, Album ignorar);

    /** Nota geral (RN07) de todos os álbuns que já têm, por id do álbum. */
    Map<Integer, NotaGeral> notasGerais();

    Optional<NotaGeral> notaGeral(Album album);

    /** Quantas avaliações existem nas faixas deste álbum (para avisar antes de excluir). */
    int contarAvaliacoes(Album album);

    /** Quantas entradas de diário citam este álbum. */
    int contarEntradas(Album album);
}
