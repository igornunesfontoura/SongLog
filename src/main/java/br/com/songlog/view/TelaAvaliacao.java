package br.com.songlog.view;

import static br.com.songlog.view.Preferencias.px;

import br.com.songlog.model.Album;
import br.com.songlog.model.Avaliacao;
import br.com.songlog.model.Faixa;
import br.com.songlog.model.Usuario;
import br.com.songlog.service.Contexto;
import br.com.songlog.service.NotaService;
import br.com.songlog.service.NotaService.NotaAlbum;
import br.com.songlog.service.NotaService.NotaGeral;
import br.com.songlog.util.Formatos;
import br.com.songlog.view.Fontes.Estilo;
import br.com.songlog.view.componentes.BarraDeNota;
import br.com.songlog.view.componentes.Botao;
import br.com.songlog.view.componentes.Campo;
import br.com.songlog.view.componentes.MensagemErro;
import br.com.songlog.view.componentes.Rolagem;
import br.com.songlog.view.componentes.Tag;
import br.com.songlog.view.dialogo.Dialogos;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * W4: avaliação do álbum faixa a faixa (RF05, RF06, RF10). Cada mudança de nota é salva na hora
 * e recalcula "Sua nota" (média ponderada pela duração, RN05) e a nota geral (RN07).
 */
public class TelaAvaliacao extends JPanel implements Tela {

    private static final int[] LARGURAS = { 40, 100, 96, 270, 64, 200 };

    /** Com texto grande a coluna Duração some (a duração fica na dica e no nome acessível) para o título não ser espremido. */
    private static boolean compacto() {
        return Preferencias.escala() >= 1.25;
    }

    private final Navegador nav;
    private final Contexto ctx;
    private final Album album;
    private final Usuario usuario;
    private final List<LinhaFaixa> linhas = new ArrayList<>();

    private final JLabel suaNota = Ui.texto("—", Estilo.DISPLAY, Tema.atual().accent);
    private final JPanel suaApoio = Ui.painel();
    private final JLabel notaGeral = Ui.texto("—", Estilo.DISPLAY, Tema.atual().accent);
    private final JLabel geralApoio = Ui.texto("", Estilo.SMALL, Tema.atual().ink);
    private final JPanel painelFormula = new Ui.Bloco();
    private final Botao alternar = Botao.link("Como é calculada? +");
    private boolean formulaAberta;

    public TelaAvaliacao(Navegador nav, Album album) {
        this.nav = nav;
        this.ctx = nav.contexto();
        this.album = album;
        this.usuario = ctx.usuario();
        Tema t = Tema.atual();
        setOpaque(true);
        setBackground(t.bg);
        setLayout(new BorderLayout());
        setBorder(Ui.margem(28, 40, 0, 40));

        // tudo acima do rodapé rola junto: em janela pequena ou com texto grande nada fica inacessível
        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setOpaque(false);
        envoltorio.add(Ui.coluna(topo(t), tabela(t)), BorderLayout.NORTH);
        add(new Rolagem(envoltorio), BorderLayout.CENTER);
        add(rodape(t), BorderLayout.SOUTH);
        recalcular();
        for (int i = 0; i < linhas.size(); i++) {
            ligarNavegacao(i);
        }
    }

    // ---------------------------------------------------------------- montagem

