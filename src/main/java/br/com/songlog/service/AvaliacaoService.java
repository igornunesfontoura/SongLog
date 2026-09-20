package br.com.songlog.service;

import br.com.songlog.model.Avaliacao;
import br.com.songlog.model.Faixa;
import br.com.songlog.model.Usuario;
import br.com.songlog.repositorio.AvaliacaoRepositorio;

/**
 * Notas por faixa (RF05, RF10). A regra é sempre a mesma: valida, grava no banco e só então
 * altera o usuário em memória, para os dois nunca ficarem diferentes se o banco falhar.
 */
public class AvaliacaoService {

    private final AvaliacaoRepositorio avaliacoes;

    public AvaliacaoService(AvaliacaoRepositorio avaliacoes) {
        this.avaliacoes = avaliacoes;
    }

    /** Cria ou atualiza a avaliação do usuário para a faixa (RN03, RN04). */
    public Avaliacao avaliar(Usuario usuario, Faixa faixa, double nota, String comentario) {
        Avaliacao nova = new Avaliacao(usuario, faixa, nota, comentario); // valida a escala e o comentário
        avaliacoes.salvar(nova);
        usuario.aplicarAvaliacao(nova);
        return nova;
    }

    /** Muda só o comentário; a faixa precisa já ter nota. */
    public Avaliacao comentar(Usuario usuario, Faixa faixa, String comentario) {
        Avaliacao atual = usuario.getAvaliacaoDe(faixa)
                .orElseThrow(() -> new ValidacaoException("comentario", "dê uma nota à faixa antes de comentar."));
        return avaliar(usuario, faixa, atual.getNota(), comentario);
    }

    /** "Limpar": remove a nota da faixa. */
    public void limpar(Usuario usuario, Faixa faixa) {
        avaliacoes.remover(usuario, faixa);
        usuario.removerAvaliacao(faixa);
    }
}
