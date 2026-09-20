package br.com.songlog.service;

import br.com.songlog.model.Album;
import br.com.songlog.model.Avaliacao;
import br.com.songlog.model.EntradaDiario;
import br.com.songlog.model.Usuario;
import br.com.songlog.util.Formatos;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.OptionalDouble;
import java.util.Set;

/** Diário de audição e estatísticas do perfil (RF07 a RF10, RN08 a RN10). */
public class PerfilService {

    public record Estatisticas(int albunsOuvidos, int faixasAvaliadas, OptionalDouble notaMedia, int reaudicoes) {
    }

    public Estatisticas estatisticas(Usuario u) {
        Set<Integer> albuns = new HashSet<>();
        int reaudicoes = 0;
        for (EntradaDiario e : u.getDiario()) {
            albuns.add(e.getAlbum().getId());
            if (e.isReaudicao()) {
                reaudicoes++;
            }
        }
        OptionalDouble media = u.getAvaliacoes().stream().mapToDouble(Avaliacao::getNota).average();
        return new Estatisticas(albuns.size(), u.getAvaliacoes().size(), media, reaudicoes);
    }

    public EntradaDiario registrarAudicao(Usuario u, Album album, String dataTexto, String comentario) {
        if (album == null) {
            throw new ValidacaoException("album", "escolha o álbum.");
        }
        LocalDate data = interpretarData(dataTexto);
        validarComentario(comentario);
        return u.registrarAudicao(album, data, comentario);
    }

    public void editarEntrada(EntradaDiario entrada, String dataTexto, String comentario) {
        LocalDate data = interpretarData(dataTexto);
        validarComentario(comentario);
        entrada.setDataAudicao(data);
        entrada.setComentario(comentario);
    }

    public void excluirEntrada(Usuario u, EntradaDiario entrada) {
        u.removerEntrada(entrada);
    }

    private static LocalDate interpretarData(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new ValidacaoException("data", "informe a data da audição.");
        }
        LocalDate data;
        try {
            data = Formatos.interpretarData(texto);
        } catch (IllegalArgumentException e) {
            throw new ValidacaoException("data", e.getMessage());
        }
        if (data.isAfter(LocalDate.now())) {
            throw new ValidacaoException("data", "a data de audição não pode estar no futuro.");
        }
        return data;
    }

    private static void validarComentario(String comentario) {
        if (comentario != null && comentario.strip().length() > 500) {
            throw new ValidacaoException("comentario", "o comentário passa de 500 caracteres.");
        }
    }
}
