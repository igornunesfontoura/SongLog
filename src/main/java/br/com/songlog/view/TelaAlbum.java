package br.com.songlog.view;

import static br.com.songlog.view.Preferencias.px;

import br.com.songlog.model.Album;
import br.com.songlog.model.Faixa;
import br.com.songlog.service.CatalogoService.RascunhoAlbum;
import br.com.songlog.service.CatalogoService.RascunhoFaixa;
import br.com.songlog.service.Contexto;
import br.com.songlog.service.ValidacaoException;
import br.com.songlog.util.Tempo;
import br.com.songlog.view.Fontes.Estilo;
import br.com.songlog.view.componentes.Botao;
import br.com.songlog.view.componentes.Campo;
import br.com.songlog.view.componentes.MensagemErro;
import br.com.songlog.view.componentes.Rolagem;
import br.com.songlog.view.dialogo.Dialogos;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

/**
 * W3: cadastro e edição de álbum e faixas (RF02, RF03). Valida ao sair do campo e ao salvar.
 * Remover uma faixa só vale ao salvar; "Cancelar" e as abas pedem confirmação se houver alterações.
 */
public class TelaAlbum extends JPanel implements Tela {

    private final Navegador nav;
    private final Contexto ctx;
    private final Album existente;

    private final Campo titulo = new Campo("Título do álbum");
    private final Campo artista = new Campo("Artista");
    private final Campo ano = new Campo("Ano de lançamento");
    private final Campo genero = new Campo("Gênero");
    private final MensagemErro eTitulo = new MensagemErro();
    private final MensagemErro eArtista = new MensagemErro();
    private final MensagemErro eAno = new MensagemErro();
    private final MensagemErro eGenero = new MensagemErro();
    private final MensagemErro eAlbum = new MensagemErro();
    private final MensagemErro eFaixas = new MensagemErro();
    private final JPanel lista = Ui.painel();
    private final JLabel total = Ui.texto("", Estilo.MONO, Tema.atual().ink, 13);
    private final List<Linha> linhas = new ArrayList<>();
    private boolean sujo;
    private boolean carregando = true;

    public TelaAlbum(Navegador nav, Album existente) {
        this.nav = nav;
        this.ctx = nav.contexto();
        this.existente = existente;
        Tema t = Tema.atual();
        setOpaque(true);
        setBackground(t.bg);
        setLayout(new BorderLayout());
        setBorder(Ui.margem(28, 40, 0, 40));

        add(cabecalho(t), BorderLayout.NORTH);
        JPanel colunas = new JPanel(new BorderLayout(px(48), 0));
        colunas.setOpaque(false);
        colunas.setBorder(Ui.margem(24, 0, 0, 0));
        colunas.add(colunaDados(t), BorderLayout.WEST);
        colunas.add(colunaFaixas(t), BorderLayout.CENTER);
        add(colunas, BorderLayout.CENTER);
        add(rodape(t), BorderLayout.SOUTH);

        RascunhoAlbum r = existente == null ? new RascunhoAlbum() : ctx.catalogo.rascunhoDe(existente);
        titulo.setText(r.titulo);
        artista.setText(r.artista);
        ano.setText(r.ano);
        genero.setText(r.genero);
        if (r.faixas.isEmpty()) {
            adicionar(null, false);
        }
        for (RascunhoFaixa f : r.faixas) {
            adicionar(f, false);
        }
        renumerar();
        observar(titulo, "titulo");
        observar(artista, "artista");
        observar(ano, "ano");
        observar(genero, "genero");
        TelaCatalogo.acao(this, JComponent.WHEN_IN_FOCUSED_WINDOW, "ctrl S", "salvar", this::salvar);
        carregando = false;
    }

    // ---------------------------------------------------------------- montagem

    private JComponent cabecalho(Tema t) {
        JPanel crumb = Ui.linha();
        Botao catalogo = Botao.link("Catálogo");
        catalogo.addActionListener(e -> nav.irParaCatalogo());
        crumb.add(catalogo);
        if (existente != null) {
            crumb.add(Ui.texto(" / ", Estilo.SMALL, t.ink2));
            Botao a = Botao.link(existente.getTitulo());
            a.addActionListener(e -> nav.irParaAvaliacao(existente));
            crumb.add(a);
        }
        crumb.add(Ui.texto(" / ", Estilo.SMALL, t.ink2));
        crumb.add(Ui.texto(existente == null ? "Novo álbum" : "Editar", Estilo.SMALL, t.ink2));
        crumb.add(Ui.cola());
        JLabel h1 = Ui.texto(existente == null ? "Novo álbum" : "Editar álbum", Estilo.H1, t.ink);
        h1.setBorder(Ui.margem(0, 0, 16, 0));
        JPanel titulo = Ui.faixa(h1, Ui.filete(t.ink, 0, 0, 2, 0));
        return Ui.coluna(crumb, Ui.espaco(0, 4), titulo);
    }

