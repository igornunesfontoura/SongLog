package br.com.songlog.view.dialogo;

import static br.com.songlog.view.Preferencias.px;

import br.com.songlog.model.Album;
import br.com.songlog.model.EntradaDiario;
import br.com.songlog.service.Contexto;
import br.com.songlog.service.ValidacaoException;
import br.com.songlog.util.Formatos;
import br.com.songlog.view.Fontes.Estilo;
import br.com.songlog.view.Preferencias;
import br.com.songlog.view.Tema;
import br.com.songlog.view.Ui;
import br.com.songlog.view.componentes.Botao;
import br.com.songlog.view.componentes.Campo;
import br.com.songlog.view.componentes.MensagemErro;
import br.com.songlog.view.componentes.Seletor;
import java.awt.Window;
import java.time.LocalDate;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/** Diálogos usados pelas telas. Nenhum contém regra de negócio: chamam os serviços. */
public final class Dialogos {

    private Dialogos() {
    }

    /** Corpo de um diálogo de confirmação (público para os testes de captura). */
    public static JComponent corpoDeConfirmacao(String mensagem) {
        return paragrafo(mensagem, 472);
    }

    /** Texto que quebra linha na largura dada (JTextArea: o rótulo HTML do Swing ignora a fonte do projeto). */
    private static JTextArea paragrafo(String texto, int larguraPx) {
        JTextArea a = new JTextArea(texto);
        a.setEditable(false);
        a.setFocusable(false);
        a.setOpaque(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBorder(null);
        a.setFont(Estilo.BODY.fonte());
        a.setForeground(Tema.atual().ink);
        a.setSize(new java.awt.Dimension(px(larguraPx), 1));
        a.setPreferredSize(new java.awt.Dimension(px(larguraPx), a.getPreferredSize().height));
        return a;
    }

    private static JPanel grupo(String rotulo, JComponent campo, MensagemErro erro) {
        JLabel r = Ui.texto(rotulo, Estilo.BODY_BOLD, Tema.atual().ink, 13);
        r.setBorder(Ui.margem(0, 0, 6, 0));
        JPanel g = Ui.coluna(r, campo, erro);
        g.setBorder(Ui.margem(0, 0, 14, 0));
        return g;
    }

    /**
     * Pergunta de confirmação. O foco inicial fica em "cancelar" para que Enter não destrua nada.
     */
    public static boolean confirmar(Window dono, String titulo, String mensagem, String rotuloConfirmar, String rotuloCancelar) {
        Botao cancelar = Botao.secundario(rotuloCancelar);
        Botao ok = Botao.primario(rotuloConfirmar);
        DialogoBase d = new DialogoBase(dono, titulo, paragrafo(mensagem, 472), cancelar, cancelar, ok);
        cancelar.addActionListener(e -> d.fechar(false));
        ok.addActionListener(e -> d.fechar(true));
        return d.mostrar();
    }

    /** RF07: registra uma audição. Devolve {@code true} se registrou. */
    public static boolean registrarAudicao(Window dono, Contexto ctx, Album inicial) {
        Seletor<Album> album = new Seletor<Album>("Álbum").dica("Escolha o álbum");
        for (Album a : ctx.albuns.todos()) {
            album.addItem(a);
        }
        album.setSelectedItem(inicial);
        Campo data = new Campo("Data da audição");
        data.setText(Formatos.data(LocalDate.now()));
        Campo comentario = new Campo("Comentário").dica("Opcional, até 500 caracteres");
        MensagemErro eAlbum = new MensagemErro();
        MensagemErro eData = new MensagemErro();
        MensagemErro eComentario = new MensagemErro();
        JPanel corpo = Ui.coluna(grupo("Álbum *", album, eAlbum), grupo("Data *", data, eData),
                grupo("Comentário (opcional)", comentario, eComentario));

        Botao cancelar = Botao.secundario("Cancelar");
        Botao registrar = Botao.primario("Registrar");
        DialogoBase d = new DialogoBase(dono, "Registrar audição", corpo, inicial == null ? album : data, cancelar, registrar);
        cancelar.addActionListener(e -> d.fechar(false));
        Runnable enviar = () -> {
            eAlbum.limpar();
            eData.limpar();
            eComentario.limpar();
            data.setErro(false);
            comentario.setErro(false);
            try {
                ctx.perfil.registrarAudicao(ctx.usuario(), (Album) album.getSelectedItem(), data.getText(), comentario.getText());
                d.fechar(true);
            } catch (ValidacaoException ex) {
                switch (String.valueOf(ex.getCampo())) {
                    case "album" -> eAlbum.mostrar(ex.getMessage());
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
                d.pack();
            }
        };
        registrar.addActionListener(e -> enviar.run());
        data.addActionListener(e -> enviar.run());
        comentario.addActionListener(e -> enviar.run());
        return d.mostrar();
    }

    /** RF10: edita data e comentário de uma entrada do diário. */
    public static boolean editarEntrada(Window dono, Contexto ctx, EntradaDiario entrada) {
        JLabel titulo = Ui.texto(entrada.getAlbum().toString(), Estilo.BODY_BOLD, Tema.atual().ink);
        titulo.setBorder(Ui.margem(0, 0, 14, 0));
        Campo data = new Campo("Data da audição");
        data.setText(Formatos.data(entrada.getDataAudicao()));
        Campo comentario = new Campo("Comentário");
        comentario.setText(entrada.getComentario() == null ? "" : entrada.getComentario());
        MensagemErro eData = new MensagemErro();
        MensagemErro eComentario = new MensagemErro();
        JPanel corpo = Ui.coluna(titulo, grupo("Data *", data, eData), grupo("Comentário (opcional)", comentario, eComentario));

        Botao cancelar = Botao.secundario("Cancelar");
        Botao salvar = Botao.primario("Salvar");
        DialogoBase d = new DialogoBase(dono, "Editar audição", corpo, data, cancelar, salvar);
        cancelar.addActionListener(e -> d.fechar(false));
        Runnable enviar = () -> {
            eData.limpar();
            eComentario.limpar();
            data.setErro(false);
            comentario.setErro(false);
            try {
                ctx.perfil.editarEntrada(entrada, data.getText(), comentario.getText());
                d.fechar(true);
            } catch (ValidacaoException ex) {
                if ("data".equals(ex.getCampo())) {
                    eData.mostrar(ex.getMessage());
                    data.setErro(true);
                    data.requestFocusInWindow();
                } else {
                    eComentario.mostrar(ex.getMessage());
                    comentario.setErro(true);
                }
                d.pack();
            }
        };
        salvar.addActionListener(e -> enviar.run());
        data.addActionListener(e -> enviar.run());
        comentario.addActionListener(e -> enviar.run());
        return d.mostrar();
    }

    /** Tema e tamanho do texto. Devolve {@code true} se algo mudou (a janela se reconstrói). */
    public static boolean preferencias(Window dono) {
        Seletor<Tema> tema = new Seletor<Tema>("Tema");
        for (Tema t : Tema.values()) {
            tema.addItem(t);
        }
        tema.setSelectedItem(Tema.atual());
        Seletor<String> texto = new Seletor<String>("Tamanho do texto");
        String[] tamanhos = { "100%", "125%", "150%" };
        for (String s : tamanhos) {
            texto.addItem(s);
        }
        texto.setSelectedIndex(Preferencias.escala() >= 1.5 ? 2 : Preferencias.escala() >= 1.25 ? 1 : 0);
        JLabel nota = Ui.texto("O alto contraste usa preto sobre branco e anel de foco de 3 px.", Estilo.SMALL, Tema.atual().ink2);
        JPanel corpo = Ui.coluna(grupo("Tema", tema, new MensagemErro()), grupo("Tamanho do texto", texto, new MensagemErro()), nota);

        Botao cancelar = Botao.secundario("Cancelar");
        Botao aplicar = Botao.primario("Aplicar");
        DialogoBase d = new DialogoBase(dono, "Preferências de exibição", corpo, tema, cancelar, aplicar);
        cancelar.addActionListener(e -> d.fechar(false));
        aplicar.addActionListener(e -> d.fechar(true));
        if (!d.mostrar()) {
            return false;
        }
        Tema novoTema = (Tema) tema.getSelectedItem();
        double novaEscala = new double[] { 1.0, 1.25, 1.5 }[texto.getSelectedIndex()];
        boolean mudou = novoTema != Tema.atual() || novaEscala != Preferencias.escala();
        Tema.definir(novoTema);
        Preferencias.definirEscala(novaEscala);
        return mudou;
    }

    public static void atalhos(Window dono) {
        String[][] linhas = {
            { "Ctrl+1 / Ctrl+2", "Ir para Catálogo / Perfil e Diário" },
            { "Ctrl+N", "Novo álbum" },
            { "Ctrl+F", "Focar a busca do catálogo" },
            { "Ctrl+S", "Salvar o formulário do álbum" },
            { "Enter", "Abrir o álbum selecionado; confirmar; passar à próxima faixa" },
            { "F2", "Editar o álbum selecionado" },
            { "Delete", "Excluir o item selecionado (com confirmação)" },
            { "Esc", "Cancelar; desfazer a nota da faixa" },
            { "↑ ↓", "Ir para a faixa anterior / próxima" },
            { "← → ou + −", "Mudar a nota da faixa em 0,5" },
            { "4  ou  4,5", "Digitar a nota da faixa" },
            { "F1", "Esta lista" },
        };
        JPanel corpo = Ui.coluna();
        for (String[] l : linhas) {
            JLabel k = Ui.texto(l[0], Estilo.MONO, Tema.atual().ink, 13);
            k.setPreferredSize(new java.awt.Dimension(px(150), k.getPreferredSize().height));
            JLabel v = Ui.texto(l[1], Estilo.BODY, Tema.atual().ink, 13);
            JPanel linha = Ui.linha(k, v);
            linha.setBorder(Ui.margem(4, 0, 4, 0));
            corpo.add(linha);
        }
        Botao fechar = Botao.primario("Fechar");
        DialogoBase d = new DialogoBase(dono, "Atalhos de teclado", corpo, fechar, fechar);
        fechar.addActionListener(e -> d.fechar(true));
        d.mostrar();
    }
}