    private JComponent topo(Tema t) {
        Botao catalogo = Botao.link("Catálogo");
        catalogo.addActionListener(e -> nav.irParaCatalogo());
        JPanel crumb = Ui.linha(catalogo, Ui.texto(" / ", Estilo.SMALL, t.ink2), Ui.texto(album.getTitulo(), Estilo.SMALL, t.ink2), Ui.cola());

        JLabel h1 = Ui.texto(album.getTitulo(), Estilo.H1, t.ink);
        JLabel artista = Ui.texto(album.getArtista(), Estilo.H2, t.ink2);
        JLabel meta = Ui.texto(album.getAnoLancamento() + " · " + (album.getGenero() == null ? "sem gênero" : album.getGenero()) + " · "
                + album.getTotalFaixas() + (album.getTotalFaixas() == 1 ? " faixa · " : " faixas · ") + album.getDuracaoTotalFormatada(),
                Estilo.MONO, t.mute, 13);
        Botao editar = Botao.link("Editar álbum");
        editar.addActionListener(e -> nav.irParaAlbum(album));
        JPanel info = Ui.coluna(h1, artista, Ui.espaco(0, 6), meta, Ui.espaco(0, 4), editar);
        info.setBorder(Ui.margem(4, 0, 0, 0));

        // grade de 2 colunas: os dois blocos têm a mesma altura e os filetes verticais se alinham
        JPanel herois = new JPanel(new java.awt.GridLayout(1, 2));
        herois.setOpaque(false);
        herois.add(hero("Sua nota", suaNota, suaApoio, true));
        herois.add(hero("Nota geral", notaGeral, Ui.coluna(geralApoio), false));
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setOpaque(false);
        cabecalho.add(info, BorderLayout.WEST);
        cabecalho.add(herois, BorderLayout.EAST);

        alternar.getAccessibleContext().setAccessibleDescription("Recolhido");
        alternar.addActionListener(e -> alternarFormula());
        JPanel linhaAlternar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        linhaAlternar.setOpaque(false);
        linhaAlternar.setBorder(Ui.margem(6, 0, 12, 0));
        linhaAlternar.add(alternar);

        painelFormula.setLayout(new BoxLayout(painelFormula, BoxLayout.Y_AXIS));
        painelFormula.setOpaque(true);
        painelFormula.setBackground(t.surface);
        painelFormula.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(Ui.filete(t.rule, 1, 0, 0, 0), Ui.filete(t.ink, 0, 0, 2, 0)),
                Ui.margem(14, 16, 14, 16)));
        painelFormula.setVisible(false);

        JPanel c = Ui.coluna(crumb, Ui.espaco(0, 8), cabecalho, linhaAlternar, painelFormula);
        return c;
    }

    private JComponent hero(String rotulo, JLabel valor, JComponent apoio, boolean primeiro) {
        Tema t = Tema.atual();
        JPanel p = Ui.coluna(Ui.texto(rotulo, Estilo.LABEL, t.ink2), valor, apoio);
        p.setBorder(BorderFactory.createCompoundBorder(Ui.filete(t.ink, 0, 2, 0, 0), Ui.margem(0, 32, 0, primeiro ? 32 : 0)));
        valor.setAlignmentX(LEFT_ALIGNMENT);
        return p;
    }

    private JComponent tabela(Tema t) {
        JPanel cabecalho = linhaGrade(
                rotuloTabela("#", false), rotuloTabela("Faixa", false), rotuloTabela("Duração", true),
                rotuloTabela("Sua nota (0,5 a 5,0)", false), rotuloTabela("Valor", true), rotuloTabela("", false), 36);
        cabecalho.setOpaque(true);
        cabecalho.setBackground(t.surface);
        cabecalho.setBorder(BorderFactory.createCompoundBorder(Ui.filete(t.ink, 0, 0, 2, 0), Ui.margem(0, 16, 0, 12)));

        JPanel lista = Ui.painel();
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        for (Faixa f : album.getFaixas()) {
            LinhaFaixa l = new LinhaFaixa(f);
            linhas.add(l);
            lista.add(l.painel);
        }
        return Ui.coluna(cabecalho, lista);
    }

    private JLabel rotuloTabela(String s, boolean direita) {
        JLabel l = Ui.texto(s, Estilo.LABEL, Tema.atual().ink2);
        l.setHorizontalAlignment(direita ? JLabel.RIGHT : JLabel.LEFT);
        return l;
    }

    private JPanel linhaGrade(JComponent n, JComponent titulo, JComponent dur, JComponent barra, JComponent valor, JComponent acoes, int altura) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        JComponent[] cs = { n, titulo, dur, barra, valor, acoes };
        int[] ancoras = { GridBagConstraints.WEST, GridBagConstraints.WEST, GridBagConstraints.EAST,
            GridBagConstraints.WEST, GridBagConstraints.EAST, GridBagConstraints.WEST };
        for (int i = 0; i < cs.length; i++) {
            if (i == 2 && compacto()) {
                cs[i] = new JLabel(); // coluna oculta
            }
            JPanel celula = new JPanel(new GridBagLayout());
            celula.setOpaque(false);
            GridBagConstraints c = new GridBagConstraints();
            c.weightx = 1;
            c.anchor = ancoras[i];
            c.fill = i == 2 || i == 4 ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
            if (i == 2) {
                celula.setBorder(Ui.margem(0, 0, 0, 16));
            }
            if (i == 5) {
                celula.setBorder(Ui.margem(0, 12, 0, 0));
            }
            celula.add(cs[i], c);
            celula.setPreferredSize(new Dimension(i == 2 && compacto() ? 0 : px(LARGURAS[i]), px(altura)));
            GridBagConstraints g = new GridBagConstraints();
            g.gridx = i;
            g.weightx = i == 1 ? 1 : 0;
            g.fill = GridBagConstraints.BOTH;
            g.weighty = 1;
            p.add(celula, g);
        }
        return p;
    }

    private JComponent rodape(Tema t) {
        JPanel r = Ui.linha(Ui.texto("Ouviu hoje?", Estilo.BODY, t.ink), Ui.espaco(16, 0));
        Botao registrar = Botao.secundario("Registrar audição");
        registrar.addActionListener(e -> {
            if (Dialogos.registrarAudicao(nav.janela(), ctx, album)) {
                nav.status("Audição de “" + album.getTitulo() + "” registrada no diário.");
            }
        });
        r.add(registrar);
        r.add(Ui.cola());
        r.setBorder(BorderFactory.createCompoundBorder(Ui.filete(t.ink, 2, 0, 0, 0), Ui.margem(10, 0, 10, 0)));
        botaoRegistrar = registrar;
        return r;
    }

    private Botao botaoRegistrar;

    // ---------------------------------------------------------------- notas

    private void recalcular() {
        Tema t = Tema.atual();
        Optional<NotaAlbum> minha = NotaService.notaDoUsuario(album, usuario);
        suaNota.setText(minha.map(n -> Formatos.nota2(n.valor())).orElse("—"));
        suaNota.setForeground(minha.isPresent() ? t.accent : t.mute);
        suaApoio.removeAll();
        suaApoio.setLayout(new BoxLayout(suaApoio, BoxLayout.Y_AXIS));
        if (minha.isEmpty()) {
            suaApoio.add(Ui.coluna(Ui.texto("Nenhuma faixa avaliada", Estilo.SMALL, t.ink)));
        } else if (minha.get().completa()) {
            suaApoio.add(Ui.coluna(Ui.texto(minha.get().faixasAvaliadas() + " de " + minha.get().totalFaixas() + " faixas", Estilo.SMALL, t.ink)));
        } else {
            JPanel l = Ui.linha(new Tag("Parcial"), Ui.espaco(8, 0),
                    Ui.texto(minha.get().faixasAvaliadas() + " de " + minha.get().totalFaixas() + " faixas", Estilo.SMALL, t.ink), Ui.cola());
            l.setBorder(Ui.margem(4, 0, 6, 0));
            suaApoio.add(Ui.coluna(l));
            suaApoio.add(Ui.coluna(Ui.texto("Avalie as " + minha.get().totalFaixas() + " faixas para entrar na nota geral.", Estilo.SMALL, t.ink)));
        }
        Optional<NotaGeral> geral = NotaService.notaGeral(album, ctx.usuarios.todos());
        notaGeral.setText(geral.map(g -> Formatos.nota2(g.valor())).orElse("—"));
        notaGeral.setForeground(geral.isPresent() ? t.accent : t.mute);
        geralApoio.setText(geral.map(g -> g.totalAvaliadores() + (g.totalAvaliadores() == 1 ? " avaliador" : " avaliadores")).orElse("sem avaliações completas"));
        montarFormula(t, minha);
        suaApoio.revalidate();
        suaApoio.repaint();
    }

    private void montarFormula(Tema t, Optional<NotaAlbum> minha) {
        painelFormula.removeAll();
        painelFormula.add(Ui.coluna(Ui.texto("Como a nota é calculada", Estilo.LABEL, t.ink2)));
        painelFormula.add(Ui.espaco(0, 6));
        painelFormula.add(Ui.coluna(Ui.texto("Nota = Σ (nota × duração) ÷ Σ (duração). Faixas longas pesam mais.", Estilo.BODY, t.ink)));
        painelFormula.add(Ui.espaco(0, 6));
        if (minha.isEmpty()) {
            painelFormula.add(Ui.coluna(Ui.texto("Avalie ao menos uma faixa para ver a conta com os números deste álbum.", Estilo.SMALL, t.ink)));
        } else {
            StringBuilder termos = new StringBuilder();
            int peso = 0;
            double soma = 0;
            double simples = 0;
            int n = 0;
            for (Faixa f : album.getFaixas()) {
                Optional<Avaliacao> a = usuario.getAvaliacaoDe(f);
                if (a.isPresent()) {
                    termos.append(termos.length() == 0 ? "" : " + ").append(Formatos.nota1(a.get().getNota())).append("×").append(f.getDuracaoSegundos());
                    peso += f.getDuracaoSegundos();
                    soma += a.get().getNota() * f.getDuracaoSegundos();
                    simples += a.get().getNota();
                    n++;
                }
            }
            double ponderada = soma / peso;
            simples /= n;
            painelFormula.add(Ui.coluna(Ui.texto("(" + termos + ") ÷ " + peso + " s = " + Formatos.nota2(ponderada), Estilo.MONO, t.ink, 13)));
            painelFormula.add(Ui.espaco(0, 6));
            String cmp;
            if (Math.abs(ponderada - simples) < 0.005) {
                cmp = "Média simples: " + Formatos.nota2(simples) + ". Aqui as duas coincidem.";
            } else {
                cmp = "Média simples: " + Formatos.nota2(simples) + ". A ponderada é " + Formatos.nota2(ponderada)
                        + " porque as faixas mais longas tiveram notas " + (ponderada < simples ? "menores." : "maiores.");
            }
            painelFormula.add(Ui.coluna(Ui.texto(cmp, Estilo.SMALL, t.ink)));
        }
        painelFormula.revalidate();
        painelFormula.repaint();
    }

    private void alternarFormula() {
        formulaAberta = !formulaAberta;
        painelFormula.setVisible(formulaAberta);
        alternar.setText(formulaAberta ? "Ocultar cálculo −" : "Como é calculada? +");
        alternar.getAccessibleContext().setAccessibleDescription(formulaAberta ? "Expandido" : "Recolhido");
        revalidate();
        repaint();
    }

    private void ligarNavegacao(int i) {
        LinhaFaixa l = linhas.get(i);
        l.barra.setAoNavegar(delta -> {
            int alvo = i + delta;
            if (alvo >= 0 && alvo < linhas.size()) {
                linhas.get(alvo).barra.requestFocusInWindow();
                rolarPara(linhas.get(alvo));
            }
        });
        l.barra.setAoConfirmar(() -> {
            if (i + 1 < linhas.size()) {
                linhas.get(i + 1).barra.requestFocusInWindow();
                rolarPara(linhas.get(i + 1));
            } else {
                botaoRegistrar.requestFocusInWindow();
            }
        });
    }

    /** Mantém a faixa focada visível, com folga acima do rodapé fixo. */
    private void rolarPara(LinhaFaixa l) {
        SwingUtilities.invokeLater(() -> l.painel.scrollRectToVisible(new java.awt.Rectangle(0, -px(8), 10, l.painel.getHeight() + px(64))));
    }

    // ---------------------------------------------------------------- linha de faixa

    private final class LinhaFaixa {
        final Faixa faixa;
        final BarraDeNota barra;
        final JLabel valor = Ui.texto("—", Estilo.MONO_LG, Tema.atual().mute);
        { valor.setHorizontalAlignment(JLabel.RIGHT); }
        final Botao comentar = Botao.link("Comentar");
        final Botao limpar = Botao.link("Limpar");
        final Campo comentario = new Campo("Comentário da faixa").dica("Opcional, até 500 caracteres");
        final JLabel contador = Ui.texto("0/500", Estilo.MONO, Tema.atual().mute, 13);
        final MensagemErro erro = new MensagemErro();
        final JPanel painelComentario = new Ui.Bloco();
        final JPanel painel;

        LinhaFaixa(Faixa faixa) {
            this.faixa = faixa;
            Tema t = Tema.atual();
            double atual = usuario.getAvaliacaoDe(faixa).map(Avaliacao::getNota).orElse(0.0);
            barra = new BarraDeNota(atual, "Nota da faixa " + faixa.getNumero() + ", " + faixa.getTitulo() + ", de 0,5 a 5,0");
            barra.setOuvinte(n -> aoMudar(n));

            JLabel num = Ui.texto(String.valueOf(faixa.getNumero()), Estilo.MONO, t.ink);
            JLabel titulo = Ui.texto(faixa.getTitulo(), Estilo.BODY_BOLD, t.ink);
            titulo.setToolTipText(faixa.getTitulo() + " (" + faixa.getDuracaoFormatada() + ")"); // o título completo e a duração ficam aqui
            titulo.getAccessibleContext().setAccessibleName(faixa.getTitulo() + ", duração " + faixa.getDuracaoFormatada());
            JLabel dur = Ui.texto(faixa.getDuracaoFormatada(), Estilo.MONO, t.mute);
            dur.setHorizontalAlignment(JLabel.RIGHT);
            JPanel acoes = Ui.linha(comentar, Ui.espaco(12, 0), limpar);
            comentar.getAccessibleContext().setAccessibleName("Comentar a faixa " + faixa.getTitulo());
            limpar.getAccessibleContext().setAccessibleName("Limpar a nota da faixa " + faixa.getTitulo());
            comentar.addActionListener(e -> alternarComentario());
            limpar.addActionListener(e -> {
                barra.setNota(0);
                aoMudar(0);
            });

            JPanel principal = linhaGrade(num, titulo, dur, barra, valor, acoes, 44);
            principal.setBorder(Ui.margem(0, 16, 0, 12));

            painelComentario.setLayout(new BoxLayout(painelComentario, BoxLayout.Y_AXIS));
            painelComentario.setOpaque(true);
            painelComentario.setBackground(t.surface);
            painelComentario.setBorder(BorderFactory.createCompoundBorder(Ui.filete(t.ruleSoft, 0, 0, 1, 0), Ui.margem(10, 56, 14, 16)));
            JPanel campo = Ui.linha(comentario, Ui.espaco(16, 0), contador);
            comentario.setMaximumSize(new Dimension(px(560), px(36)));
            painelComentario.add(Ui.coluna(Ui.texto("Comentário (opcional)", Estilo.BODY_BOLD, t.ink, 13)));
            painelComentario.add(Ui.espaco(0, 6));
            painelComentario.add(Ui.coluna(campo));
            painelComentario.add(Ui.coluna(erro));
            painelComentario.setVisible(false);
            comentario.addActionListener(e -> salvarComentario());
            comentario.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    salvarComentario();
                }
            });
            comentario.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    contador.setText(comentario.getText().length() + "/500");
                }

                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    contador.setText(comentario.getText().length() + "/500");
                }

                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    contador.setText(comentario.getText().length() + "/500");
                }
            });

            painel = Ui.coluna(principal, painelComentario);
            painel.setBorder(Ui.filete(t.ruleSoft, 0, 0, 1, 0));
            atualizar();
        }

        void atualizar() {
            Tema t = Tema.atual();
            Optional<Avaliacao> a = usuario.getAvaliacaoDe(faixa);
            double n = a.map(Avaliacao::getNota).orElse(0.0);
            barra.setNota(n);
            valor.setText(n == 0 ? "—" : Formatos.nota1(n));
            valor.setForeground(n == 0 ? t.mute : t.accent);
            limpar.setVisible(n > 0);
            boolean temComentario = a.isPresent() && a.get().getComentario() != null;
            comentar.setText(temComentario ? "Editar comentário" : "Comentar");
            comentar.revalidate();
        }

        void aoMudar(double n) {
            if (n == 0) {
                usuario.removerAvaliacao(faixa);
                nav.status("Nota de “" + faixa.getTitulo() + "” removida.");
            } else {
                String c = usuario.getAvaliacaoDe(faixa).map(Avaliacao::getComentario).orElse(null);
                usuario.avaliar(faixa, n, c);
                nav.status("Nota de “" + faixa.getTitulo() + "” salva: " + Formatos.nota1(n) + ".");
            }
            atualizar();
            recalcular();
        }

        void alternarComentario() {
            boolean abrir = !painelComentario.isVisible();
            painelComentario.setVisible(abrir);
            if (abrir) {
                comentario.setText(usuario.getAvaliacaoDe(faixa).map(Avaliacao::getComentario).orElse(""));
                erro.limpar();
                comentario.requestFocusInWindow();
            }
            painel.revalidate();
            painel.repaint();
        }

        void salvarComentario() {
            if (!painelComentario.isVisible()) {
                return;
            }
            erro.limpar();
            comentario.setErro(false);
            Optional<Avaliacao> a = usuario.getAvaliacaoDe(faixa);
            String texto = comentario.getText();
            if (a.isEmpty()) {
                if (!texto.isBlank()) {
                    erro.mostrar("dê uma nota à faixa antes de comentar.");
                    comentario.setErro(true);
                }
                return;
            }
            try {
                usuario.avaliar(faixa, a.get().getNota(), texto);
                atualizar();
                nav.status("Comentário da faixa “" + faixa.getTitulo() + "” salvo.");
            } catch (IllegalArgumentException ex) {
                erro.mostrar("o comentário passa de 500 caracteres.");
                comentario.setErro(true);
            }
            painel.revalidate();
        }
    }

    @Override
    public void aoExibir() {
        if (!linhas.isEmpty()) {
            linhas.get(0).barra.requestFocusInWindow();
        }
    }
}
