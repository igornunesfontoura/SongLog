package br.com.songlog;

import br.com.songlog.model.Album;
import br.com.songlog.model.EntradaDiario;
import br.com.songlog.model.Faixa;
import br.com.songlog.model.Usuario;
import br.com.songlog.service.NotaService;
import br.com.songlog.service.NotaService.NotaAlbum;
import br.com.songlog.service.NotaService.NotaGeral;
import br.com.songlog.util.SenhaUtil;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Protótipo de console da Etapa 1 (a aplicação gráfica está em {@link Main}): exercita o modelo de domínio em memória, sem banco e sem interface.
 * Nas etapas seguintes os dados passam a vir do MySQL e a interação, das telas.
 */
public class PrototipoConsole {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final String LINHA = "-".repeat(64);

    // Simula as chaves que o MySQL (AUTO_INCREMENT) atribuirá ao salvar.
    private static int proximoId = 1;

    public static void main(String[] args) {
        // 1. Catálogo: um álbum com suas faixas (durações em segundos)
        Album album = new Album("Kind of Blue", "Miles Davis", 1959, "Jazz");
        album.adicionarFaixa("So What", 562);
        album.adicionarFaixa("Freddie Freeloader", 586);
        album.adicionarFaixa("Blue in Green", 337);
        album.adicionarFaixa("All Blues", 693);
        album.adicionarFaixa("Flamenco Sketches", 566);
        simularPersistencia(album);

        // 2. Usuários (a senha vai direto para o hash)
        Usuario ana = new Usuario("Ana Ribeiro", "ana@songlog.dev", SenhaUtil.gerarHash("senha-da-ana"));
        Usuario bruno = new Usuario("Bruno Tavares", "bruno@songlog.dev", SenhaUtil.gerarHash("senha-do-bruno"));
        Usuario carla = new Usuario("Carla Mendes", "carla@songlog.dev", SenhaUtil.gerarHash("senha-da-carla"));
        ana.setId(proximoId++);
        bruno.setId(proximoId++);
        carla.setId(proximoId++);

        // 3. Avaliação faixa a faixa (Ana e Bruno completas; Carla parcial)
        List<Faixa> faixas = album.getFaixas();
        double[] notasAna = {4.5, 4.0, 5.0, 4.5, 5.0};
        double[] notasBruno = {4.0, 3.5, 5.0, 4.0, 4.5};
        for (int i = 0; i < faixas.size(); i++) {
            ana.avaliar(faixas.get(i), notasAna[i], null);
            bruno.avaliar(faixas.get(i), notasBruno[i], null);
        }
        carla.avaliar(faixas.get(0), 5.0, "Perfeita.");
        carla.avaliar(faixas.get(2), 4.5, null);

        // 4. Diário de audição
        ana.registrarAudicao(album, LocalDate.now().minusDays(30), "Primeira vez, de ponta a ponta.");
        ana.registrarAudicao(album, LocalDate.now().minusDays(2), "Voltei só por Blue in Green.");

        // --- Relatório -----------------------------------------------------------
        List<Usuario> usuarios = List.of(ana, bruno, carla);

        System.out.println("SONGLOG — protótipo da Etapa 1");
        System.out.println(LINHA);
        System.out.println(album.getArtista().toUpperCase(PT_BR) + " / " + album.getTitulo());
        System.out.println(album.getAnoLancamento() + " · " + album.getGenero() + " · "
                + album.getTotalFaixas() + " faixas · " + album.getDuracaoTotalFormatada());
        System.out.println(LINHA);

        System.out.printf(PT_BR, "%-4s%-24s%7s%6s%6s%6s%n", "#", "FAIXA", "DUR", "ANA", "BRU", "CAR");
        for (Faixa faixa : faixas) {
            System.out.printf(PT_BR, "%-4d%-24s%7s%6s%6s%6s%n",
                    faixa.getNumero(), faixa.getTitulo(), faixa.getDuracaoFormatada(),
                    notaDaFaixa(ana, faixa), notaDaFaixa(bruno, faixa), notaDaFaixa(carla, faixa));
        }
        System.out.println(LINHA);

        for (Usuario u : usuarios) {
            Optional<NotaAlbum> nota = NotaService.notaDoUsuario(album, u);
            String texto = nota
                    .map(n -> String.format(PT_BR, "%.2f  (%d/%d faixas%s)", n.valor(), n.faixasAvaliadas(),
                            n.totalFaixas(), n.completa() ? "" : ", parcial"))
                    .orElse("sem avaliação");
            System.out.printf(PT_BR, "Nota de %-14s %s%n", u.getNome().split(" ")[0], texto);
        }

        System.out.println(LINHA);
        String geral = NotaService.notaGeral(album, usuarios)
                .map(g -> String.format(PT_BR, "%.2f  (%d avaliadores completos)", g.valor(), g.totalAvaliadores()))
                .orElse("sem avaliações completas");
        System.out.println("NOTA GERAL DO ÁLBUM   " + geral);
        System.out.println(LINHA);

        System.out.println("Diário de " + ana.getNome());
        for (EntradaDiario e : ana.getDiario()) {
            System.out.printf(PT_BR, "  %s  %s%s  %s%n", e.getDataAudicao(), e.getAlbum().getTitulo(),
                    e.isReaudicao() ? " (reaudição)" : "", e.getComentario());
        }
        System.out.println(LINHA);

        // 5. Regras de negócio protegendo o modelo
        tentar("Nota 4,3 (fora do passo de 0,5)", () -> bruno.avaliar(faixas.get(0), 4.3, null));
        tentar("Nota 6,0 (acima do máximo)", () -> bruno.avaliar(faixas.get(0), 6.0, null));
        tentar("Faixa de 0 segundos", () -> album.adicionarFaixa("Silêncio", 0));
        tentar("E-mail inválido", () -> new Usuario("Dora", "dora-sem-arroba", "x"));

        System.out.println(LINHA);
        System.out.println("Senha de Ana confere? " + SenhaUtil.verificar("senha-da-ana", ana.getSenhaHash()));
        System.out.println("Senha errada confere? " + SenhaUtil.verificar("outra", ana.getSenhaHash()));
    }

    private static void simularPersistencia(Album album) {
        album.setId(proximoId++);
        for (Faixa faixa : album.getFaixas()) {
            faixa.setId(proximoId++);
        }
    }

    private static String notaDaFaixa(Usuario usuario, Faixa faixa) {
        return usuario.getAvaliacaoDe(faixa)
                .map(a -> String.format(PT_BR, "%.1f", a.getNota()))
                .orElse("—");
    }

    private static void tentar(String descricao, Runnable acao) {
        try {
            acao.run();
            System.out.println("[FALHOU] aceitou: " + descricao);
        } catch (IllegalArgumentException e) {
            System.out.println("[recusado] " + descricao + " -> " + e.getMessage());
        }
    }
}
