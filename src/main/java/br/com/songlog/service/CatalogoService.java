package br.com.songlog.service;

import br.com.songlog.model.Album;
import br.com.songlog.model.Faixa;
import br.com.songlog.model.Usuario;
import br.com.songlog.repositorio.AlbumRepositorio;
import br.com.songlog.repositorio.UsuarioRepositorio;
import br.com.songlog.util.Formatos;
import br.com.songlog.util.Tempo;
import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Regras do catálogo: busca, validação do cadastro (RN01, RN02, RN12), salvamento e
 * exclusão em cascata (RN13). As telas só trabalham com {@link RascunhoAlbum}.
 */
public class CatalogoService {

    /** Dados digitados no formulário, ainda não validados. */
    public static final class RascunhoAlbum {
        public String titulo = "";
        public String artista = "";
        public String ano = "";
        public String genero = "";
        public final List<RascunhoFaixa> faixas = new ArrayList<>();
    }

    public static final class RascunhoFaixa {
        /** Faixa já existente no álbum, ou {@code null} se for nova. */
        public Faixa existente;
        public String titulo = "";
        public String duracao = "";
        public boolean removida;
    }

    private final AlbumRepositorio albuns;
    private final UsuarioRepositorio usuarios;

    public CatalogoService(AlbumRepositorio albuns, UsuarioRepositorio usuarios) {
        this.albuns = albuns;
        this.usuarios = usuarios;
    }

    public List<Album> todos() {
        return albuns.todos();
    }

    /** Busca por título, artista ou gênero (sem acentos); {@code genero} vazio ou nulo = todos. */
    public List<Album> buscar(String texto, String genero) {
        String q = Formatos.semAcentos(texto == null ? "" : texto.strip());
        List<Album> saida = new ArrayList<>();
        for (Album a : albuns.todos()) {
            boolean generoOk = genero == null || genero.isEmpty()
                    || (a.getGenero() != null && a.getGenero().equalsIgnoreCase(genero));
            boolean textoOk = q.isEmpty()
                    || Formatos.semAcentos(a.getTitulo()).contains(q)
                    || Formatos.semAcentos(a.getArtista()).contains(q)
                    || Formatos.semAcentos(a.getGenero() == null ? "" : a.getGenero()).contains(q);
            if (generoOk && textoOk) {
                saida.add(a);
            }
        }
        return saida;
    }

