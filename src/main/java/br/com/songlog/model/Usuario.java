package br.com.songlog.model;

import br.com.songlog.util.Validacao;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Um usuário do SongLog. Guarda suas avaliações e seu diário de audição.
 * A senha nunca é mantida em texto puro: recebe-se apenas o hash (ver SenhaUtil).
 */
public class Usuario {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private int id;
    private String nome;
    private String email;
    private String senhaHash;
    private final LocalDate dataCadastro;
    private final List<Avaliacao> avaliacoes = new ArrayList<>();
    private final List<EntradaDiario> diario = new ArrayList<>();

    public Usuario(String nome, String email, String senhaHash) {
        this(0, nome, email, senhaHash, LocalDate.now());
    }

    public Usuario(int id, String nome, String email, String senhaHash, LocalDate dataCadastro) {
        this.id = id;
        this.nome = Validacao.textoObrigatorio(nome, "Nome", 80);
        this.email = validarEmail(email);
        this.senhaHash = Validacao.textoObrigatorio(senhaHash, "Senha", 255);
        this.dataCadastro = Objects.requireNonNull(dataCadastro, "dataCadastro");
    }

    /**
     * Avalia uma faixa. Se o usuário já a avaliou, a avaliação existente é atualizada.
     */
    public Avaliacao avaliar(Faixa faixa, double nota, String comentario) {
        Objects.requireNonNull(faixa, "faixa");
        Optional<Avaliacao> existente = getAvaliacaoDe(faixa);
        if (existente.isPresent()) {
            existente.get().atualizar(nota, comentario);
            return existente.get();
        }
        Avaliacao nova = new Avaliacao(this, faixa, nota, comentario);
        avaliacoes.add(nova);
        return nova;
    }

    public Optional<Avaliacao> getAvaliacaoDe(Faixa faixa) {
        return avaliacoes.stream().filter(a -> a.getFaixa().equals(faixa)).findFirst();
    }

    /**
     * Registra uma audição no diário. É marcada como reaudição se o álbum já consta no diário.
     */
    public EntradaDiario registrarAudicao(Album album, LocalDate data, String comentario) {
        Objects.requireNonNull(album, "album");
        boolean reaudicao = diario.stream().anyMatch(e -> e.getAlbum().equals(album));
        EntradaDiario entrada = new EntradaDiario(this, album, data, comentario, reaudicao);
        diario.add(entrada);
        return entrada;
    }

    /** "Limpar" na tela de avaliação: remove a nota da faixa, se existir. */
    public boolean removerAvaliacao(Faixa faixa) {
        return avaliacoes.removeIf(a -> a.getFaixa().equals(faixa));
    }

    /** RN13: faixas excluídas levam junto as avaliações. */
    public void removerAvaliacoesDasFaixas(java.util.Collection<Faixa> faixas) {
        avaliacoes.removeIf(a -> faixas.contains(a.getFaixa()));
    }

    /** RN13: álbum excluído leva junto as entradas de diário. */
    public void removerEntradasDoAlbum(Album album) {
        diario.removeIf(e -> e.getAlbum().equals(album));
    }

    public boolean removerEntrada(EntradaDiario entrada) {
        return diario.remove(entrada);
    }

    /** Avaliações mais recentes primeiro. */
    public List<Avaliacao> getUltimasAvaliacoes(int limite) {
        return avaliacoes.stream()
                .sorted(Comparator.comparing(Avaliacao::getDataAvaliacao).reversed())
                .limit(limite)
                .toList();
    }

    public List<Avaliacao> getAvaliacoes() {
        return Collections.unmodifiableList(avaliacoes);
    }

    /** Diário do mais recente para o mais antigo. */
    public List<EntradaDiario> getDiario() {
        List<EntradaDiario> ordenado = new ArrayList<>(diario);
        ordenado.sort(Comparator.comparing(EntradaDiario::getDataAudicao).reversed());
        return Collections.unmodifiableList(ordenado);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = Validacao.textoObrigatorio(nome, "Nome", 80);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = validarEmail(email);
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = Validacao.textoObrigatorio(senhaHash, "Senha", 255);
    }

    public LocalDate getDataCadastro() {
        return dataCadastro;
    }

    private static String validarEmail(String email) {
        String limpo = Validacao.textoObrigatorio(email, "E-mail", 120).toLowerCase();
        if (!EMAIL.matcher(limpo).matches()) {
            throw new IllegalArgumentException("E-mail inválido.");
        }
        return limpo;
    }

    @Override
    public String toString() {
        return nome + " <" + email + ">";
    }
}
