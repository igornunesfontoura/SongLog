package br.com.songlog.view;

import br.com.songlog.model.Album;
import br.com.songlog.service.Contexto;
import br.com.songlog.view.componentes.BarraDeStatus;
import br.com.songlog.view.componentes.BarraSuperior;
import br.com.songlog.view.componentes.BarraSuperior.Aba;
import br.com.songlog.view.dialogo.Dialogos;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * Janela única do aplicativo. Guarda a tela atual, monta a moldura (barra superior, conteúdo,
 * barra de status) e implementa a navegação entre as telas.
 */
public class Janela extends JFrame implements Navegador {

    private final Contexto ctx;
    private Tela atual;
    private Runnable reabrir = this::mostrarLogin;
    private String mensagem = "";
    private BarraDeStatus barraDeStatus;

    public Janela(Contexto ctx) {
        super("SongLog");
        this.ctx = ctx;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1024, 680));
        setSize(1280, 800);
        setLocationRelativeTo(null);
        registrarAtalhosGlobais();
        mostrarLogin();
    }

    private void registrarAtalhosGlobais() {
        atalho("ctrl 1", this::irParaCatalogo);
        atalho("ctrl 2", this::irParaPerfil);
        atalho("F1", this::mostrarAtalhos);
    }

    private void atalho(String tecla, Runnable acao) {
        String nome = "global-" + tecla;
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(tecla), nome);
        getRootPane().getActionMap().put(nome, new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ctx.usuario() != null || tecla.equals("F1")) {
                    acao.run();
                }
            }
        });
    }

    // ---------------------------------------------------------------- montagem

    public void mostrarLogin() {
        if (!liberarTelaAtual()) {
            return;
        }
        reabrir = this::mostrarLogin;
        TelaLogin tela = new TelaLogin(this);
        atual = tela;
        setTitle("SongLog — Entrar");
        setContentPane(tela);
        revalidate();
        repaint();
        tela.aoExibir();
    }

    private void mostrar(JPanel tela, Aba aba, String titulo, Runnable novaReabertura) {
        if (!liberarTelaAtual()) {
            return;
        }
        reabrir = novaReabertura;
        atual = (Tela) tela;
        barraDeStatus = new BarraDeStatus();
        barraDeStatus.mensagem(mensagem);
        JPanel moldura = new JPanel(new BorderLayout());
        moldura.setBackground(Tema.atual().bg);
        moldura.add(new BarraSuperior(this, aba), BorderLayout.NORTH);
        moldura.add(tela, BorderLayout.CENTER);
        moldura.add(barraDeStatus, BorderLayout.SOUTH);
        setTitle("SongLog — " + titulo);
        setContentPane(moldura);
        revalidate();
        repaint();
        atual.aoExibir();
    }

    private boolean liberarTelaAtual() {
        return atual == null || atual.podeSair();
    }

    // ---------------------------------------------------------------- Navegador

    @Override
    public void irParaCatalogo() {
        mostrar(new TelaCatalogo(this), Aba.CATALOGO, "Catálogo", this::irParaCatalogo);
    }

    @Override
    public void irParaAlbum(Album album) {
        mostrar(new TelaAlbum(this, album), Aba.CATALOGO, album == null ? "Novo álbum" : "Editar álbum", () -> irParaAlbum(album));
    }

    @Override
    public void irParaAvaliacao(Album album) {
        mostrar(new TelaAvaliacao(this, album), Aba.CATALOGO, album.getTitulo(), () -> irParaAvaliacao(album));
    }

    @Override
    public void irParaPerfil() {
        mostrar(new TelaPerfilDiario(this), Aba.PERFIL, "Perfil e Diário", this::irParaPerfil);
    }

    @Override
    public void sair() {
        if (!liberarTelaAtual()) {
            return;
        }
        ctx.sair();
        mensagem = "";
        atual = null;
        mostrarLogin();
    }

    @Override
    public void status(String texto) {
        mensagem = texto == null ? "" : texto;
        if (barraDeStatus != null) {
            barraDeStatus.mensagem(mensagem);
        }
    }

    @Override
    public void abrirPreferencias() {
        if (Dialogos.preferencias(this)) {
            SwingUtilities.invokeLater(() -> {
                atual = null; // já confirmamos os dados pendentes ao trocar; recria a tela atual com o novo visual
                reabrir.run();
            });
        }
    }

    @Override
    public void mostrarAtalhos() {
        Dialogos.atalhos(this);
    }

    @Override
    public Window janela() {
        return this;
    }

    @Override
    public Contexto contexto() {
        return ctx;
    }
}
