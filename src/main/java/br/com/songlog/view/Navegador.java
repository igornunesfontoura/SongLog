package br.com.songlog.view;

import br.com.songlog.model.Album;
import br.com.songlog.service.Contexto;
import java.awt.Window;

/** O que as telas podem pedir à janela: navegar, mostrar mensagem de status e abrir diálogos globais. */
public interface Navegador {

    void irParaCatalogo();

    /** {@code album == null} abre o formulário de novo álbum. */
    void irParaAlbum(Album album);

    void irParaAvaliacao(Album album);

    void irParaPerfil();

    void sair();

    /** Mensagem persistente na barra de status (some só quando outra ação a substitui). */
    void status(String mensagem);

    void abrirPreferencias();

    void mostrarAtalhos();

    Window janela();

    Contexto contexto();
}