    public List<String> generos() {
        TreeSet<String> g = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Album a : albuns.todos()) {
            if (a.getGenero() != null) {
                g.add(a.getGenero());
            }
        }
        return new ArrayList<>(g);
    }

    /** Monta o rascunho de um álbum existente (para edição). */
    public RascunhoAlbum rascunhoDe(Album album) {
        RascunhoAlbum r = new RascunhoAlbum();
        r.titulo = album.getTitulo();
        r.artista = album.getArtista();
        r.ano = String.valueOf(album.getAnoLancamento());
        r.genero = album.getGenero() == null ? "" : album.getGenero();
        for (Faixa f : album.getFaixas()) {
            RascunhoFaixa rf = new RascunhoFaixa();
            rf.existente = f;
            rf.titulo = f.getTitulo();
            rf.duracao = f.getDuracaoFormatada();
            r.faixas.add(rf);
        }
        return r;
    }

    /**
     * Valida o rascunho. Chaves: titulo, artista, ano, genero, faixas, album,
     * faixa.N.titulo e faixa.N.duracao (N = posição na lista, contando as removidas).
     */
    public Map<String, String> validar(RascunhoAlbum r, Album existente) {
        Map<String, String> erros = new LinkedHashMap<>();
        if (r.titulo.isBlank()) {
            erros.put("titulo", "informe o título do álbum.");
        } else if (r.titulo.strip().length() > 120) {
            erros.put("titulo", "o título passa de 120 caracteres.");
        }
        if (r.artista.isBlank()) {
            erros.put("artista", "informe o artista.");
        } else if (r.artista.strip().length() > 120) {
            erros.put("artista", "o nome do artista passa de 120 caracteres.");
        }
        int maximo = Year.now().getValue() + 1;
        int ano = 0;
        try {
            ano = Integer.parseInt(r.ano.strip());
            if (ano < 1877 || ano > maximo) {
                erros.put("ano", "informe um ano entre 1877 e " + maximo + ".");
            }
        } catch (NumberFormatException e) {
            erros.put("ano", "informe o ano com 4 dígitos.");
        }
        if (r.genero.strip().length() > 40) {
            erros.put("genero", "o gênero passa de 40 caracteres.");
        }
        int ativas = 0;
        for (int i = 0; i < r.faixas.size(); i++) {
            RascunhoFaixa f = r.faixas.get(i);
            if (f.removida) {
                continue;
            }
            ativas++;
            if (f.titulo.isBlank()) {
                erros.put("faixa." + i + ".titulo", "informe o título da faixa.");
            } else if (f.titulo.strip().length() > 120) {
                erros.put("faixa." + i + ".titulo", "o título da faixa passa de 120 caracteres.");
            }
            try {
                Tempo.interpretar(f.duracao);
            } catch (IllegalArgumentException e) {
                erros.put("faixa." + i + ".duracao", e.getMessage());
            }
        }
        if (ativas == 0) {
            erros.put("faixas", "adicione ao menos uma faixa (RN01).");
        }
        if (!erros.containsKey("titulo") && !erros.containsKey("artista") && !erros.containsKey("ano")
                && albuns.existeDuplicado(r.titulo, r.artista, ano, existente)) {
            erros.put("album", "já existe um álbum com este título, artista e ano.");
        }
        return erros;
    }

    /** Aplica o rascunho ao álbum (ou cria um novo) e salva. */
    public Album salvar(RascunhoAlbum r, Album existente) {
        Map<String, String> erros = validar(r, existente);
        if (!erros.isEmpty()) {
            Map.Entry<String, String> primeiro = erros.entrySet().iterator().next();
            throw new ValidacaoException(primeiro.getKey(), primeiro.getValue());
        }
        int ano = Integer.parseInt(r.ano.strip());
        String genero = r.genero.isBlank() ? null : r.genero.strip();
        Album album;
        if (existente == null) {
            album = new Album(r.titulo, r.artista, ano, genero);
        } else {
            album = existente;
            album.setTitulo(r.titulo);
            album.setArtista(r.artista);
            album.setAnoLancamento(ano);
            album.setGenero(genero);
        }
        List<Faixa> removidas = new ArrayList<>();
        for (RascunhoFaixa f : r.faixas) {
            if (f.removida && f.existente != null) {
                album.removerFaixa(f.existente);
                removidas.add(f.existente);
            }
        }
        for (RascunhoFaixa f : r.faixas) {
            if (f.removida) {
                continue;
            }
            int segundos = Tempo.interpretar(f.duracao);
            if (f.existente != null) {
                f.existente.setTitulo(f.titulo);
                f.existente.setDuracaoSegundos(segundos);
            } else {
                album.adicionarFaixa(f.titulo, segundos);
            }
        }
        if (!removidas.isEmpty()) {
            for (Usuario u : usuarios.todos()) {
                u.removerAvaliacoesDasFaixas(removidas);
            }
        }
        return albuns.salvar(album);
    }

    /** RN13: leva junto as faixas, as avaliações dessas faixas e as entradas de diário do álbum. */
    public void excluir(Album album) {
        for (Usuario u : usuarios.todos()) {
            u.removerAvaliacoesDasFaixas(album.getFaixas());
            u.removerEntradasDoAlbum(album);
        }
        albuns.excluir(album);
    }

    public int quantidadeAvaliacoes(Album album) {
        int total = 0;
        for (Usuario u : usuarios.todos()) {
            for (Faixa f : album.getFaixas()) {
                if (u.getAvaliacaoDe(f).isPresent()) {
                    total++;
                }
            }
        }
        return total;
    }

    public int quantidadeEntradas(Album album) {
        int total = 0;
        for (Usuario u : usuarios.todos()) {
            total += (int) u.getDiario().stream().filter(e -> e.getAlbum().equals(album)).count();
        }
        return total;
    }
}
