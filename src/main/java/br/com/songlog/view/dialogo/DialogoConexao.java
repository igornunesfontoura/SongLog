package br.com.songlog.view.dialogo;

import static br.com.songlog.view.Preferencias.px;

import br.com.songlog.banco.Configuracao;
import br.com.songlog.view.Fontes.Estilo;
import br.com.songlog.view.Tema;
import br.com.songlog.view.Ui;
import br.com.songlog.view.componentes.Botao;
import br.com.songlog.view.componentes.Campo;
import br.com.songlog.view.componentes.CampoSenha;
import br.com.songlog.view.componentes.MensagemErro;
import java.awt.Dimension;
import java.awt.Window;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * Pede os dados de conexão com o MySQL quando não foi possível conectar (primeira execução ou senha
 * diferente). Mostra o motivo do erro, como no banner de falha de conexão do projeto de interfaces.
 */
public final class DialogoConexao {

    private DialogoConexao() {
    }

    /** Só o desenho do diálogo (sem abrir janela): usado pelos testes de captura de tela. */
    public static JPanel visualParaTeste(Configuracao atual, String motivo) {
        return DialogoBase.montar("Conexão com o MySQL", new Formulario(atual, motivo).corpo, Botao.secundario("Sair"), Botao.primario("Conectar"));
    }

    /** Devolve a configuração digitada, ou {@code null} se o usuário escolheu sair. */
    public static Configuracao pedir(Window dono, Configuracao atual, String motivo) {
        Formulario f = new Formulario(atual, motivo);
        Botao sair = Botao.secundario("Sair");
        Botao conectar = Botao.primario("Conectar");
        Configuracao[] resultado = new Configuracao[1];
        DialogoBase d = new DialogoBase(dono, "Conexão com o MySQL", f.corpo, f.senha, sair, conectar);
        sair.addActionListener(e -> d.fechar(false));
        Runnable enviar = () -> {
            Configuracao c = f.ler();
            if (c == null) {
                d.pack();
                return;
            }
            resultado[0] = c;
            d.fechar(true);
        };
        conectar.addActionListener(e -> enviar.run());
        f.senha.addActionListener(e -> enviar.run());
        return d.mostrar() ? resultado[0] : null;
    }

    /** Campos e mensagens do diálogo. */
    private static final class Formulario {
        final Configuracao atual;
        final Campo host = new Campo("Servidor do MySQL");
        final Campo porta = new Campo("Porta");
        final Campo usuario = new Campo("Usuário do MySQL");
        final CampoSenha senha = new CampoSenha("Senha do MySQL");
        final MensagemErro erroPorta = new MensagemErro();
        final JPanel corpo;

        Formulario(Configuracao atual, String motivo) {
            this.atual = atual;
            Tema t = Tema.atual();
            JTextArea aviso = new JTextArea("[!]  " + motivo);
            aviso.setEditable(false);
            aviso.setFocusable(false);
            aviso.setLineWrap(true);
            aviso.setWrapStyleWord(true);
            aviso.setFont(Estilo.BODY.fonte());
            aviso.setForeground(t.ink);
            aviso.setBackground(t.surface);
            aviso.setBorder(javax.swing.BorderFactory.createCompoundBorder(Ui.filete(t.ink, 0, 0, 2, 0), Ui.margem(12, 16, 12, 16)));
            aviso.setSize(new Dimension(px(480), Short.MAX_VALUE));
            aviso.setPreferredSize(new Dimension(px(480), aviso.getPreferredSize().height));

            host.setText(atual.host());
            porta.setText(String.valueOf(atual.porta()));
            usuario.setText(atual.usuario());
            senha.setText(atual.senha());
            porta.setMaximumSize(new Dimension(px(100), px(36)));
            JPanel linhaHost = Ui.linha(grupo("Servidor", host, null), Ui.espaco(16, 0), grupo("Porta", porta, erroPorta));
            JTextArea nota = texto("O SongLog cria o banco “" + atual.nome() + "”, as tabelas e os dados de teste sozinho, se este usuário tiver permissão. "
                    + "A conexão fica salva em config/songlog.local.properties.", t);
            corpo = Ui.coluna(aviso, Ui.espaco(0, 16), linhaHost, grupo("Usuário", usuario, null), grupo("Senha", senha, null), nota);
        }

        /** Lê os campos; devolve {@code null} (com o erro na tela) se a porta for inválida. */
        Configuracao ler() {
            erroPorta.limpar();
            int p;
            try {
                p = Integer.parseInt(porta.getText().strip());
                if (p < 1 || p > 65535) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                erroPorta.mostrar("use um número de 1 a 65535.");
                porta.setErro(true);
                return null;
            }
            return atual.com(host.getText().strip(), p, usuario.getText().strip(), new String(senha.getPassword()));
        }
    }

    private static JTextArea texto(String s, Tema t) {
        JTextArea a = new JTextArea(s);
        a.setEditable(false);
        a.setFocusable(false);
        a.setOpaque(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setFont(Estilo.SMALL.fonte());
        a.setForeground(t.ink2);
        a.setBorder(null);
        a.setSize(new Dimension(px(480), Short.MAX_VALUE));
        a.setPreferredSize(new Dimension(px(480), a.getPreferredSize().height));
        return a;
    }

    private static JPanel grupo(String rotulo, JComponent campo, MensagemErro erro) {
        JLabel r = Ui.texto(rotulo, Estilo.BODY_BOLD, Tema.atual().ink, 13);
        r.setBorder(Ui.margem(0, 0, 6, 0));
        JPanel g = erro == null ? Ui.coluna(r, campo) : Ui.coluna(r, campo, erro);
        g.setBorder(Ui.margem(0, 0, 12, 0));
        return g;
    }
}
