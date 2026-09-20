package br.com.songlog.repositorio;

import br.com.songlog.model.Avaliacao;
import br.com.songlog.model.Faixa;
import br.com.songlog.model.Usuario;

/** Gravação das notas de faixa. */
public interface AvaliacaoRepositorio {

    /** Insere ou atualiza a avaliação do par (usuário, faixa) e atribui o id. */
    void salvar(Avaliacao avaliacao);

    void remover(Usuario usuario, Faixa faixa);
}
