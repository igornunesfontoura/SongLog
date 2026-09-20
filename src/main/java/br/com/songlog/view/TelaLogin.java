package br.com.songlog.view;

import static br.com.songlog.view.Preferencias.px;

import br.com.songlog.model.Usuario;
import br.com.songlog.repositorio.DadosIniciais;
import br.com.songlog.service.Contexto;
import br.com.songlog.service.ValidacaoException;
import br.com.songlog.view.Fontes.Estilo;
import br.com.songlog.view.componentes.Botao;
import br.com.songlog.view.componentes.Campo;
import br.com.songlog.view.componentes.CampoSenha;
import br.com.songlog.view.componentes.MensagemErro;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Graphics;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

/**
 * W1: entrar e criar conta (RF01). Duas colunas: mensagem do produto sobre fundo em tinta
 * e o formulário sobre o papel. O hash de senha (PBKDF2) roda fora da thread da interface.
 */
public class TelaLogin extends JPanel implements Tela {

    private final Navegador nav;
    private final Contexto ctx;
    private boolean cadastro;
    // largura fixa de 360 px; a altura acompanha o conteúdo (o modo cadastro tem mais campos)
    private final JPanel formulario = new JPanel() {
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(px(360), super.getPreferredSize().height);
        }
    };

    private Campo nome;
    private Campo email;
    private CampoSenha senha;
    private CampoSenha confirmacao;
    private MensagemErro erroGeral;
    private MensagemErro erroNome;
    private MensagemErro erroEmail;
    private MensagemErro erroSenha;
    private MensagemErro erroConfirmacao;
    private Botao enviar;

    public TelaLogin(Navegador nav) {
        this.nav = nav;
        this.ctx = nav.contexto();
        Tema t = Tema.atual();
        setLayout(new GridBagLayout());
        setBackground(t.bg);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.gridy = 0;
        c.gridx = 0;
        c.weightx = 0.5625;
        add(painelEsquerdo(t), c);
        c.gridx = 1;
        c.weightx = 0.4375;
        add(painelDireito(t), c);
        montarFormulario();
    }

    private JComponent painelEsquerdo(Tema t) {
        JPanel p = new JPanel();
        p.setBackground(t.ink);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(Ui.margem(64, 64, 64, 64));

        JLabel marca = new JLabel("SONGLOG");
        Map<TextAttribute, Object> m = new HashMap<>();
        m.put(TextAttribute.TRACKING, 0.08f);
        marca.setFont(Estilo.H2.fonte(28).deriveFont(m));
        marca.setForeground(t.bg);

        JLabel l1 = Ui.texto("O disco é a", Estilo.H1, t.bg, 64);
        JLabel l2 = Ui.texto("soma das faixas.", Estilo.H1, t.bg, 64);
        JLabel s1 = Ui.texto("Avalie faixa por faixa.", Estilo.BODY, t.surface, 16);
        JLabel s2 = Ui.texto("Registre o que ouviu.", Estilo.BODY, t.surface, 16);
        JLabel versao = Ui.texto("v0.3.0", Estilo.MONO, t.surface, 13);

        for (JComponent j : new JComponent[] { marca, l1, l2, s1, s2, versao }) {
            j.setAlignmentX(LEFT_ALIGNMENT);
        }
        p.add(marca);
        p.add(Ui.cola());
        p.add(l1);
        p.add(l2);
        p.add(Ui.espaco(0, 24));
        p.add(s1);
        p.add(s2);
        p.add(Ui.cola());
        p.add(versao);
        return p;
    }

    private JComponent painelDireito(Tema t) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(t.bg);
        formulario.setOpaque(false);
        formulario.setLayout(new BoxLayout(formulario, BoxLayout.Y_AXIS));
        p.add(formulario);
        return p;
    }

    private JPanel grupo(String rotulo, JComponent campo, MensagemErro erro, Botao extra) {
        JLabel r = Ui.texto(rotulo, Estilo.BODY_BOLD, Tema.atual().ink, 13);
        JPanel cima = Ui.linha(r, Ui.cola());
        if (extra != null) {
            cima.add(extra);
        }
        cima.setBorder(Ui.margem(0, 0, 6, 0));
        JPanel g = Ui.coluna(cima, campo, erro);
        g.setBorder(Ui.margem(0, 0, 14, 0));
        return g;
    }

    private void montarFormulario() {
        Tema t = Tema.atual();
        formulario.removeAll();
        erroGeral = new MensagemErro();
        erroNome = new MensagemErro();
        erroEmail = new MensagemErro();
        erroSenha = new MensagemErro();
        erroConfirmacao = new MensagemErro();
        nome = new Campo("Nome");
        email = new Campo("E-mail");
        senha = new CampoSenha("Senha");
        confirmacao = new CampoSenha("Confirmar senha");
        if (!cadastro) {
            email.setText("");
        }

        JLabel titulo = Ui.texto(cadastro ? "Criar conta" : "Entrar", Estilo.H2, t.ink, 32);
        JPanel filete = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(Tema.atual().ink);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        filete.setPreferredSize(new Dimension(1, 2));
        filete.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));

        formulario.add(Ui.coluna(titulo));
        formulario.add(Ui.espaco(0, 8));
        formulario.add(Ui.coluna(filete));
        formulario.add(Ui.espaco(0, 20));
        formulario.add(Ui.coluna(erroGeral));
        if (cadastro) {
            formulario.add(Ui.coluna(grupo("Nome *", nome, erroNome, null)));
        }
        formulario.add(Ui.coluna(grupo("E-mail *", email, erroEmail, null)));
        Botao mostrar = Botao.link("Mostrar senha");
        mostrar.getAccessibleContext().setAccessibleName("Mostrar senha");
        mostrar.addActionListener(e -> {
            boolean visivel = senha.getEchoChar() != 0;
            senha.mostrar(visivel);
            confirmacao.mostrar(visivel);
            mostrar.setText(visivel ? "Ocultar senha" : "Mostrar senha");
        });
        formulario.add(Ui.coluna(grupo("Senha *", senha, erroSenha, mostrar)));
        if (cadastro) {
            JLabel dica = Ui.texto("Mínimo de " + br.com.songlog.service.AutenticacaoService.TAMANHO_MINIMO_SENHA + " caracteres.", Estilo.SMALL, t.mute);
            dica.setBorder(Ui.margem(0, 0, 10, 0));
            formulario.add(Ui.coluna(dica));
            formulario.add(Ui.coluna(grupo("Confirmar senha *", confirmacao, erroConfirmacao, null)));
        }

        enviar = Botao.primario(cadastro ? "Criar conta" : "Entrar");
        enviar.setPreferredSize(new Dimension(px(360), px(40)));
        enviar.larguraTotal();
        enviar.setMaximumSize(new Dimension(Integer.MAX_VALUE, px(40)));
        enviar.addActionListener(e -> enviar());
        formulario.add(Ui.coluna(enviar));
        formulario.add(Ui.espaco(0, 16));

        Botao alternar = Botao.link(cadastro ? "Já tenho conta" : "Criar conta");
        alternar.addActionListener(e -> {
            cadastro = !cadastro;
            montarFormulario();
            aoExibir();
        });
        JPanel rodape = Ui.linha(Ui.texto(cadastro ? "" : "Não tem conta? ", Estilo.SMALL, t.ink), alternar);
        rodape.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        formulario.add(Ui.coluna(rodape));
        if (!cadastro) {
            formulario.add(Ui.espaco(0, 20));
            JLabel demo = Ui.texto("Demonstração: ana@songlog.dev  ·  senha " + DadosIniciais.SENHA_DEMO, Estilo.SMALL, t.mute);
            formulario.add(Ui.coluna(demo));
        }

        for (javax.swing.JTextField f : new javax.swing.JTextField[] { nome, email, senha, confirmacao }) {
            f.addActionListener(e -> enviar());
        }
        formulario.revalidate();
        formulario.repaint();
    }

    private void limparErros() {
        for (MensagemErro m : new MensagemErro[] { erroGeral, erroNome, erroEmail, erroSenha, erroConfirmacao }) {
            m.limpar();
        }
        nome.setErro(false);
        email.setErro(false);
        senha.setErro(false);
        confirmacao.setErro(false);
    }

    private void enviar() {
        limparErros();
        enviar.setEnabled(false);
        nav.status(cadastro ? "Criando conta…" : "Entrando…");
        String n = nome.getText();
        String e = email.getText();
        String s = new String(senha.getPassword());
        String c = new String(confirmacao.getPassword());
        boolean criando = cadastro;
        new SwingWorker<Usuario, Void>() {
            @Override
            protected Usuario doInBackground() {
                return criando ? ctx.autenticacao.cadastrar(n, e, s, c) : ctx.autenticacao.entrar(e, s);
            }

            @Override
            protected void done() {
                enviar.setEnabled(true);
                try {
                    Usuario u = get();
                    ctx.entrar(u);
                    nav.status(criando ? "Conta criada. Bem-vinda(o), " + u.getNome() + "." : "");
                    nav.irParaCatalogo();
                } catch (java.util.concurrent.ExecutionException ex) {
                    nav.status("");
                    if (ex.getCause() instanceof ValidacaoException v) {
                        mostrarErro(v);
                    } else {
                        erroGeral.mostrar("não foi possível concluir. Tente novamente.");
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void mostrarErro(ValidacaoException v) {
        String campo = String.valueOf(v.getCampo());
        switch (campo) {
            case "nome" -> {
                erroNome.mostrar(v.getMessage());
                nome.setErro(true);
                nome.requestFocusInWindow();
            }
            case "email" -> {
                erroEmail.mostrar(v.getMessage());
                email.setErro(true);
                email.requestFocusInWindow();
            }
            case "senha" -> {
                erroSenha.mostrar(v.getMessage());
                senha.setErro(true);
                senha.requestFocusInWindow();
            }
            case "confirmacao" -> {
                erroConfirmacao.mostrar(v.getMessage());
                confirmacao.setErro(true);
                confirmacao.requestFocusInWindow();
            }
            default -> {
                erroGeral.mostrar(v.getMessage());
                senha.setText("");
                senha.requestFocusInWindow();
            }
        }
        formulario.revalidate();
    }

    @Override
    public void aoExibir() {
        (cadastro ? nome : email).requestFocusInWindow();
    }
}
