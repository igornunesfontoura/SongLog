package br.com.songlog.repositorio;

import br.com.songlog.model.Album;
import br.com.songlog.model.Faixa;
import br.com.songlog.model.Usuario;
import br.com.songlog.service.Contexto;
import br.com.songlog.util.Tempo;
import java.time.LocalDate;
import java.util.List;

/**
 * Dados de demonstração carregados na memória. "Kind of Blue" reproduz o exemplo real do
 * projeto; os demais álbuns são fictícios (nomes, artistas e faixas inventados).
 * Todos os usuários de demonstração têm a senha {@value #SENHA_DEMO}.
 */
public final class DadosIniciais {

    public static final String SENHA_DEMO = "songlog123";
    // PBKDF2 de SENHA_DEMO, pré-calculado para o programa abrir sem esperar o hash.
    private static final String HASH_DEMO =
            "pbkdf2$600000$3IFSlAfoBeqeULf5jk1+qQ==$T016NcM1x9PRAM4t0DsVOLfC0EnPpRBxCbOmWLkyOp8=";

    private DadosIniciais() {
    }

    public static void carregar(Contexto ctx) {
        Usuario ana = ctx.usuarios.salvar(new Usuario(0, "Ana Ribeiro", "ana@songlog.dev", HASH_DEMO, LocalDate.of(2026, 3, 12)));
        Usuario bruno = ctx.usuarios.salvar(new Usuario(0, "Bruno Tavares", "bruno@songlog.dev", HASH_DEMO, LocalDate.of(2026, 4, 2)));
        Usuario carla = ctx.usuarios.salvar(new Usuario(0, "Carla Mendes", "carla@songlog.dev", HASH_DEMO, LocalDate.of(2026, 5, 20)));

        Album kob = album(ctx, "Kind of Blue", "Miles Davis", 1959, "Jazz",
                "So What|9:22", "Freddie Freeloader|9:46", "Blue in Green|5:37", "All Blues|11:33", "Flamenco Sketches|9:26");
        Album rio = album(ctx, "Rio Sem Margem", "Marina Costa", 2019, "MPB",
                "Cais de Areia|4:12", "Fio d'Água|3:48", "Maré Vazante|5:03", "Carta Sem Endereço|3:31", "Rio Sem Margem|6:20", "Ponte de Papel|4:05");
        Album lisboa = album(ctx, "Estação Lisboa", "Duo Atlântico", 2015, "Jazz",
                "Partida às Seis|5:44", "Elétrico 28|4:36", "Miradouro|6:12", "Fado Curto|3:20", "Noite no Cais|7:05");
        Album concreto = album(ctx, "Concreto e Flor", "Os Vendavais", 2022, "Rock",
                "Rachadura|3:22", "Flor de Asfalto|4:01", "Voltagem|3:47", "Ruído Branco|2:58", "Cidade Dormindo|5:19", "Concreto|4:33", "Flor|3:12");
        album(ctx, "Sal e Ferro", "Tomás Aragão", 2011, "Folk",
                "Âncora|4:20", "Sal na Pele|3:55", "Ferro Velho|4:48", "O Farol|5:30", "Lenha Úmida|3:26", "Estrada de Volta|4:59");
        Album vidro = album(ctx, "Luz de Vidro", "Helena Prado", 2020, "Pop",
                "Vidro|3:14", "Prisma|3:38", "Refletir|3:02", "Luz Baixa|4:10", "Estática|2:51", "Ao Contrário|3:44");
        album(ctx, "Madrugada Elétrica", "Coletivo Norte", 2008, "Eletrônica",
                "Abertura Fria|6:45", "Fase 2|5:20", "Circuito|7:10", "Sol Artificial|6:02", "Desligar|8:15");
        album(ctx, "Cartas ao Vento", "Inês Falcão", 2017, "Samba",
                "Recado|3:40", "Tamborim de Papel|3:12", "Roda de Domingo|4:25", "Saudade Elétrica|3:58", "Cartas ao Vento|5:10", "Terreiro de Luz|4:02");

        // "Kind of Blue": Ana e Bruno completos (nota geral 4,34), Carla parcial.
        notas(ana, kob, 4.5, 4.0, 5.0, 4.5, 5.0);
        notas(bruno, kob, 4.0, 3.5, 5.0, 4.0, 4.5);
        notas(carla, kob, 5.0, 0, 4.5, 0, 0);
        notas(bruno, rio, 4.0, 4.5, 3.5, 4.0, 5.0, 4.0);
        notas(carla, rio, 4.5, 4.0, 4.0, 3.5, 5.0, 4.5);
        notas(ana, rio, 4.0, 0, 0, 0, 5.0, 0);
        notas(ana, lisboa, 4.5, 4.0, 5.0, 3.5, 4.5);
        notas(bruno, lisboa, 4.0, 4.0, 4.5, 3.0, 4.0);
        notas(bruno, concreto, 3.5, 4.0, 3.0, 3.0, 4.0, 3.5, 3.0);
        notas(carla, vidro, 3.0, 3.5, 3.0, 4.0, 3.0, 3.5);

        LocalDate hoje = LocalDate.now();
        ana.registrarAudicao(rio, hoje.minusDays(45), "Recomendação da Carla.");
        ana.registrarAudicao(kob, hoje.minusDays(30), "Primeira vez, de ponta a ponta.");
        ana.registrarAudicao(lisboa, hoje.minusDays(9), "O disco certo para a viagem de volta.");
        ana.registrarAudicao(kob, hoje.minusDays(2), "Voltei só por Blue in Green.");
        bruno.registrarAudicao(kob, hoje.minusDays(12), "Clássico que eu ainda não tinha ouvido inteiro.");
        carla.registrarAudicao(vidro, hoje.minusDays(5), null);
    }

    private static Album album(Contexto ctx, String titulo, String artista, int ano, String genero, String... faixas) {
        Album a = new Album(titulo, artista, ano, genero);
        for (String f : faixas) {
            String[] p = f.split("\\|");
            a.adicionarFaixa(p[0], Tempo.interpretar(p[1]));
        }
        return ctx.albuns.salvar(a);
    }

    /** Uma nota por faixa; 0 = sem nota. */
    private static void notas(Usuario u, Album a, double... notas) {
        List<Faixa> faixas = a.getFaixas();
        for (int i = 0; i < notas.length; i++) {
            if (notas[i] > 0) {
                u.avaliar(faixas.get(i), notas[i], null);
            }
        }
    }
}
