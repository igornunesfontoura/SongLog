package br.com.songlog.model;

import br.com.songlog.util.Validacao;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Uma linha do diário de audição: "ouvi este álbum neste dia".
 * Independe de avaliação, e o mesmo álbum pode aparecer várias vezes (reaudição).
 */
public class EntradaDiario {

    private int id;
    private final Usuario usuario;
    private final Album album;
    private LocalDate dataAudicao;
    private String comentario;
    private final boolean reaudicao;

    public EntradaDiario(Usuario usuario, Album album, LocalDate dataAudicao, String comentario, boolean reaudicao) {
        this(0, usuario, album, dataAudicao, comentario, reaudicao);
    }

    public EntradaDiario(int id, Usuario usuario, Album album, LocalDate dataAudicao, String comentario, boolean reaudicao) {
        this.id = id;
        this.usuario = Objects.requireNonNull(usuario, "usuario");
        this.album = Objects.requireNonNull(album, "album");
        this.dataAudicao = validarData(dataAudicao);
        this.comentario = Validacao.textoOpcional(comentario, "Comentário", 500);
        this.reaudicao = reaudicao;
    }

    private static LocalDate validarData(LocalDate data) {
        Objects.requireNonNull(data, "dataAudicao");
        if (data.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A data de audição não pode estar no futuro.");
        }
        return data;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Album getAlbum() {
        return album;
    }

    public LocalDate getDataAudicao() {
        return dataAudicao;
    }

    public void setDataAudicao(LocalDate dataAudicao) {
        this.dataAudicao = validarData(dataAudicao);
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = Validacao.textoOpcional(comentario, "Comentário", 500);
    }

    public boolean isReaudicao() {
        return reaudicao;
    }
}
