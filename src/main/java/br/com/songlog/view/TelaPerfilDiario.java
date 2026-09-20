package br.com.songlog.view;

import static br.com.songlog.view.Preferencias.px;

import br.com.songlog.model.Album;
import br.com.songlog.model.Avaliacao;
import br.com.songlog.model.EntradaDiario;
import br.com.songlog.model.Faixa;
import br.com.songlog.model.Usuario;
import br.com.songlog.service.Contexto;
import br.com.songlog.service.PerfilService.Estatisticas;
import br.com.songlog.service.ValidacaoException;
import br.com.songlog.util.Formatos;
import br.com.songlog.view.Fontes.Estilo;
import br.com.songlog.view.componentes.Botao;
import br.com.songlog.view.componentes.Campo;
import br.com.songlog.view.componentes.MensagemErro;
import br.com.songlog.view.componentes.Rolagem;
import br.com.songlog.view.componentes.Seletor;
import br.com.songlog.view.componentes.Tag;
import br.com.songlog.view.dialogo.Dialogos;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * W5: perfil e diário de audição (RF07 a RF10). Estatísticas, diário agrupado por mês
 * (mais recente primeiro), registro de nova audição e últimas avaliações.
 */
public class TelaPerfilDiario extends JPanel implements Tela {

    private final Navegador nav;
    private final Contexto ctx;
    private final Usuario usuario;

    private final JPanel estatisticas = new JPanel(new GridLayout(1, 4));
    private final JPanel diario = Ui.painel();
    private final JPanel ultimas = Ui.painel();
    private final Seletor<Album> album = new Seletor<Album>("Álbum").dica("Escolha o álbum");
    private final Campo data = new Campo("Data da audição");
    private final Campo comentario = new Campo("Comentário").dica("Opcional, até 500 caracteres");
    private final MensagemErro eAlbum = new MensagemErro();
    private final MensagemErro eData = new MensagemErro();
    private final MensagemErro eComentario = new MensagemErro();

    public TelaPerfilDiario(Navegador nav) {
        this.nav = nav;
        this.ctx = nav.contexto();
        this.usuario = ctx.usuario();
        Tema t = Tema.atual();
        setOpaque(true);
        setBackground(t.bg);
        setLayout(new BorderLayout());
        setBorder(Ui.margem(28, 40, 0, 40));

        JLabel nome = Ui.texto(usuario.getNome(), Estilo.H1, t.ink);
        JLabel desde = Ui.texto("Membro desde " + Formatos.data(usuario.getDataCadastro()), Estilo.SMALL, t.ink2);
        desde.setBorder(Ui.margem(4, 0, 12, 0));
        estatisticas.setOpaque(false);
        estatisticas.setBorder(BorderFactory.createCompoundBorder(Ui.filete(t.ink, 2, 0, 0, 0), Ui.filete(t.rule, 0, 0, 1, 0)));
        add(Ui.coluna(nome, desde, estatisticas), BorderLayout.NORTH);

        diario.setLayout(new BoxLayout(diario, BoxLayout.Y_AXIS));
        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.add(diario, BorderLayout.NORTH);
        JPanel esquerda = new JPanel(new BorderLayout());
        esquerda.setOpaque(false);
        JLabel h2 = Ui.texto("Diário", Estilo.H2, t.ink);
        h2.setBorder(Ui.margem(0, 0, 8, 0));
        esquerda.add(h2, BorderLayout.NORTH);
        esquerda.add(new Rolagem(envoltorio), BorderLayout.CENTER);

        JComponent direita = colunaRegistro(t);
        direita.setPreferredSize(new Dimension(px(360), 10));
        JPanel colunas = new JPanel(new BorderLayout(px(56), 0));
        colunas.setOpaque(false);
        colunas.setBorder(Ui.margem(16, 0, 0, 0));
        colunas.add(esquerda, BorderLayout.CENTER);
        colunas.add(direita, BorderLayout.EAST);
        add(colunas, BorderLayout.CENTER);
        atualizar();
    }

    // ---------------------------------------------------------------- montagem

