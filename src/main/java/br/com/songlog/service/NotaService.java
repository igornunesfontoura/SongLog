package br.com.songlog.service;

import br.com.songlog.model.Album;
import br.com.songlog.model.Avaliacao;
import br.com.songlog.model.Faixa;
import br.com.songlog.model.Usuario;
import java.util.Optional;

/**
 * Nota ponderada de um usuário para um álbum (RN05 e RN06). A nota geral (RN07) é calculada
 * pelo banco, na view vw_nota_geral_album, e chega pelo AlbumRepositorio.
 */
public final class NotaService {

    /** Nota de um usuário para um álbum, ponderada pela duração das faixas que ele avaliou. */
    public record NotaAlbum(double valor, int faixasAvaliadas, int totalFaixas) {
        public boolean completa() {
            return faixasAvaliadas == totalFaixas;
        }
    }

    private NotaService() {
    }

    /**
     * Média ponderada pela duração: soma(nota * duração) / soma(duração),
     * considerando só as faixas avaliadas. Vazio se o usuário não avaliou nenhuma.
     */
    public static Optional<NotaAlbum> notaDoUsuario(Album album, Usuario usuario) {
        double somaPonderada = 0;
        long somaPesos = 0;
        int avaliadas = 0;

        for (Faixa faixa : album.getFaixas()) {
            Optional<Avaliacao> avaliacao = usuario.getAvaliacaoDe(faixa);
            if (avaliacao.isPresent()) {
                somaPonderada += avaliacao.get().getNota() * faixa.getDuracaoSegundos();
                somaPesos += faixa.getDuracaoSegundos();
                avaliadas++;
            }
        }

        if (avaliadas == 0) {
            return Optional.empty();
        }
        return Optional.of(new NotaAlbum(somaPonderada / somaPesos, avaliadas, album.getTotalFaixas()));
    }
}
