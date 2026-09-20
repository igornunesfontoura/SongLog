package br.com.songlog;

import br.com.songlog.model.Album;
import br.com.songlog.model.Avaliacao;
import br.com.songlog.model.EntradaDiario;
import br.com.songlog.model.Faixa;
import br.com.songlog.model.Usuario;
import br.com.songlog.service.Contexto;
import br.com.songlog.service.NotaService;
import br.com.songlog.service.ValidacaoException;
import br.com.songlog.util.Formatos;
import br.com.songlog.view.Tema;
import br.com.songlog.view.TelaAlbum;
import br.com.songlog.view.TelaAvaliacao;
import br.com.songlog.view.TelaCatalogo;
import br.com.songlog.view.TelaLogin;
import br.com.songlog.view.TelaPerfilDiario;
import br.com.songlog.view.componentes.BarraDeNota;
import br.com.songlog.view.componentes.Campo;
import br.com.songlog.view.componentes.CampoSenha;
import br.com.songlog.view.componentes.MensagemErro;
import br.com.songlog.view.componentes.Seletor;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.AbstractButton;
import javax.swing.JPanel;
import javax.swing.JTable;

/**
 * Teste de interação sem monitor: monta as telas reais, dispara cliques e teclas e confere o
 * resultado no modelo. Uso: java -Djava.awt.headless=true -cp target/classes:target/test-classes
 * br.com.songlog.TesteDeInteracao (sai com código 1 se algo falhar).
 */
public class TesteDeInteracao {

    static int falhas;
    static int total;
    static Contexto ctx;
    static NavegadorDeTeste nav;

    static void ok(String descricao, boolean condicao) {
        total++;
        if (!condicao) {
            falhas++;
        }
        System.out.println((condicao ? "  [ok]      " : "  [FALHOU]  ") + descricao);
    }

    static <T> T naInterface(Supplier<T> s) {
        Object[] r = new Object[1];
        Achar.naInterface(() -> r[0] = s.get());
        @SuppressWarnings("unchecked")
        T t = (T) r[0];
        return t;
    }

    static Campo campo(Component tela, String nome, int indice) {
        return Achar.todos(tela, Campo.class).stream().filter(c -> nome.equals(c.getAccessibleContext().getAccessibleName())).skip(indice).findFirst()
                .orElseThrow(() -> new AssertionError("campo não encontrado: " + nome));
    }

    static void clicar(Component tela, String texto) {
        AbstractButton b = Achar.botao(tela, texto);
        Achar.naInterface(b::doClick);
    }

