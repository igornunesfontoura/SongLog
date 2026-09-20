package br.com.songlog.model;

import br.com.songlog.util.Tempo;
import br.com.songlog.util.Validacao;

/**
 * Uma faixa de um álbum. A duração (em segundos) é também o peso da faixa
 * no cálculo da nota ponderada do álbum.
 */
public class Faixa {

    private int id;
    private int numero;
    private String titulo;
    private int duracaoSegundos;

    public Faixa(int numero, String titulo, int duracaoSegundos) {
        this(0, numero, titulo, duracaoSegundos);
    }

    public Faixa(int id, int numero, String titulo, int duracaoSegundos) {
        if (numero < 1) {
            throw new IllegalArgumentException("O número da faixa deve ser 1 ou maior.");
        }
        if (duracaoSegundos < 1) {
            throw new IllegalArgumentException("A duração da faixa deve ser maior que zero.");
        }
        this.id = id;
        this.numero = numero;
        this.titulo = Validacao.textoObrigatorio(titulo, "Título da faixa", 120);
        this.duracaoSegundos = duracaoSegundos;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumero() {
        return numero;
    }

    /** Uso interno do Album: refaz a numeração sequencial (RN01). */
    void renumerar(int novoNumero) {
        this.numero = novoNumero;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = Validacao.textoObrigatorio(titulo, "Título da faixa", 120);
    }

    public int getDuracaoSegundos() {
        return duracaoSegundos;
    }

    public void setDuracaoSegundos(int duracaoSegundos) {
        if (duracaoSegundos < 1) {
            throw new IllegalArgumentException("A duração da faixa deve ser maior que zero.");
        }
        this.duracaoSegundos = duracaoSegundos;
    }

    public String getDuracaoFormatada() {
        return Tempo.formatar(duracaoSegundos);
    }

    // Igualdade por id (chave do banco). Antes de persistir (id == 0) vale a identidade do objeto.
    // hashCode constante porque o id muda de 0 para o valor real ao salvar.
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Faixa outra && id != 0 && id == outra.id;
    }

    @Override
    public int hashCode() {
        return Faixa.class.hashCode();
    }

    @Override
    public String toString() {
        return numero + ". " + titulo + " (" + getDuracaoFormatada() + ")";
    }
}
