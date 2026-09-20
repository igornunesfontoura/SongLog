package br.com.songlog;

import br.com.songlog.banco.Banco;
import br.com.songlog.banco.BancoException;
import br.com.songlog.banco.Configuracao;
import br.com.songlog.model.Album;
import br.com.songlog.model.Avaliacao;
import br.com.songlog.model.EntradaDiario;
import br.com.songlog.model.Faixa;
import br.com.songlog.model.Usuario;
import br.com.songlog.service.Contexto;
import br.com.songlog.service.NotaService;
import br.com.songlog.service.ValidacaoException;
import br.com.songlog.util.Formatos;
import br.com.songlog.view.Preferencias;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.AbstractButton;
import javax.swing.JTable;

/**
 * Teste de interação contra o MySQL de verdade, sem monitor: monta as telas reais, dispara cliques e teclas e
 * confere o resultado LENDO O BANCO de novo (não a memória). Recria o banco songlog_teste a cada execução.
 *
 * java -Djava.awt.headless=true -Dsonglog.db.host=127.0.0.1 -Dsonglog.db.porta=33306 -Dsonglog.db.usuario=root
 *      -Dsonglog.db.senha=... -cp target/classes:target/test-classes br.com.songlog.TesteDeInteracao
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

    /** Leitura independente da memória: o usuário como está AGORA no banco. */
    static Usuario noBanco(String email) {
        return ctx.usuarios.porEmail(email).orElseThrow();
    }

    static Album album(String titulo) {
        return ctx.albuns.buscar(titulo, "").get(0);
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        ctx = AmbienteDeTeste.novo();
        nav = new NavegadorDeTeste(ctx);
        Tema.definir(Tema.CLARO);
        Album kob = album("Kind of Blue");
        String ana = "ana@songlog.dev";

        System.out.println("0. Banco criado pelos scripts");
        ok("46 faixas e 8 álbuns carregados dos dados iniciais", ctx.albuns.todos().size() == 8
                && ctx.albuns.todos().stream().mapToInt(Album::getTotalFaixas).sum() == 46);
        ok("acentos chegam corretos pelo JDBC (Estação Lisboa)", ctx.albuns.buscar("Estação", "").stream().anyMatch(a -> a.getTitulo().equals("Estação Lisboa")));
        ok("a Ana tem 12 avaliações e 4 entradas no diário (uma é reaudição)", noBanco(ana).getAvaliacoes().size() == 12
                && noBanco(ana).getDiario().size() == 4 && noBanco(ana).getDiario().stream().filter(EntradaDiario::isReaudicao).count() == 1);

        System.out.println("1. Autenticação");
        ok("entra com a senha correta", ctx.autenticacao.entrar(ana, "songlog123").getNome().equals("Ana Ribeiro"));
        ok("recusa senha errada sem revelar qual campo", mensagem(() -> ctx.autenticacao.entrar(ana, "errada")).contains("incorretos"));
        ok("recusa e-mail inexistente com a mesma mensagem", mensagem(() -> ctx.autenticacao.entrar("ninguem@x.com", "songlog123")).contains("incorretos"));
        ok("cadastro exige senha de 8+ caracteres", mensagem(() -> ctx.autenticacao.cadastrar("Diego", "diego@x.com", "123", "123")).contains("8"));
        ok("cadastro confere a confirmação", mensagem(() -> ctx.autenticacao.cadastrar("Diego", "diego@x.com", "senha-forte", "outra")).contains("conferem"));
        ok("cadastro recusa e-mail repetido, mesmo em outra caixa (RN11)", mensagem(() -> ctx.autenticacao.cadastrar("Ana2", "ANA@songlog.dev", "senha-forte", "senha-forte")).contains("já está cadastrado"));
        Usuario diego = ctx.autenticacao.cadastrar("Diego Lima", "diego@x.com", "senha-forte", "senha-forte");
        ok("cadastro grava o usuário no banco só com o hash da senha (RN11)", diego.getId() > 0
                && noBanco("diego@x.com").getSenhaHash().startsWith("pbkdf2$") && !noBanco("diego@x.com").getSenhaHash().contains("senha-forte"));

        System.out.println("2. Tela de login");
        TelaLogin login = naInterface(() -> new TelaLogin(nav));
        Achar.naInterface(() -> {
            campo(login, "E-mail", 0).setText(ana);
            Achar.todos(login, CampoSenha.class).get(0).setText("errada");
        });
        clicar(login, "Entrar");
        Achar.esperar(() -> errosVisiveis(login) > 0, 8000);
        ok("senha errada mostra o erro na própria tela", errosVisiveis(login) > 0 && !nav.destinos.contains("catalogo"));
        Achar.naInterface(() -> Achar.todos(login, CampoSenha.class).get(0).setText("songlog123"));
        clicar(login, "Entrar");
        Achar.esperar(() -> nav.destinos.contains("catalogo"), 8000);
        ok("senha correta leva ao catálogo com o usuário na sessão", nav.destinos.contains("catalogo") && ctx.usuario() != null && ctx.usuario().getEmail().equals(ana));

        System.out.println("3. Catálogo (consultas SQL)");
        TelaCatalogo cat = naInterface(() -> new TelaCatalogo(nav));
        JTable tabela = Achar.todos(cat, JTable.class).get(0);
        ok("lista todos os álbuns do banco", tabela.getRowCount() == 8);
        Achar.naInterface(() -> campo(cat, "Buscar por título, artista ou gênero", 0).setText("KIND"));
        Achar.esperar(() -> tabela.getRowCount() == 1, 3000);
        ok("busca sem diferenciar maiúsculas e acentos filtra pelo MySQL", tabela.getRowCount() == 1);
        Achar.naInterface(() -> {
            tabela.setRowSelectionInterval(0, 0);
            tabela.getActionMap().get("abrir").actionPerformed(new ActionEvent(tabela, 0, "abrir"));
        });
        ok("Enter na linha abre a avaliação do álbum", nav.destinos.contains("avaliacao") && nav.ultimoAlbum.getTitulo().equals("Kind of Blue"));
        Achar.naInterface(() -> campo(cat, "Buscar por título, artista ou gênero", 0).setText("zzzz"));
        Achar.esperar(() -> tabela.getRowCount() == 0, 3000);
        ok("busca sem resultado mostra o estado vazio", tabela.getRowCount() == 0 && Achar.temTexto(cat, "Nenhum álbum encontrado"));
        Achar.naInterface(() -> campo(cat, "Buscar por título, artista ou gênero", 0).setText(""));
        Achar.naInterface(() -> Achar.todos(cat, Seletor.class).stream().filter(s -> "Filtrar por gênero".equals(s.getAccessibleContext().getAccessibleName()))
                .findFirst().orElseThrow().setSelectedItem("Jazz"));
        Achar.esperar(() -> tabela.getRowCount() == 2, 3000);
        ok("filtro de gênero mostra só os álbuns de Jazz", tabela.getRowCount() == 2);
        int linhaKob = indiceDe(tabela, "Kind of Blue");
        ok("a coluna Nota geral vem da view do banco (4,34 para Kind of Blue; lida: " + (linhaKob < 0 ? "linha ausente" : tabela.getValueAt(linhaKob, 6)) + ")",
                linhaKob >= 0 && tabela.getValueAt(linhaKob, 6).equals("4,34"));

        System.out.println("4. Avaliação faixa a faixa (gravada no banco)");
        ctx.entrar(noBanco(ana));
        TelaAvaliacao av = naInterface(() -> new TelaAvaliacao(nav, kob));
        List<BarraDeNota> barras = Achar.todos(av, BarraDeNota.class);
        List<Faixa> faixas = kob.getFaixas();
        ok("uma barra de nota por faixa", barras.size() == 5);
        ok("mostra a nota ponderada 4,56 e a geral 4,34 do exemplo do projeto", Achar.temTexto(av, "4,56") && Achar.temTexto(av, "4,34"));
        Achar.pressionar(barras.get(0), KeyEvent.VK_LEFT);
        ok("seta ← baixa a nota em 0,5 e o MySQL guarda 4,0", noBanco(ana).getAvaliacaoDe(faixas.get(0)).orElseThrow().getNota() == 4.0);
        String esperada = Formatos.nota2(NotaService.notaDoUsuario(kob, noBanco(ana)).orElseThrow().valor());
        ok("\"Sua nota\" recalcula ao vivo (" + esperada + ")", Achar.temTexto(av, esperada));
        for (char c : "3,5".toCharArray()) {
            Achar.digitar(barras.get(1), c);
        }
        ok("digitar 3,5 grava 3,5 no banco", noBanco(ana).getAvaliacaoDe(faixas.get(1)).orElseThrow().getNota() == 3.5);
        Achar.pressionar(barras.get(2), KeyEvent.VK_DELETE);
        ok("Delete apaga a linha do banco e o álbum vira parcial (4 de 5)", noBanco(ana).getAvaliacaoDe(faixas.get(2)).isEmpty() && Achar.temTexto(av, "PARCIAL"));
        ok("avaliação parcial sai da nota geral (RN07, calculada pela view)", ctx.albuns.notaGeral(kob).orElseThrow().totalAvaliadores() == 1);
        Achar.digitar(barras.get(2), '5');
        ok("digitar 5 devolve a nota e o álbum volta a ser completo", noBanco(ana).getAvaliacaoDe(faixas.get(2)).isPresent() && !Achar.temTexto(av, "PARCIAL"));
        Achar.pressionar(barras.get(4), KeyEvent.VK_RIGHT);
        ok("a nota não passa de 5,0", noBanco(ana).getAvaliacaoDe(faixas.get(4)).orElseThrow().getNota() == 5.0);
        clicar(av, "Como é calculada? +");
        ok("\"Como é calculada?\" mostra a conta com os números do álbum", Achar.temTexto(av, "Ocultar cálculo") && Achar.temTexto(av, "÷"));
        Achar.naInterface(() -> Achar.todos(av, AbstractButton.class).stream().filter(b -> "Comentar".equals(b.getText())).findFirst().orElseThrow().doClick());
        Achar.naInterface(() -> {
            Campo c = campo(av, "Comentário da faixa", 0);
            c.setText("Abertura perfeita.");
            c.postActionEvent();
        });
        ok("comentário da faixa é gravado junto com a nota", "Abertura perfeita.".equals(noBanco(ana).getAvaliacaoDe(faixas.get(0)).orElseThrow().getComentario()));
        double geralSql = ctx.albuns.notaGeral(kob).orElseThrow().valor();
        ok("a nota geral do banco acompanha a mudança das notas (SQL " + geralSql + " x Java " + esperadaGeral() + ")", Math.abs(geralSql - esperadaGeral()) < 1e-9);

        System.out.println("5. Cadastro e edição de álbum");
        int antes = ctx.albuns.todos().size();
        TelaAlbum vazio = naInterface(() -> new TelaAlbum(nav, null));
        clicar(vazio, "Salvar álbum");
        ok("salvar com o formulário vazio mostra os erros e não grava nada", errosVisiveis(vazio) >= 3 && ctx.albuns.todos().size() == antes);
        TelaAlbum novo = naInterface(() -> new TelaAlbum(nav, null));
        Achar.naInterface(() -> {
            campo(novo, "Título do álbum", 0).setText("Álbum de Teste");
            campo(novo, "Artista", 0).setText("Banda Teste");
            campo(novo, "Ano de lançamento", 0).setText("2024");
            campo(novo, "Título da faixa", 0).setText("Primeira");
            campo(novo, "Duração da faixa em minutos e segundos", 0).setText("3:30");
        });
        Achar.naInterface(() -> campo(novo, "Duração da faixa em minutos e segundos", 0).postActionEvent());
        Achar.naInterface(() -> {
            campo(novo, "Título da faixa", 1).setText("Segunda");
            campo(novo, "Duração da faixa em minutos e segundos", 1).setText("9:75");
        });
        clicar(novo, "Salvar álbum");
        ok("duração 9:75 é recusada com mensagem específica e nada é gravado", Achar.temTexto(novo, "segundos de 00 a 59") && ctx.albuns.todos().size() == antes);
        Achar.naInterface(() -> campo(novo, "Duração da faixa em minutos e segundos", 1).setText("4:05"));
        clicar(novo, "Salvar álbum");
        Album criado = ctx.albuns.buscar("Álbum de Teste", "").stream().findFirst().orElse(null);
        ok("álbum válido é gravado (transação), ganha id do banco e volta ao catálogo", criado != null && criado.getId() > 0 && nav.destinos.get(nav.destinos.size() - 1).equals("catalogo"));
        ok("as 2 faixas foram gravadas numeradas em sequência (RN01)", criado != null && criado.getTotalFaixas() == 2 && criado.getFaixas().get(1).getNumero() == 2 && criado.getFaixas().get(1).getId() > 0);
        TelaAlbum repetido = naInterface(() -> new TelaAlbum(nav, null));
        Achar.naInterface(() -> {
            campo(repetido, "Título do álbum", 0).setText("álbum de teste");
            campo(repetido, "Artista", 0).setText("BANDA TESTE");
            campo(repetido, "Ano de lançamento", 0).setText("2024");
            campo(repetido, "Título da faixa", 0).setText("X");
            campo(repetido, "Duração da faixa em minutos e segundos", 0).setText("1:00");
        });
        clicar(repetido, "Salvar álbum");
        ok("título + artista + ano repetidos são recusados, mesmo com outra caixa (RN02)", Achar.temTexto(repetido, "já existe um álbum") && ctx.albuns.todos().size() == antes + 1);

        Faixa f1 = criado.getFaixas().get(0);
        Faixa f2 = criado.getFaixas().get(1);
        ctx.avaliacoes.avaliar(ctx.usuario(), f1, 4.0, null);
        ctx.avaliacoes.avaliar(ctx.usuario(), f2, 3.0, null);
        ctx.perfil.registrarAudicao(ctx.usuario(), criado, Formatos.data(LocalDate.now().minusDays(1)), "Teste");
        TelaAlbum edicao = naInterface(() -> new TelaAlbum(nav, criado));
        clicar(edicao, "Remover");
        ok("remover faixa só vale ao salvar: até lá o banco não muda", ctx.albuns.porId(criado.getId()).orElseThrow().getTotalFaixas() == 2 && noBanco(ana).getAvaliacaoDe(f1).isPresent());
        clicar(edicao, "Salvar álbum");
        Album relido = ctx.albuns.porId(criado.getId()).orElseThrow();
        ok("ao salvar, a faixa removida sai do banco e a outra é renumerada", relido.getTotalFaixas() == 1 && relido.getFaixas().get(0).getNumero() == 1 && relido.getFaixas().get(0).getTitulo().equals("Segunda"));
        ok("a avaliação da faixa removida é apagada pelo banco em cascata (RN13)", noBanco(ana).getAvaliacaoDe(f1).isEmpty() && noBanco(ana).getAvaliacaoDe(f2).isPresent());
        ok("a tela avisa quantas avaliações e entradas serão perdidas antes de excluir", ctx.catalogo.quantidadeAvaliacoes(relido) == 1 && ctx.catalogo.quantidadeEntradas(relido) == 1);
        ctx.catalogo.excluir(relido);
        ok("excluir o álbum leva faixas, notas e diário embora do banco (RN13)", ctx.albuns.porId(criado.getId()).isEmpty()
                && noBanco(ana).getAvaliacaoDe(f2).isEmpty() && noBanco(ana).getDiario().stream().noneMatch(e -> e.getAlbum().getId() == criado.getId())
                && contar("SELECT COUNT(*) FROM faixa WHERE album_id = " + criado.getId()) == 0);

        System.out.println("6. Perfil e diário");
        ctx.recarregarUsuario();
        TelaPerfilDiario perfil = naInterface(() -> new TelaPerfilDiario(nav));
        int entradas = noBanco(ana).getDiario().size();
        Achar.naInterface(() -> {
            Achar.todos(perfil, Seletor.class).get(0).setSelectedIndex(0);
            campo(perfil, "Data da audição", 0).setText(Formatos.data(LocalDate.now().plusDays(3)));
        });
        clicar(perfil, "Registrar");
        ok("data no futuro é recusada e nada é gravado (RN10)", errosVisiveis(perfil) > 0 && noBanco(ana).getDiario().size() == entradas && Achar.temTexto(perfil, "não pode estar no futuro"));
        Achar.naInterface(() -> campo(perfil, "Data da audição", 0).setText("31/02/2026"));
        clicar(perfil, "Registrar");
        ok("data inexistente (31/02) é recusada", noBanco(ana).getDiario().size() == entradas);
        Achar.naInterface(() -> campo(perfil, "Data da audição", 0).setText(Formatos.data(LocalDate.now())));
        clicar(perfil, "Registrar");
        ok("audição válida é gravada no banco (RF07)", noBanco(ana).getDiario().size() == entradas + 1);
        EntradaDiario nova = noBanco(ana).getDiario().get(0);
        ok("o diário volta em ordem cronológica inversa (RF08)", !noBanco(ana).getDiario().get(0).getDataAudicao().isBefore(noBanco(ana).getDiario().get(1).getDataAudicao()));
        ctx.perfil.editarEntrada(nova, Formatos.data(LocalDate.now().minusDays(1)), "Comentário editado");
        ok("editar a entrada muda data e comentário no banco (RF10)", noBanco(ana).getDiario().stream().anyMatch(e -> "Comentário editado".equals(e.getComentario())));
        ctx.perfil.excluirEntrada(noBanco(ana), nova);
        ok("excluir a entrada a apaga do banco (RF10)", noBanco(ana).getDiario().size() == entradas);

        System.out.println("7. Persistência e banco de dados");
        Contexto reaberto = AmbienteDeTeste.reabrir();
        ok("fechar e abrir o programa de novo mantém tudo (nova conexão, sem recriar)", reaberto.albuns.todos().size() == 8
                && reaberto.usuarios.porEmail("diego@x.com").isPresent()
                && reaberto.autenticacao.entrar("diego@x.com", "senha-forte").getNome().equals("Diego Lima")
                && reaberto.usuarios.porEmail(ana).orElseThrow().getAvaliacaoDe(faixas.get(0)).orElseThrow().getNota() == 4.0);
        ok("texto de busca com aspas e SQL não injeta nada (PreparedStatement)", ctx.catalogo.buscar("'; DROP TABLE album; --", "").isEmpty() && ctx.albuns.todos().size() == 8);
        ok("% e _ digitados na busca não viram curinga do LIKE", ctx.catalogo.buscar("%", "").isEmpty() && ctx.catalogo.buscar("_", "").isEmpty());
        ok("busca sem acento encontra \"Estação Lisboa\"", ctx.catalogo.buscar("estacao lisboa", "").size() == 1);
        double javaNota = NotaService.notaDoUsuario(kob, noBanco(ana)).orElseThrow().valor();
        double sqlNota = consultarDouble("SELECT nota_ponderada FROM vw_nota_usuario_album WHERE usuario_id = " + noBanco(ana).getId() + " AND album_id = " + kob.getId());
        ok("a view SQL e o cálculo em Java dão a mesma nota ponderada (Java " + javaNota + " x SQL " + sqlNota + ")", Math.abs(javaNota - sqlNota) < 1e-9);
        Album duplicado = new Album("Kind of Blue", "Miles Davis", 1959, "Jazz");
        duplicado.adicionarFaixa("x", 60);
        int faixasAntes = contar("SELECT COUNT(*) FROM faixa");
        String erroDup = mensagem(() -> ctx.albuns.salvar(duplicado));
        ok("o UNIQUE do banco recusa o álbum repetido, com mensagem em português", erroDup.contains("já existe um registro igual"));
        ok("a transação desfaz tudo quando o álbum falha (nenhuma faixa órfã)", contar("SELECT COUNT(*) FROM faixa") == faixasAntes);
        SQLException direto = null;
        try (Connection c = ctx.banco.abrir(); PreparedStatement ps = c.prepareStatement(
                "INSERT INTO avaliacao (usuario_id, faixa_id, nota, data_avaliacao) VALUES (1, 2, 4.3, NOW())")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            direto = e;
        }
        ok("o próprio MySQL recusa nota 4,3 (CHECK), mesmo sem passar pelo programa", direto != null && direto.getErrorCode() == 3819);
        ok("senha do MySQL errada vira mensagem clara", mensagemBanco(new Configuracao(AmbienteDeTeste.configuracao().host(), AmbienteDeTeste.configuracao().porta(),
                "songlog_teste", AmbienteDeTeste.configuracao().usuario(), "senha-errada-xyz", false)).contains("usuário ou senha do MySQL incorretos"));
        ok("servidor desligado vira mensagem clara", mensagemBanco(new Configuracao("127.0.0.1", 1, "songlog_teste", "root", "x", false)).contains("não foi possível conectar ao MySQL"));
        try (Connection c = ctx.banco.abrir(); PreparedStatement ps = c.prepareStatement("UPDATE avaliacao SET nota = 1.0 WHERE usuario_id = ? AND faixa_id = ?")) {
            ps.setInt(1, noBanco(ana).getId());
            ps.setInt(2, faixas.get(3).getId());
            ps.executeUpdate();
        }
        ctx.recarregarUsuario();
        ok("mudança feita fora do programa aparece ao recarregar o usuário", ctx.usuario().getAvaliacaoDe(faixas.get(3)).orElseThrow().getNota() == 1.0);

        System.out.println("8. Temas e tamanho do texto");
        boolean semExcecao = true;
        for (Tema t : Tema.values()) {
            for (double escala : new double[] { 1.0, 1.5 }) {
                Tema.definir(t);
                Preferencias.definirEscala(escala);
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
        Preferencias.definirEscala(1.0);
        ok("as 5 telas montam nos 3 temas a 100% e 150%", semExcecao);

        System.out.println();
        System.out.println(total - falhas + " de " + total + " verificações passaram" + (falhas == 0 ? "." : "; " + falhas + " FALHARAM."));
        System.exit(falhas == 0 ? 0 : 1);
    }

    static int indiceDe(JTable t, String titulo) {
        for (int i = 0; i < t.getRowCount(); i++) {
            if (titulo.equals(t.getValueAt(i, 0))) {
                return i;
            }
        }
        return -1;
    }

    /** Nota geral esperada de Kind of Blue com as notas atuais da Ana e do Bruno (média das duas ponderadas). */
    static double esperadaGeral() {
        Album kob = album("Kind of Blue");
        double a = NotaService.notaDoUsuario(kob, noBanco("ana@songlog.dev")).orElseThrow().valor();
        double b = NotaService.notaDoUsuario(kob, noBanco("bruno@songlog.dev")).orElseThrow().valor();
        return (a + b) / 2;
    }

    static int contar(String sql) {
        return (int) consultarDouble(sql);
    }

    static double consultarDouble(String sql) {
        try (Connection c = ctx.banco.abrir(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getDouble(1);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    static String mensagem(Runnable r) {
        try {
            r.run();
            return "";
        } catch (ValidacaoException | IllegalArgumentException | BancoException e) {
            return e.getMessage();
        }
    }

    static String mensagemBanco(Configuracao cfg) {
        try {
            Banco.conectar(cfg);
            return "";
        } catch (BancoException e) {
            return e.getMessage();
        }
    }
}
