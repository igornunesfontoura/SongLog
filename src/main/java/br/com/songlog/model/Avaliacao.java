package br.com.songlog.model;

import br.com.songlog.util.Validacao;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A nota de um usuário para uma faixa. Existe no máximo uma avaliação
 * por par (usuário, faixa); reavaliar atualiza a existente.
 */
public class Avaliacao {

    public static final double NOTA_MIN = 0.5;
    public static final double NOTA_MAX = 5.0;
    public static final double PASSO = 0.5;

    private int id;
    private final Usuario usuario;
    private final Faixa faixa;
    private double nota;
    private String comentario;
    private LocalDateTime dataAvaliacao;

    public Avaliacao(Usuario usuario, Faixa faixa, double nota, String comentario) {
        this(0, usuario, faixa, nota, comentario, LocalDateTime.now());
    }

    public Avaliacao(int id, Usuario usuario, Faixa faixa, double nota, String comentario, LocalDateTime dataAvaliacao) {
        validarNota(nota);
        this.id = id;
        this.usuario = Objects.requireNonNull(usuario, "usuario");
        this.faixa = Objects.requireNonNull(faixa, "faixa");
        this.nota = nota;
        this.comentario = Validacao.textoOpcional(comentario, "Comentário", 500);
        this.dataAvaliacao = Objects.requireNonNull(dataAvaliacao, "dataAvaliacao");
    }

    /** Aceita de 0,5 a 5,0 em passos de 0,5. */
    public static void validarNota(double nota) {
        double passos = nota / PASSO;
        boolean multiploDoPasso = Math.abs(passos - Math.rint(passos)) < 1e-9;
        if (nota < NOTA_MIN || nota > NOTA_MAX || !multiploDoPasso) {
            throw new IllegalArgumentException("A nota deve estar entre 0,5 e 5,0, em passos de 0,5.");
        }
    }

    public void atualizar(double novaNota, String novoComentario) {
        validarNota(novaNota);
        this.nota = novaNota;
        this.comentario = Validacao.textoOpcional(novoComentario, "Comentário", 500);
        this.dataAvaliacao = LocalDateTime.now();
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

    public Faixa getFaixa() {
        return faixa;
    }

    public double getNota() {
        return nota;
    }

    public String getComentario() {
        return comentario;
    }

    public LocalDateTime getDataAvaliacao() {
        return dataAvaliacao;
    }
}