    private JPanel grupo(String rotulo, JComponent campo, MensagemErro erro) {
        JLabel r = Ui.texto(rotulo, Estilo.BODY_BOLD, Tema.atual().ink, 13);
        r.setBorder(Ui.margem(0, 0, 6, 0));
        JPanel g = Ui.coluna(r, campo, erro);
        g.setBorder(Ui.margem(0, 0, 14, 0));
        return g;
    }

    private JComponent colunaDados(Tema t) {
        ano.setPreferredSize(new Dimension(px(120), px(36)));
        // os erros de Ano e Gênero ficam abaixo da linha, com a largura toda (a coluna do Ano é estreita)
        JPanel anoGenero = Ui.linha(grupo("Ano *", ano, new MensagemErro()), Ui.espaco(16, 0), grupo("Gênero", genero, new MensagemErro()));
        anoGenero.setBorder(Ui.margem(0, 0, 0, 0));
        JLabel legenda = Ui.texto("* obrigatório", Estilo.SMALL, t.mute);
        JPanel p = Ui.coluna(Ui.texto("Dados do álbum", Estilo.LABEL, t.ink2), Ui.espaco(0, 16),
                grupo("Título *", titulo, eTitulo), grupo("Artista *", artista, eArtista), anoGenero, eAno, eGenero, eAlbum, Ui.espaco(0, 8), legenda);
        p.setPreferredSize(new Dimension(px(400), 10));
        return p;
    }

    private JComponent colunaFaixas(Tema t) {
        JPanel cabecalhoTabela = new JPanel(new BorderLayout(px(12), 0));
        cabecalhoTabela.setBackground(t.surface);
        cabecalhoTabela.setBorder(BorderFactory.createCompoundBorder(Ui.filete(t.ink, 0, 0, 2, 0), Ui.margem(0, 16, 0, 0)));
        JLabel n = Ui.texto("#", Estilo.LABEL, t.ink2);
        n.setPreferredSize(new Dimension(px(32), px(34)));
        n.setHorizontalAlignment(JLabel.RIGHT);
        JPanel esq = new JPanel(new BorderLayout(px(12), 0));
        esq.setOpaque(false);
        esq.add(n, BorderLayout.WEST);
        esq.add(Ui.texto("Título", Estilo.LABEL, t.ink2), BorderLayout.CENTER);
        JPanel dir = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        dir.setOpaque(false);
        JLabel d = Ui.texto("Duração (m:ss)", Estilo.LABEL, t.ink2);
        d.setPreferredSize(new Dimension(px(130) + px(12) + px(100), px(34)));
        dir.add(d);
        cabecalhoTabela.add(esq, BorderLayout.CENTER);
        cabecalhoTabela.add(dir, BorderLayout.EAST);

        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setOpaque(false);

        Botao adicionar = Botao.secundario("Adicionar faixa");
        adicionar.addActionListener(e -> {
            Linha l = adicionar(null, true);
            l.titulo.requestFocusInWindow();
        });
        JPanel rodapeLista = new JPanel(new BorderLayout());
        rodapeLista.setOpaque(false);
        rodapeLista.setBorder(Ui.margem(16, 0, 12, 0));
        rodapeLista.add(adicionar, BorderLayout.WEST);
        rodapeLista.add(total, BorderLayout.EAST);

        JPanel topo = Ui.coluna(Ui.texto("Faixas", Estilo.LABEL, t.ink2), Ui.espaco(0, 16), cabecalhoTabela);
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JPanel norte = new JPanel(new BorderLayout());
        norte.setOpaque(false);
        norte.add(topo, BorderLayout.NORTH);
        norte.add(eFaixas, BorderLayout.SOUTH);
        p.add(norte, BorderLayout.NORTH);
        JPanel conteudo = Ui.coluna(lista, rodapeLista);
        envoltorio.add(conteudo, BorderLayout.NORTH);
        p.add(new Rolagem(envoltorio), BorderLayout.CENTER);
        return p;
    }

    private JComponent rodape(Tema t) {
        JPanel r = new JPanel(new BorderLayout());
        r.setOpaque(false);
        r.setBorder(BorderFactory.createCompoundBorder(Ui.filete(t.ink, 2, 0, 0, 0), Ui.margem(12, 0, 12, 0)));
        if (existente != null) {
            Botao excluir = Botao.secundario("Excluir álbum");
            excluir.addActionListener(e -> excluir());
            r.add(excluir, BorderLayout.WEST);
        }
        Botao cancelar = Botao.secundario("Cancelar");
        cancelar.addActionListener(e -> nav.irParaCatalogo());
        Botao salvar = Botao.primario("Salvar álbum");
        salvar.getAccessibleContext().setAccessibleDescription("Atalho Ctrl+S");
        salvar.addActionListener(e -> salvar());
        r.add(Ui.linha(cancelar, Ui.espaco(12, 0), salvar), BorderLayout.EAST);
        return r;
    }