    private JComponent colunaRegistro(Tema t) {
        for (Album a : ctx.albuns.todos()) {
            album.addItem(a);
        }
        album.setSelectedIndex(-1);
        data.setText(Formatos.data(LocalDate.now()));
        Botao registrar = Botao.primario("Registrar").larguraTotal();
        registrar.addActionListener(e -> registrar());
        data.addActionListener(e -> registrar());
        comentario.addActionListener(e -> registrar());

        JLabel h2 = Ui.texto("Registrar audição", Estilo.H2, t.ink);
        h2.setBorder(Ui.margem(0, 0, 16, 0));
        JLabel tituloUltimas = Ui.texto("Últimas avaliações", Estilo.LABEL, t.ink2);
        tituloUltimas.setBorder(Ui.margem(0, 0, 8, 0));
        JPanel faixaUltimas = Ui.faixa(tituloUltimas, Ui.filete(t.ink, 0, 0, 2, 0));
        ultimas.setLayout(new BoxLayout(ultimas, BoxLayout.Y_AXIS));
        JPanel p = Ui.coluna(h2, campo("Álbum *", album, eAlbum), campo("Data *", data, eData),
                campo("Comentário (opcional)", comentario, eComentario), registrar, Ui.espaco(0, 16), faixaUltimas, ultimas);
        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.add(p, BorderLayout.NORTH);
        return new Rolagem(envoltorio);
    }

    private JPanel campo(String rotulo, JComponent c, MensagemErro erro) {
        JLabel r = Ui.texto(rotulo, Estilo.BODY_BOLD, Tema.atual().ink, 13);
        r.setBorder(Ui.margem(0, 0, 6, 0));
        JPanel g = Ui.coluna(r, c, erro);
        g.setBorder(Ui.margem(0, 0, 10, 0));
        return g;
    }

    // ---------------------------------------------------------------- dados

    private void atualizar() {
        Tema t = Tema.atual();
        Estatisticas e = ctx.perfil.estatisticas(usuario);
        estatisticas.removeAll();
        celulaEstatistica("Álbuns ouvidos", String.valueOf(e.albunsOuvidos()), false, false);
        celulaEstatistica("Faixas avaliadas", String.valueOf(e.faixasAvaliadas()), false, true);
        celulaEstatistica("Nota média dada", e.notaMedia().isPresent() ? Formatos.nota2(e.notaMedia().getAsDouble()) : "—", e.notaMedia().isPresent(), true);
        celulaEstatistica("Reaudições", String.valueOf(e.reaudicoes()), false, true);

        diario.removeAll();
        List<EntradaDiario> entradas = usuario.getDiario();
        if (entradas.isEmpty()) {
            diario.add(Ui.coluna(Ui.texto("Seu diário está vazio. Registre o primeiro álbum que você ouviu.", Estilo.BODY, t.ink2)));
        }
        String mesAtual = "";
        for (EntradaDiario en : entradas) {
            String mes = Formatos.mesAno(en.getDataAudicao());
            if (!mes.equals(mesAtual)) {
                mesAtual = mes;
                JLabel m = Ui.texto(mes, Estilo.LABEL, t.ink2);
                m.setBorder(Ui.margem(0, 0, 8, 0));
                JPanel faixaMes = Ui.faixa(m, Ui.filete(t.ink, 0, 0, 2, 0));
                JPanel bloco = Ui.coluna(Ui.espaco(0, diario.getComponentCount() == 0 ? 8 : 24), faixaMes);
                diario.add(Ui.coluna(bloco));
            }
            diario.add(Ui.coluna(entrada(en, t)));
        }
        diario.revalidate();
        diario.repaint();

        ultimas.removeAll();
        for (Avaliacao a : usuario.getUltimasAvaliacoes(3)) {
            Album dono = albumDe(a.getFaixa());
            Botao link = Botao.link(a.getFaixa().getTitulo());
            link.getAccessibleContext().setAccessibleName("Abrir " + a.getFaixa().getTitulo() + (dono == null ? "" : ", do álbum " + dono.getTitulo()));
            link.addActionListener(ev -> {
                if (dono != null) {
                    nav.irParaAvaliacao(dono);
                }
            });
            JPanel linha = new JPanel(new BorderLayout());
            linha.setOpaque(false);
            linha.setBorder(BorderFactory.createCompoundBorder(Ui.filete(t.ruleSoft, 0, 0, 1, 0), Ui.margem(2, 0, 2, 0)));
            linha.add(link, BorderLayout.WEST);
            linha.add(Ui.texto(Formatos.nota1(a.getNota()), Estilo.MONO, t.accent), BorderLayout.EAST);
            ultimas.add(Ui.coluna(linha));
        }
        if (usuario.getAvaliacoes().isEmpty()) {
            ultimas.add(Ui.coluna(Ui.texto("Nenhuma faixa avaliada ainda.", Estilo.SMALL, t.ink2)));
        }
        ultimas.revalidate();
        ultimas.repaint();
        estatisticas.revalidate();
        estatisticas.repaint();
    }

    private void celulaEstatistica(String rotulo, String valor, boolean destaque, boolean regua) {
        Tema t = Tema.atual();
        JPanel c = Ui.coluna(Ui.texto(rotulo, Estilo.LABEL, t.ink2), Ui.texto(valor, Estilo.H1, destaque ? t.accent : t.ink));
        c.setBorder(BorderFactory.createCompoundBorder(Ui.filete(t.rule, 0, regua ? 1 : 0, 0, 0), Ui.margem(8, 20, 4, 20)));
        estatisticas.add(c);
    }

