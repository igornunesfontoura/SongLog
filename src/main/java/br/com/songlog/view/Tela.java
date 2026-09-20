package br.com.songlog.view;

/** Ciclo de vida mínimo de uma tela. */
public interface Tela {

    /** Chamado antes de sair da tela; devolver {@code false} cancela a navegação (ex.: alterações não salvas). */
    default boolean podeSair() {
        return true;
    }

    /** Chamado quando a tela aparece; use para dar o foco inicial. */
    default void aoExibir() {
    }
}
