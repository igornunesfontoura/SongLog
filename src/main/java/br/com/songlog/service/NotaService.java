package br.com.songlog.service;

import br.com.songlog.model.Album;
import br.com.songlog.model.Avaliacao;
import br.com.songlog.model.Faixa;
import br.com.songlog.model.Usuario;
import java.util.Collection;
import java.util.Optional;

/**
 * Regras de cálculo das notas de álbum (RN04 e RN05 do documento de projeto).
 */
public final class NotaService {

    /** Nota de um usuário para um álbum, ponderada pela duração das faixas que ele avaliou. */
    public record NotaAlbum(double valor, int faixasAvaliadas, int totalFaixas) {
        public boolean completa() {
            return faixasAvaliadas == totalFaixas;
        }
    }

    /** Nota geral do álbum: média das notas dos usuários que avaliaram todas as faixas. */
    public record NotaGeral(double valor, int totalAvaliadores) {
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

    /**
     * Média simples das notas de álbum dos usuários com avaliação completa.
     * Avaliações parciais ficam de fora para não distorcer o resultado.
     */
    public static Optional<NotaGeral> notaGeral(Album album, Collection<Usuario> usuarios) {
        double soma = 0;
        int avaliadores = 0;

        for (Usuario usuario : usuarios) {
            Optional<NotaAlbum> nota = notaDoUsuario(album, usuario);
            if (nota.isPresent() && nota.get().completa()) {
                soma += nota.get().valor();
                avaliadores++;
            }
        }

        if (avaliadores == 0) {
            return Optional.empty();
        }
        return Optional.of(new NotaGeral(soma / avaliadores, avaliadores));
    }
}