    private JComponent entrada(EntradaDiario en, Tema t) {
        JPanel linha = new JPanel(new BorderLayout(px(0), 0));
        linha.setOpaque(false);
        linha.setBorder(BorderFactory.createCompoundBorder(Ui.filete(t.ruleSoft, 0, 0, 1, 0), Ui.margem(12, 0, 12, 0)));

        JLabel dia = Ui.texto(Formatos.diaMes(en.getDataAudicao()), Estilo.MONO, t.ink);
        dia.setPreferredSize(new Dimension(px(72), px(20)));
        dia.setVerticalAlignment(JLabel.TOP);
        dia.getAccessibleContext().setAccessibleName("Audição em " + Formatos.data(en.getDataAudicao()));
        linha.add(dia, BorderLayout.WEST);

        Botao titulo = Botao.link(en.getAlbum().getTitulo() + " — " + en.getAlbum().getArtista());
        titulo.setFont(Estilo.BODY_BOLD.fonte());
        titulo.addActionListener(e -> nav.irParaAvaliacao(en.getAlbum()));
        JPanel l1 = Ui.linha(titulo);
        if (en.isReaudicao()) {
            l1.add(Ui.espaco(8, 0));
            l1.add(new Tag("Reaudição"));
        }
        l1.add(Ui.cola());
        JPanel centro = Ui.coluna(l1);
        if (en.getComentario() != null && !en.getComentario().isBlank()) {
            JLabel c = Ui.texto("“" + en.getComentario() + "”", Estilo.ITALICO, t.ink2);
            c.setBorder(Ui.margem(2, 4, 0, 8)); // folga: o itálico avança além da largura calculada
            centro.add(c);
        }
        linha.add(centro, BorderLayout.CENTER);

        Botao editar = Botao.link("Editar");
        editar.getAccessibleContext().setAccessibleName("Editar audição de " + en.getAlbum().getTitulo() + " em " + Formatos.data(en.getDataAudicao()));
        editar.addActionListener(e -> {
            if (Dialogos.editarEntrada(nav.janela(), ctx, en)) {
                nav.status("Audição atualizada.");
                atualizar();
            }
        });
        Botao excluir = Botao.link("Excluir");
        excluir.getAccessibleContext().setAccessibleName("Excluir audição de " + en.getAlbum().getTitulo() + " em " + Formatos.data(en.getDataAudicao()));
        excluir.addActionListener(e -> {
            String msg = "A audição de “" + en.getAlbum().getTitulo() + "” em " + Formatos.data(en.getDataAudicao())
                    + " será removida do diário. As notas das faixas não mudam.";
            if (Dialogos.confirmar(nav.janela(), "Excluir esta audição?", msg, "Excluir audição", "Cancelar")) {
                ctx.perfil.excluirEntrada(usuario, en);
                nav.status("Audição excluída do diário.");
                atualizar();
            }
        });
        JPanel acoes = Ui.linha(editar, Ui.espaco(8, 0), excluir);
        JPanel dir = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        dir.setOpaque(false);
        dir.add(acoes);
        linha.add(dir, BorderLayout.EAST);
        return linha;
    }

    private Album albumDe(Faixa f) {
        for (Album a : ctx.albuns.todos()) {
            if (a.getFaixas().contains(f)) {
                return a;
            }
        }
        return null;
    }

    // ---------------------------------------------------------------- ações

    private void registrar() {
        eAlbum.limpar();
        eData.limpar();
        eComentario.limpar();
        data.setErro(false);
        comentario.setErro(false);
        try {
            ctx.perfil.registrarAudicao(usuario, (Album) album.getSelectedItem(), data.getText(), comentario.getText());
            Album a = (Album) album.getSelectedItem();
            nav.status("Audição de “" + a.getTitulo() + "” registrada.");
            album.setSelectedIndex(-1);
            comentario.setText("");
            data.setText(Formatos.data(LocalDate.now()));
            atualizar();
        } catch (ValidacaoException ex) {
            switch (String.valueOf(ex.getCampo())) {
                case "album" -> {
                    eAlbum.mostrar(ex.getMessage());
                    album.requestFocusInWindow();
                }
                case "data" -> {
                    eData.mostrar(ex.getMessage());
                    data.setErro(true);
                    data.requestFocusInWindow();
                }
                default -> {
                    eComentario.mostrar(ex.getMessage());
                    comentario.setErro(true);
                }
            }
            nav.status("Corrija o campo indicado para registrar a audição.");
            revalidate();
        }
    }

    @Override
    public void aoExibir() {
        album.requestFocusInWindow();
    }
}
