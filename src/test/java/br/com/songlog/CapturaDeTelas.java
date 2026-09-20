package br.com.songlog;

import br.com.songlog.model.Album;
import br.com.songlog.model.Usuario;
import br.com.songlog.view.componentes.Botao;
import br.com.songlog.view.componentes.Campo;
import br.com.songlog.view.dialogo.DialogoBase;
import br.com.songlog.view.dialogo.Dialogos;
import javax.swing.JTable;
import br.com.songlog.service.Contexto;
import br.com.songlog.view.Preferencias;
import br.com.songlog.view.Tema;
import br.com.songlog.view.TelaAlbum;
import br.com.songlog.view.TelaAvaliacao;
import br.com.songlog.view.TelaCatalogo;
import br.com.songlog.view.TelaLogin;
import br.com.songlog.view.TelaPerfilDiario;
import br.com.songlog.view.componentes.BarraDeStatus;
import br.com.songlog.view.componentes.BarraSuperior;
import br.com.songlog.view.componentes.BarraSuperior.Aba;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * Renderiza as telas em PNG sem abrir janela (modo headless), no tamanho do projeto de
 * interfaces (1280 x 800). Uso: java -Djava.awt.headless=true -cp target/classes:target/test-classes
 * br.com.songlog.CapturaDeTelas [pasta-de-saída]
 */
public class CapturaDeTelas {

    static NavegadorDeTeste nav;
    static File saida;

    public static void main(String[] args) throws Exception {
        System.setProperty("java.awt.headless", "true");
        saida = new File(args.length > 0 ? args[0] : "target/capturas");
        saida.mkdirs();
        Contexto ctx = Contexto.emMemoria();
        ctx.entrar(ctx.usuarios.porEmail("ana@songlog.dev").orElseThrow());
        nav = new NavegadorDeTeste(ctx);
        Album kob = ctx.albuns.todos().get(0);

        for (Tema tema : Tema.values()) {
            Tema.definir(tema);
            String sufixo = tema == Tema.CLARO ? "" : "-" + tema.name().toLowerCase();
            capturar("W1-login" + sufixo, () -> new TelaLogin(nav), null, "");
            capturar("W2-catalogo" + sufixo, () -> new TelaCatalogo(nav), Aba.CATALOGO, "“Kind of Blue” selecionado. Enter para avaliar.");
            capturar("W3-album" + sufixo, () -> new TelaAlbum(nav, kob), Aba.CATALOGO, "Alterações não salvas.");
            capturar("W4-avaliacao" + sufixo, () -> new TelaAvaliacao(nav, kob), Aba.CATALOGO, "Nota de “So What” salva: 4,5.");
            capturar("W5-perfil" + sufixo, () -> new TelaPerfilDiario(nav), Aba.PERFIL, "Audição registrada.");
        }
        estados(ctx, kob);
        Tema.definir(Tema.CLARO);
        System.out.println("Capturas em " + saida.getAbsolutePath());
    }