    // ---------------------------------------------------------------- faixas

    private final class Linha {
        final Faixa existente;
        final JLabel numero = Ui.texto("", Estilo.MONO, Tema.atual().ink);
        final Campo titulo = new Campo("Título da faixa");
        final Campo duracao = new Campo("Duração da faixa em minutos e segundos").dica("m:ss");
        final Botao remover = Botao.secundario("Remover");
        final MensagemErro eT = new MensagemErro();
        final MensagemErro eD = new MensagemErro();
        final JPanel painel;
        boolean removida;

        Linha(Faixa existente) {
            this.existente = existente;
            numero.setHorizontalAlignment(JLabel.RIGHT);
            numero.setPreferredSize(new Dimension(px(32), px(36)));
            duracao.setPreferredSize(new Dimension(px(130), px(36)));
            duracao.setMaximumSize(new Dimension(px(130), px(36)));
            remover.setPreferredSize(new Dimension(px(100), px(36)));

            JPanel linha = new JPanel(new BorderLayout(px(12), 0));
            linha.setOpaque(false);
            linha.setBorder(Ui.margem(4, 16, 4, 0));
            JPanel esq = new JPanel(new BorderLayout(px(12), 0));
            esq.setOpaque(false);
            esq.add(numero, BorderLayout.WEST);
            esq.add(titulo, BorderLayout.CENTER);
            JPanel dir = Ui.linha(duracao, Ui.espaco(12, 0), remover);
            dir.setBorder(BorderFactory.createEmptyBorder(0, px(12), 0, 0));
            linha.add(esq, BorderLayout.CENTER);
            linha.add(dir, BorderLayout.EAST);

            JPanel erros = new JPanel();
            erros.setOpaque(false);
            erros.setLayout(new BoxLayout(erros, BoxLayout.Y_AXIS));
            erros.setBorder(Ui.margem(0, 16 + 32 + 12, 0, 0));
            eT.setAlignmentX(LEFT_ALIGNMENT);
            eD.setAlignmentX(LEFT_ALIGNMENT);
            erros.add(eT);
            erros.add(eD);

            painel = Ui.coluna(linha, erros);
            painel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.atual().ruleSoft));
            painel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        }
    }

    private Linha adicionar(RascunhoFaixa r, boolean marcarSujo) {
        Linha l = new Linha(r == null ? null : r.existente);
        if (r != null) {
            l.titulo.setText(r.titulo);
            l.duracao.setText(r.duracao);
        }
        observarLinha(l);
        l.remover.addActionListener(e -> alternarRemocao(l));
        l.duracao.addActionListener(e -> {
            if (l == ultimaAtiva()) {
                Linha nova = adicionar(null, true);
                nova.titulo.requestFocusInWindow();
            } else {
                l.duracao.transferFocus();
            }
        });
        l.titulo.addActionListener(e -> l.duracao.requestFocusInWindow());
        linhas.add(l);
        lista.add(l.painel);
        renumerar();
        lista.revalidate();
        lista.repaint();
        if (marcarSujo) {
            sujo = true;
        }
        return l;
    }

    private Linha ultimaAtiva() {
        for (int i = linhas.size() - 1; i >= 0; i--) {
            if (!linhas.get(i).removida) {
                return linhas.get(i);
            }
        }
        return null;
    }

    private void alternarRemocao(Linha l) {
        l.removida = !l.removida;
        l.titulo.setEnabled(!l.removida);
        l.duracao.setEnabled(!l.removida);
        l.remover.setText(l.removida ? "Restaurar" : "Remover");
        l.eT.limpar();
        l.eD.limpar();
        sujo = true;
        renumerar();
        nav.status(l.removida ? "Faixa marcada para remoção. Ela só sai ao salvar." : "Faixa restaurada.");
        l.remover.revalidate();
        l.remover.repaint();
    }

    private void renumerar() {
        int n = 0;
        for (Linha l : linhas) {
            l.numero.setText(l.removida ? "—" : String.valueOf(++n));
        }
        atualizarTotal();
    }

    private void atualizarTotal() {
        int faixas = 0;
        int segundos = 0;
        for (Linha l : linhas) {
            if (l.removida) {
                continue;
            }
            faixas++;
            try {
                segundos += Tempo.interpretar(l.duracao.getText());
            } catch (IllegalArgumentException ignorada) {
                // duração ainda incompleta: não entra na soma
            }
        }
        total.setText("Total: " + faixas + (faixas == 1 ? " faixa" : " faixas") + " · " + Tempo.formatar(segundos));
    }

    // ---------------------------------------------------------------- estado e validação

    private void observar(JTextComponent c, String chave) {
        c.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                alterou();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                alterou();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                alterou();
            }
        });
        c.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (!carregando && sujo) {
                    validar(false);
                }
            }
        });
    }

    private void observarLinha(Linha l) {
        observar(l.titulo, "faixa");
        observar(l.duracao, "faixa");
    }

    private void alterou() {
        if (!carregando) {
            sujo = true;
            atualizarTotal();
        }
    }

    private RascunhoAlbum coletar() {
        RascunhoAlbum r = new RascunhoAlbum();
        r.titulo = titulo.getText();
        r.artista = artista.getText();
        r.ano = ano.getText();
        r.genero = genero.getText();
        for (Linha l : linhas) {
            RascunhoFaixa f = new RascunhoFaixa();
            f.existente = l.existente;
            f.titulo = l.titulo.getText();
            f.duracao = l.duracao.getText();
            f.removida = l.removida;
            r.faixas.add(f);
        }
        return r;
    }

    /** Mostra os erros; devolve o mapa. {@code focar} leva o foco ao primeiro campo inválido. */
    private Map<String, String> validar(boolean focar) {
        Map<String, String> erros = ctx.catalogo.validar(coletar(), existente);
        limparErros();
        JComponent primeiro = null;
        for (Map.Entry<String, String> e : erros.entrySet()) {
            String k = e.getKey();
            String m = e.getValue();
            JComponent alvo = null;
            switch (k) {
                case "titulo" -> { eTitulo.mostrar(m); titulo.setErro(true); alvo = titulo; }
                case "artista" -> { eArtista.mostrar(m); artista.setErro(true); alvo = artista; }
                case "ano" -> { eAno.mostrar(m); ano.setErro(true); alvo = ano; }
                case "genero" -> { eGenero.mostrar(m); genero.setErro(true); alvo = genero; }
                case "faixas" -> eFaixas.mostrar(m);
                case "album" -> { eAlbum.mostrar(m); alvo = titulo; }
                default -> {
                    String[] p = k.split("\\.");
                    Linha l = linhas.get(Integer.parseInt(p[1]));
                    if (p[2].equals("titulo")) {
                        l.eT.mostrar(m);
                        l.titulo.setErro(true);
                        alvo = l.titulo;
                    } else {
                        l.eD.mostrar(m);
                        l.duracao.setErro(true);
                        alvo = l.duracao;
                    }
                }
            }
            if (primeiro == null && alvo != null) {
                primeiro = alvo;
            }
        }
        if (focar && primeiro != null) {
            primeiro.requestFocusInWindow();
        }
        revalidate();
        repaint();
        return erros;
    }

    private void limparErros() {
        for (MensagemErro m : new MensagemErro[] { eTitulo, eArtista, eAno, eGenero, eAlbum, eFaixas }) {
            m.limpar();
        }
        for (Campo c : new Campo[] { titulo, artista, ano, genero }) {
            c.setErro(false);
        }
        for (Linha l : linhas) {
            l.eT.limpar();
            l.eD.limpar();
            l.titulo.setErro(false);
            l.duracao.setErro(false);
        }
    }

    // ---------------------------------------------------------------- ações

    private void salvar() {
        Map<String, String> erros = validar(true);
        if (!erros.isEmpty()) {
            nav.status("Corrija " + erros.size() + (erros.size() == 1 ? " campo" : " campos") + " para salvar o álbum.");
            return;
        }
        try {
            Album salvo = ctx.catalogo.salvar(coletar(), existente);
            sujo = false;
            nav.status("Álbum “" + salvo.getTitulo() + "” salvo.");
            nav.irParaCatalogo();
        } catch (ValidacaoException ex) {
            nav.status("Não foi possível salvar: " + ex.getMessage());
        }
    }

    private void excluir() {
        String msg = "Serão excluídas as " + existente.getTotalFaixas() + " faixas, as " + ctx.catalogo.quantidadeAvaliacoes(existente)
                + " avaliações dessas faixas e as " + ctx.catalogo.quantidadeEntradas(existente)
                + " entradas de diário deste álbum. Esta ação não pode ser desfeita.";
        if (Dialogos.confirmar(nav.janela(), "Excluir “" + existente.getTitulo() + "”?", msg, "Excluir álbum", "Cancelar")) {
            ctx.catalogo.excluir(existente);
            sujo = false;
            nav.status("Álbum “" + existente.getTitulo() + "” excluído.");
            nav.irParaCatalogo();
        }
    }

    @Override
    public boolean podeSair() {
        if (!sujo) {
            return true;
        }
        return Dialogos.confirmar(nav.janela(), "Descartar alterações?", "As alterações deste álbum ainda não foram salvas.",
                "Descartar", "Continuar editando");
    }

    @Override
    public void aoExibir() {
        titulo.requestFocusInWindow();
    }
}