    static long errosVisiveis(Component tela) {
        return Achar.todos(tela, MensagemErro.class).stream().filter(Component::isVisible).count();
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        ctx = Contexto.emMemoria();
        nav = new NavegadorDeTeste(ctx);
        Tema.definir(Tema.CLARO);
        Usuario ana = ctx.usuarios.porEmail("ana@songlog.dev").orElseThrow();
        Album kob = ctx.albuns.todos().get(0);

        System.out.println("1. Autenticação (serviço)");
        ok("entra com a senha correta", ctx.autenticacao.entrar("ana@songlog.dev", "songlog123").getNome().equals("Ana Ribeiro"));
        ok("recusa senha errada sem revelar qual campo", mensagem(() -> ctx.autenticacao.entrar("ana@songlog.dev", "errada")).contains("incorretos"));
        ok("recusa e-mail inexistente com a mesma mensagem", mensagem(() -> ctx.autenticacao.entrar("ninguem@x.com", "songlog123")).contains("incorretos"));
        ok("cadastro exige senha de 8+ caracteres", mensagem(() -> ctx.autenticacao.cadastrar("Diego", "diego@x.com", "123", "123")).contains("8"));
        ok("cadastro confere a confirmação", mensagem(() -> ctx.autenticacao.cadastrar("Diego", "diego@x.com", "senha-forte", "outra")).contains("conferem"));
        ok("cadastro recusa e-mail repetido (RN11)", mensagem(() -> ctx.autenticacao.cadastrar("Ana2", "ana@songlog.dev", "senha-forte", "senha-forte")).contains("já está cadastrado"));
        Usuario diego = ctx.autenticacao.cadastrar("Diego Lima", "diego@x.com", "senha-forte", "senha-forte");
        ok("cadastro guarda só o hash da senha (RN11)", diego.getSenhaHash().startsWith("pbkdf2$") && !diego.getSenhaHash().contains("senha-forte"));

        System.out.println("2. Tela de login");
        TelaLogin login = naInterface(() -> new TelaLogin(nav));
        Achar.naInterface(() -> {
            campo(login, "E-mail", 0).setText("ana@songlog.dev");
            Achar.todos(login, CampoSenha.class).get(0).setText("errada");
        });
        clicar(login, "Entrar");
        Achar.esperar(() -> errosVisiveis(login) > 0, 6000);
        ok("senha errada mostra o erro na própria tela", errosVisiveis(login) > 0 && !nav.destinos.contains("catalogo"));
        Achar.naInterface(() -> Achar.todos(login, CampoSenha.class).get(0).setText("songlog123"));
        clicar(login, "Entrar");
        Achar.esperar(() -> nav.destinos.contains("catalogo"), 8000);
        ok("senha correta leva ao catálogo com o usuário na sessão", nav.destinos.contains("catalogo") && ctx.usuario() != null && ctx.usuario().equals(ana));
        clicar(login, "Criar conta");
        ok("\"Criar conta\" troca o formulário para o modo de cadastro", Achar.temTexto(login, "Confirmar senha"));

        System.out.println("3. Catálogo");
        TelaCatalogo cat = naInterface(() -> new TelaCatalogo(nav));
        JTable tabela = Achar.todos(cat, JTable.class).get(0);
        ok("lista todos os álbuns do catálogo", tabela.getRowCount() == ctx.catalogo.todos().size());
        Achar.naInterface(() -> campo(cat, "Buscar por título, artista ou gênero", 0).setText("KIND"));
        Achar.esperar(() -> tabela.getRowCount() == 1, 2000);
        ok("busca sem diferenciar maiúsculas e sem acentos filtra enquanto digita", tabela.getRowCount() == 1);
        Achar.naInterface(() -> {
            tabela.setRowSelectionInterval(0, 0);
            tabela.getActionMap().get("abrir").actionPerformed(new ActionEvent(tabela, 0, "abrir"));
        });
        ok("Enter na linha abre a avaliação do álbum", nav.destinos.contains("avaliacao") && nav.ultimoAlbum == kob);
        Achar.naInterface(() -> tabela.getActionMap().get("editar").actionPerformed(new ActionEvent(tabela, 0, "editar")));
        ok("F2 abre o formulário de edição", nav.destinos.contains("album:editar"));
        Achar.naInterface(() -> campo(cat, "Buscar por título, artista ou gênero", 0).setText("zzzz"));
        Achar.esperar(() -> tabela.getRowCount() == 0, 2000);
        ok("busca sem resultado mostra o estado vazio", tabela.getRowCount() == 0 && Achar.temTexto(cat, "Nenhum álbum encontrado"));
        Achar.naInterface(() -> campo(cat, "Buscar por título, artista ou gênero", 0).setText(""));
        Achar.naInterface(() -> Achar.todos(cat, Seletor.class).stream().filter(s -> "Filtrar por gênero".equals(s.getAccessibleContext().getAccessibleName()))
                .findFirst().orElseThrow().setSelectedItem("Jazz"));
        Achar.esperar(() -> tabela.getRowCount() == 2, 2000);
        ok("filtro de gênero mostra só os álbuns de Jazz", tabela.getRowCount() == 2);

        System.out.println("4. Avaliação faixa a faixa");
        ctx.entrar(ana);
        TelaAvaliacao av = naInterface(() -> new TelaAvaliacao(nav, kob));
        List<BarraDeNota> barras = Achar.todos(av, BarraDeNota.class);
        List<Faixa> faixas = kob.getFaixas();
        ok("uma barra de nota por faixa", barras.size() == 5);
        ok("mostra a nota ponderada 4,56 e a geral 4,34 do exemplo do projeto", Achar.temTexto(av, "4,56") && Achar.temTexto(av, "4,34"));
        Achar.pressionar(barras.get(0), KeyEvent.VK_LEFT);
        ok("seta ← baixa a nota em 0,5 e salva na hora", ana.getAvaliacaoDe(faixas.get(0)).orElseThrow().getNota() == 4.0);
        String esperada = Formatos.nota2(NotaService.notaDoUsuario(kob, ana).orElseThrow().valor());
        ok("\"Sua nota\" recalcula ao vivo (" + esperada + ")", Achar.temTexto(av, esperada));
        for (char c : "3,5".toCharArray()) {
            Achar.digitar(barras.get(1), c);
        }
        ok("digitar 3,5 define a nota da faixa", ana.getAvaliacaoDe(faixas.get(1)).orElseThrow().getNota() == 3.5);
        Achar.pressionar(barras.get(2), KeyEvent.VK_DELETE);
        ok("Delete limpa a nota e o álbum vira parcial (4 de 5)", ana.getAvaliacaoDe(faixas.get(2)).isEmpty() && Achar.temTexto(av, "PARCIAL"));
        ok("avaliação parcial sai da nota geral (RN07)", NotaService.notaGeral(kob, ctx.usuarios.todos()).orElseThrow().totalAvaliadores() == 1);
        Achar.digitar(barras.get(2), '5');
        ok("digitar 5 devolve a nota e o álbum volta a ser completo", ana.getAvaliacaoDe(faixas.get(2)).isPresent() && !Achar.temTexto(av, "PARCIAL"));
        Achar.pressionar(barras.get(4), KeyEvent.VK_RIGHT);
        Achar.pressionar(barras.get(4), KeyEvent.VK_RIGHT);
        ok("a nota não passa de 5,0", ana.getAvaliacaoDe(faixas.get(4)).orElseThrow().getNota() == 5.0);
        clicar(av, "Como é calculada? +");
        ok("\"Como é calculada?\" mostra a conta com os números do álbum", Achar.temTexto(av, "Ocultar cálculo") && Achar.temTexto(av, "÷"));
        Achar.naInterface(() -> Achar.todos(av, AbstractButton.class).stream().filter(b -> "Comentar".equals(b.getText())).findFirst().orElseThrow().doClick());
        Achar.naInterface(() -> {
            Campo c = campo(av, "Comentário da faixa", 0);
            c.setText("Abertura perfeita.");
            c.postActionEvent();
        });
        ok("comentário da faixa é salvo com a nota", "Abertura perfeita.".equals(ana.getAvaliacaoDe(faixas.get(0)).orElseThrow().getComentario()));
        try {
            Avaliacao.validarNota(4.3);
            ok("nota fora do passo de 0,5 é recusada", false);
        } catch (IllegalArgumentException e) {
            ok("nota fora do passo de 0,5 é recusada (RN03)", true);
        }

        System.out.println("5. Cadastro e edição de álbum");
        int antes = ctx.catalogo.todos().size();
        TelaAlbum vazio = naInterface(() -> new TelaAlbum(nav, null));
        clicar(vazio, "Salvar álbum");
        ok("salvar com o formulário vazio mostra os erros e não cria nada", errosVisiveis(vazio) >= 3 && ctx.catalogo.todos().size() == antes);
        TelaAlbum novo = naInterface(() -> new TelaAlbum(nav, null));
        Achar.naInterface(() -> {
            campo(novo, "Título do álbum", 0).setText("Álbum de Teste");
            campo(novo, "Artista", 0).setText("Banda Teste");
            campo(novo, "Ano de lançamento", 0).setText("2024");
            campo(novo, "Título da faixa", 0).setText("Primeira");
            campo(novo, "Duração da faixa em minutos e segundos", 0).setText("3:30");
        });
        Achar.naInterface(() -> campo(novo, "Duração da faixa em minutos e segundos", 0).postActionEvent()); // Enter na última duração adiciona faixa
        Achar.naInterface(() -> {
            campo(novo, "Título da faixa", 1).setText("Segunda");
            campo(novo, "Duração da faixa em minutos e segundos", 1).setText("9:75");
        });
        clicar(novo, "Salvar álbum");
        ok("duração 9:75 é recusada com mensagem específica (segundos 00 a 59)", Achar.temTexto(novo, "segundos de 00 a 59") && ctx.catalogo.todos().size() == antes);
        Achar.naInterface(() -> campo(novo, "Duração da faixa em minutos e segundos", 1).setText("4:05"));
        clicar(novo, "Salvar álbum");
        Album criado = ctx.catalogo.todos().stream().filter(a -> a.getTitulo().equals("Álbum de Teste")).findFirst().orElse(null);
        ok("álbum válido é salvo, ganha id e volta ao catálogo", criado != null && criado.getId() > 0 && nav.destinos.get(nav.destinos.size() - 1).equals("catalogo"));
        ok("as faixas foram numeradas em sequência (RN01)", criado != null && criado.getTotalFaixas() == 2 && criado.getFaixas().get(1).getNumero() == 2);
        TelaAlbum repetido = naInterface(() -> new TelaAlbum(nav, null));
        Achar.naInterface(() -> {
            campo(repetido, "Título do álbum", 0).setText("álbum de teste");
            campo(repetido, "Artista", 0).setText("BANDA TESTE");
            campo(repetido, "Ano de lançamento", 0).setText("2024");
            campo(repetido, "Título da faixa", 0).setText("X");
            campo(repetido, "Duração da faixa em minutos e segundos", 0).setText("1:00");
        });
        clicar(repetido, "Salvar álbum");
        ok("título + artista + ano repetidos são recusados, mesmo com outra caixa (RN02)", Achar.temTexto(repetido, "já existe um álbum") && ctx.catalogo.todos().size() == antes + 1);

        Faixa f1 = criado.getFaixas().get(0);
        Faixa f2 = criado.getFaixas().get(1);
        ana.avaliar(f1, 4.0, null);
        ana.avaliar(f2, 3.0, null);
        ana.registrarAudicao(criado, LocalDate.now().minusDays(1), "Teste");
        TelaAlbum edicao = naInterface(() -> new TelaAlbum(nav, criado));
        clicar(edicao, "Remover");
        Achar.naInterface(() -> { });
        ok("remover faixa só vale ao salvar (nada muda antes)", criado.getTotalFaixas() == 2 && ana.getAvaliacaoDe(f1).isPresent());
        clicar(edicao, "Salvar álbum");
        ok("ao salvar, a faixa removida sai e o álbum renumera", criado.getTotalFaixas() == 1 && criado.getFaixas().get(0).getNumero() == 1
                && criado.getFaixas().get(0).getTitulo().equals("Segunda"));
        ok("a avaliação da faixa removida é excluída junto (RN13)", ana.getAvaliacaoDe(f1).isEmpty() && ana.getAvaliacaoDe(f2).isPresent());
        ctx.catalogo.excluir(criado);
        ok("excluir o álbum leva faixas, notas e diário (RN13)", ctx.catalogo.todos().size() == antes
                && ana.getAvaliacaoDe(f2).isEmpty() && ana.getDiario().stream().noneMatch(e -> e.getAlbum().equals(criado)));

        System.out.println("6. Perfil e diário");
        TelaPerfilDiario perfil = naInterface(() -> new TelaPerfilDiario(nav));
        int entradas = ana.getDiario().size();
        Achar.naInterface(() -> {
            Achar.todos(perfil, Seletor.class).get(0).setSelectedIndex(0);
            campo(perfil, "Data da audição", 0).setText(Formatos.data(LocalDate.now().plusDays(3)));
        });
        clicar(perfil, "Registrar");
        ok("data no futuro é recusada (RN10)", errosVisiveis(perfil) > 0 && ana.getDiario().size() == entradas && Achar.temTexto(perfil, "não pode estar no futuro"));
        Achar.naInterface(() -> campo(perfil, "Data da audição", 0).setText("31/02/2026"));
        clicar(perfil, "Registrar");
        ok("data inexistente (31/02) é recusada", ana.getDiario().size() == entradas);
        Achar.naInterface(() -> campo(perfil, "Data da audição", 0).setText(Formatos.data(LocalDate.now())));
        clicar(perfil, "Registrar");
        ok("audição válida entra no diário (RF07)", ana.getDiario().size() == entradas + 1);
        EntradaDiario nova = ana.getDiario().get(0);
        ok("entradas ficam em ordem cronológica inversa (RF08)", !ana.getDiario().get(0).getDataAudicao().isBefore(ana.getDiario().get(1).getDataAudicao()));
        ctx.perfil.editarEntrada(nova, Formatos.data(LocalDate.now().minusDays(1)), "Comentário editado");
        ok("editar a entrada muda data e comentário (RF10)", "Comentário editado".equals(nova.getComentario()));
        ctx.perfil.excluirEntrada(ana, nova);
        ok("excluir a entrada a remove do diário (RF10)", ana.getDiario().size() == entradas);

        System.out.println("7. Temas e tamanho do texto");
        boolean semExcecao = true;
        for (Tema t : Tema.values()) {
            for (double escala : new double[] { 1.0, 1.5 }) {
                Tema.definir(t);
                br.com.songlog.view.Preferencias.definirEscala(escala);
                try {
                    naInterface(() -> new TelaCatalogo(nav));
                    naInterface(() -> new TelaAvaliacao(nav, kob));
                    naInterface(() -> new TelaAlbum(nav, kob));
                    naInterface(() -> new TelaPerfilDiario(nav));
                    naInterface(() -> new TelaLogin(nav));
                } catch (RuntimeException e) {
                    semExcecao = false;
                    e.printStackTrace();
                }
            }
        }
        Tema.definir(Tema.CLARO);
        br.com.songlog.view.Preferencias.definirEscala(1.0);
        ok("as 5 telas montam nos 3 temas a 100% e 150%", semExcecao);

        System.out.println();
        System.out.println(total - falhas + " de " + total + " verificações passaram" + (falhas == 0 ? "." : "; " + falhas + " FALHARAM."));
        System.exit(falhas == 0 ? 0 : 1);
    }

    static String mensagem(Runnable r) {
        try {
            r.run();
            return "";
        } catch (ValidacaoException | IllegalArgumentException e) {
            return e.getMessage();
        }
    }
}
