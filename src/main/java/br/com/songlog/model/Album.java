package br.com.songlog.model;

import br.com.songlog.util.Tempo;
import br.com.songlog.util.Validacao;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Um álbum do catálogo. É dono das suas faixas (composição): sem álbum, a faixa não existe.
 */
public class Album {

    private static final int PRIMEIRO_ANO_GRAVADO = 1877;

    private int id;
    private String titulo;
    private String artista;
    private int anoLancamento;
    private String genero;
    private final List<Faixa> faixas = new ArrayList<>();

    public Album(String titulo, String artista, int anoLancamento, String genero) {
        this(0, titulo, artista, anoLancamento, genero);
    }

    public Album(int id, String titulo, String artista, int anoLancamento, String genero) {
        this.id = id;
        this.titulo = Validacao.textoObrigatorio(titulo, "Título do álbum", 120);
        this.artista = Validacao.textoObrigatorio(artista, "Artista", 120);
        this.anoLancamento = validarAno(anoLancamento);
        this.genero = Validacao.textoOpcional(genero, "Gênero", 40);
    }

    /** Cria a próxima faixa do álbum (numeração sequencial a partir de 1). */
    public Faixa adicionarFaixa(String titulo, int duracaoSegundos) {
        Faixa faixa = new Faixa(faixas.size() + 1, titulo, duracaoSegundos);
        faixas.add(faixa);
        return faixa;
    }

    public int getTotalFaixas() {
        return faixas.size();
    }

    public int getDuracaoTotalSegundos() {
        return faixas.stream().mapToInt(Faixa::getDuracaoSegundos).sum();
    }

    public String getDuracaoTotalFormatada() {
        return Tempo.formatar(getDuracaoTotalSegundos());
    }

    public List<Faixa> getFaixas() {
        return Collections.unmodifiableList(faixas);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = Validacao.textoObrigatorio(titulo, "Título do álbum", 120);
    }

    public String getArtista() {
        return artista;
    }

    public void setArtista(String artista) {
        this.artista = Validacao.textoObrigatorio(artista, "Artista", 120);
    }

    public int getAnoLancamento() {
        return anoLancamento;
    }

    public void setAnoLancamento(int anoLancamento) {
        this.anoLancamento = validarAno(anoLancamento);
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = Validacao.textoOpcional(genero, "Gênero", 40);
    }

    private static int validarAno(int ano) {
        int maximo = Year.now().getValue() + 1;
        if (ano < PRIMEIRO_ANO_GRAVADO || ano > maximo) {
            throw new IllegalArgumentException("Ano de lançamento deve estar entre " + PRIMEIRO_ANO_GRAVADO + " e " + maximo + ".");
        }
        return ano;
    }

    // Igualdade por id; ver comentário em Faixa.
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Album outro && id != 0 && id == outro.id;
    }

    @Override
    public int hashCode() {
        return Album.class.hashCode();
    }

    @Override
    public String toString() {
        return artista + " — " + titulo + " (" + anoLancamento + ")";
    }
}