    /** Estados que não aparecem na tela "de repouso": erros, estado vazio, parcial, fórmula, diálogo, 150% e janela mínima. */
    static void estados(Contexto ctx, Album kob) throws Exception {
        Tema.definir(Tema.CLARO);
        Usuario ana = ctx.usuarios.porEmail("ana@songlog.dev").orElseThrow();
        Usuario carla = ctx.usuarios.porEmail("carla@songlog.dev").orElseThrow();
        capturar("estado-cadastro", () -> new TelaLogin(nav), null, "", 1280, 800, p -> clique(p, "Criar conta"));
        capturar("estado-catalogo-vazio", () -> new TelaCatalogo(nav), Aba.CATALOGO, "", 1280, 800,
                p -> Achar.todos(p, Campo.class).get(0).setText("zzzz"), 600);
        capturar("estado-album-erros", () -> new TelaAlbum(nav, null), Aba.CATALOGO, "Corrija 4 campos para salvar o álbum.", 1280, 800, p -> clique(p, "Salvar álbum"));
        capturar("estado-album-faixa-removida", () -> new TelaAlbum(nav, kob), Aba.CATALOGO, "Faixa marcada para remoção. Ela só sai ao salvar.", 1280, 800, p -> clique(p, "Remover"));
        capturar("estado-avaliacao-formula", () -> new TelaAvaliacao(nav, kob), Aba.CATALOGO, "", 1280, 800, p -> clique(p, "Como é calculada? +"));
        capturar("estado-avaliacao-comentario", () -> new TelaAvaliacao(nav, kob), Aba.CATALOGO, "Comentário da faixa “So What” salvo.", 1280, 800, p -> {
            clique(p, "Comentar");
            Achar.todos(p, Campo.class).get(0).setText("Abertura perfeita.");
        });
        ctx.entrar(carla);
        capturar("estado-avaliacao-parcial", () -> new TelaAvaliacao(nav, kob), Aba.CATALOGO, "Nota de “Blue in Green” salva: 4,5.", 1280, 800, p -> { });
        ctx.entrar(ana);
        capturar("estado-janela-minima-catalogo", () -> new TelaCatalogo(nav), Aba.CATALOGO, "", 1024, 680, p -> { });
        capturar("estado-janela-minima-avaliacao", () -> new TelaAvaliacao(nav, kob), Aba.CATALOGO, "", 1024, 680, p -> { });
        capturar("estado-janela-minima-perfil", () -> new TelaPerfilDiario(nav), Aba.PERFIL, "", 1024, 680, p -> { });
        Preferencias.definirEscala(1.5);
        capturar("estado-texto-150-avaliacao", () -> new TelaAvaliacao(nav, kob), Aba.CATALOGO, "", 1280, 800, p -> { });
        capturar("estado-texto-150-catalogo", () -> new TelaCatalogo(nav), Aba.CATALOGO, "", 1280, 800, p -> { });
        Preferencias.definirEscala(1.0);
        SwingUtilities.invokeAndWait(() -> {
            try {
                Botao cancelar = Botao.secundario("Cancelar");
                Botao excluir = Botao.primario("Excluir álbum");
                JPanel d = DialogoBase.montar("Excluir “Kind of Blue”?", Dialogos.corpoDeConfirmacao("Serão excluídas as 5 faixas, as 12 avaliações dessas faixas e as 4 entradas de diário deste álbum. Esta ação não pode ser desfeita."), cancelar, excluir);
                java.awt.Dimension pref = d.getPreferredSize();
                gravar("estado-dialogo-excluir", d, pref.width, pref.height);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    static void clique(JPanel painel, String texto) {
        Achar.botao(painel, texto).doClick();
    }

    static void capturar(String nome, Supplier<JPanel> tela, Aba aba, String status) throws Exception {
        capturar(nome, tela, aba, status, 1280, 800, p -> { });
    }

    static void capturar(String nome, Supplier<JPanel> tela, Aba aba, String status, int w, int h, Consumer<JPanel> acao) throws Exception {
        capturar(nome, tela, aba, status, w, h, acao, 0);
    }

    /** Monta a tela e roda a ação; espera {@code esperaMs} com a fila de eventos livre (temporizadores); e então desenha. */
    static void capturar(String nome, Supplier<JPanel> tela, Aba aba, String status, int w, int h, Consumer<JPanel> acao, long esperaMs) throws Exception {
        JPanel[] miolo = new JPanel[1];
        SwingUtilities.invokeAndWait(() -> {
            miolo[0] = tela.get();
            acao.accept(miolo[0]);
        });
        if (esperaMs > 0) {
            Thread.sleep(esperaMs);
            SwingUtilities.invokeAndWait(() -> { });
        }
        SwingUtilities.invokeAndWait(() -> {
            try {
                JPanel raiz = miolo[0];
                if (aba != null) {
                    JPanel moldura = new JPanel(new BorderLayout());
                    moldura.setBackground(Tema.atual().bg);
                    moldura.add(new BarraSuperior(nav, aba), BorderLayout.NORTH);
                    moldura.add(raiz, BorderLayout.CENTER);
                    BarraDeStatus b = new BarraDeStatus();
                    b.mensagem(status);
                    moldura.add(b, BorderLayout.SOUTH);
                    raiz = moldura;
                }
                gravar(nome, raiz, w, h);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    static void gravar(String nome, JPanel raiz, int w, int h) throws Exception {
        raiz.setSize(w, h);
        layout(raiz);
        layout(raiz); // segunda passada: as alturas dependem das larguras já resolvidas
        BufferedImage img = new BufferedImage(w * 2, h * 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.scale(2, 2);
        raiz.paint(g);
        g.dispose();
        ImageIO.write(img, "png", new File(saida, nome + ".png"));
    }

    static void layout(Component c) {
        if (c instanceof Container k) {
            k.doLayout();
            if (c instanceof JScrollPane sp) {
                sp.getViewport().doLayout();
            }
            for (Component f : k.getComponents()) {
                layout(f);
            }
        }
    }
}
