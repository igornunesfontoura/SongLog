package br.com.songlog.view.componentes;

import br.com.songlog.view.Fontes.Estilo;
import br.com.songlog.view.Tema;
import br.com.songlog.view.Ui;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** "Erro: ..." abaixo do campo. O rótulo vai em acento, a mensagem em tinta: nunca só cor. */
public class MensagemErro extends JPanel {

    private final JLabel texto = Ui.texto("", Estilo.SMALL, Tema.atual().ink);

    public MensagemErro() {
        setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        setBorder(Ui.margem(6, 0, 0, 0));
        JLabel rotulo = Ui.texto("Erro:", Estilo.BODY_BOLD, Tema.atual().accent, 13);
        add(rotulo);
        add(javax.swing.Box.createHorizontalStrut(4));
        add(texto);
        setVisible(false);
    }

    public void mostrar(String mensagem) {
        texto.setText(mensagem);
        getAccessibleContext().setAccessibleName("Erro: " + mensagem);
        setVisible(true);
        revalidate();
    }

    public void limpar() {
        setVisible(false);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }
}
