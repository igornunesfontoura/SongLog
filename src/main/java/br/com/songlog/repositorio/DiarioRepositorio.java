package br.com.songlog.repositorio;

import br.com.songlog.model.EntradaDiario;
import java.time.LocalDate;

/** Gravação do diário de audição. */
public interface DiarioRepositorio {

    /** Insere a entrada e atribui o id gerado. */
    void inserir(EntradaDiario entrada);

    void atualizar(EntradaDiario entrada, LocalDate novaData, String novoComentario);

    void excluir(EntradaDiario entrada);
}
