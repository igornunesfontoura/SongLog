package br.com.songlog.view;

import static br.com.songlog.view.Preferencias.px;

import br.com.songlog.model.Album;
import br.com.songlog.service.Contexto;
import br.com.songlog.model.NotaGeral;
import br.com.songlog.util.Formatos;
import br.com.songlog.view.Fontes.Estilo;
import br.com.songlog.view.componentes.Botao;
import br.com.songlog.view.componentes.Campo;
import br.com.songlog.view.componentes.Rolagem;
import br.com.songlog.view.componentes.Seletor;
import br.com.songlog.view.dialogo.Dialogos;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * W2: catálogo de álbuns (RF04). Busca enquanto digita, filtro de gênero, ordenação por
 * coluna, paginação e as ações por teclado: Enter abre, F2 edita, Delete exclui, Ctrl+N cria.
 */
public class TelaCatalogo extends JPanel implements Tela {

    private static final int POR_PAGINA = 25;
    private static final String[] COLUNAS = { "Título", "Artista", "Ano", "Gênero", "Faixas", "Duração", "Nota geral" };
    private static final double[] PROPORCAO = { 0.24, 0.19, 0.08, 0.14, 0.08, 0.11, 0.16 };
    private static final boolean[] NUMERICA = { false, false, true, false, true, true, true };

    private final Navegador nav;
    private final Contexto ctx;
    private final Campo busca = new Campo("Buscar por título, artista ou gênero").dica("Título, artista ou gênero");
    private final Seletor<String> genero = new Seletor<>("Filtrar por gênero");
    private final Seletor<String> ordenar = new Seletor<>("Ordenar por");
    private final JLabel contagem = Ui.texto("", Estilo.MONO, Tema.atual().mute, 13);
    private final JLabel resumo = Ui.texto("", Estilo.MONO, Tema.atual().ink, 13);
    private final JLabel paginaInfo = Ui.texto("", Estilo.SMALL, Tema.atual().ink);
    private final Botao anterior = Botao.secundario("Anterior");
    private final Botao proxima = Botao.secundario("Próxima");
    private final Modelo modelo = new Modelo();
    private final Tabela tabela = new Tabela(modelo);
    private final CardLayout cartas = new CardLayout();
    private final JPanel area = Ui.painel();
    private final JPanel vazio = Ui.painel();
    private final Map<Album, String> notasGerais = new HashMap<>();

    private List<Album> filtrados = new ArrayList<>();
    private int pagina;
    private int colunaOrdem;
    private boolean crescente = true;

    public TelaCatalogo(Navegador nav) {
        this.nav = nav;
        this.ctx = nav.contexto();
        Tema t = Tema.atual();
        setOpaque(true);
        setBackground(t.bg);
        setLayout(new BorderLayout());
        setBorder(Ui.margem(28, 40, 0, 40));

        add(cabecalho(t), BorderLayout.NORTH);
        area.setLayout(cartas);
        Rolagem rolagem = new Rolagem(tabela);
        rolagem.setOpaque(true);
        rolagem.getViewport().setOpaque(true);
        rolagem.getViewport().setBackground(t.bg);
        rolagem.setBackground(t.bg);
        rolagem.setColumnHeaderView(tabela.getTableHeader());
        area.add(rolagem, "tabela");
        montarVazio(t);
        area.add(vazio, "vazio");
        add(area, BorderLayout.CENTER);
        add(rodape(t), BorderLayout.SOUTH);

        configurarTabela(t);
        configurarFiltros();
        atalhos();
        atualizar();
    }

    // ---------------------------------------------------------------- montagem

    private JComponent cabecalho(Tema t) {
        JLabel titulo = Ui.texto("Catálogo", Estilo.H1, t.ink);
        contagem.setBorder(Ui.margem(4, 0, 0, 0));
        JPanel esquerda = Ui.coluna(titulo, contagem);
        Botao novo = Botao.primario("Novo álbum");
        novo.getAccessibleContext().setAccessibleDescription("Atalho Ctrl+N");
        novo.addActionListener(e -> nav.irParaAlbum(null));
        JPanel topo = new JPanel(new BorderLayout());
        topo.setOpaque(false);
        topo.add(esquerda, BorderLayout.WEST);
        topo.add(Ui.coluna(Ui.cola(), novo), BorderLayout.EAST);
        topo.setBorder(BorderFactory.createCompoundBorder(Ui.filete(t.ink, 0, 0, 2, 0), Ui.margem(0, 0, 16, 0)));

        genero.setPreferredSize(new Dimension(px(210), px(36)));
        ordenar.setPreferredSize(new Dimension(px(210), px(36)));
        busca.setPreferredSize(new Dimension(px(420), px(36)));
        JPanel filtros = Ui.linha(rotulado("Buscar", busca), Ui.espaco(24, 0), rotulado("Gênero", genero), Ui.espaco(24, 0),
                rotulado("Ordenar por", ordenar), Ui.cola());
        filtros.setBorder(Ui.margem(20, 0, 24, 0));

        JPanel c = Ui.coluna(topo, filtros);
        return c;
    }

    private JPanel rotulado(String rotulo, JComponent campo) {
        JLabel r = Ui.texto(rotulo, Estilo.BODY_BOLD, Tema.atual().ink, 13);
        r.setBorder(Ui.margem(0, 0, 6, 0));
        JPanel p = Ui.coluna(r, campo);
        p.setMaximumSize(new Dimension(campo.getPreferredSize().width, p.getPreferredSize().height));
        return p;
    }

    private JComponent rodape(Tema t) {
        JPanel r = new JPanel(new BorderLayout());
        r.setOpaque(false);
        r.setBorder(Ui.margem(20, 0, 20, 0));
        r.add(resumo, BorderLayout.WEST);
        r.add(Ui.linha(anterior, Ui.espaco(16, 0), paginaInfo, Ui.espaco(16, 0), proxima), BorderLayout.EAST);
        anterior.addActionListener(e -> mudarPagina(-1));
        proxima.addActionListener(e -> mudarPagina(1));
        return r;
    }

    private void montarVazio(Tema t) {
        vazio.setLayout(new BorderLayout());
        vazio.setOpaque(true);
        vazio.setBackground(t.bg);
        vazio.setBorder(Ui.margem(48, 0, 0, 0));
    }

    private void configurarFiltros() {
        genero.addItem("Todos os gêneros");
        for (String g : ctx.catalogo.generos()) {
            genero.addItem(g);
        }
        for (String c : COLUNAS) {
            ordenar.addItem(c);
        }
        genero.addActionListener(e -> {
            pagina = 0;
            atualizar();
        });
        ordenar.addActionListener(e -> {
            colunaOrdem = ordenar.getSelectedIndex();
            crescente = colunaOrdem != 2 && colunaOrdem != 6; // ano e nota geral: mais altos primeiro
            pagina = 0;
            atualizar();
        });
        Timer debounce = new Timer(250, e -> {
            pagina = 0;
            atualizar();
        });
        debounce.setRepeats(false);
        busca.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                debounce.restart();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                debounce.restart();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                debounce.restart();
            }
        });
    }

    private void configurarTabela(Tema t) {
        tabela.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = tabela.columnAtPoint(e.getPoint());
                if (col < 0) {
                    return;
                }
                if (col == colunaOrdem) {
                    crescente = !crescente;
                } else {
                    colunaOrdem = col;
                    crescente = true;
                }
                ordenar.setSelectedIndex(col);
                atualizar();
            }
        });
        tabela.getSelectionModel().addListSelectionListener(e -> {
            Album a = selecionado();
            if (a != null && !e.getValueIsAdjusting()) {
                nav.status("“" + a.getTitulo() + "” selecionado. Enter para avaliar, F2 para editar, Delete para excluir.");
            }
        });
        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabela.rowAtPoint(e.getPoint()) >= 0) {
                    abrir();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                selecionarSob(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                selecionarSob(e);
            }

            private void selecionarSob(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int r = tabela.rowAtPoint(e.getPoint());
                    if (r >= 0) {
                        tabela.setRowSelectionInterval(r, r);
                    }
                }
            }
        });
        tabela.setComponentPopupMenu(menuContexto(t));

        acao(tabela, JComponent.WHEN_FOCUSED, "ENTER", "abrir", this::abrir);
        acao(tabela, JComponent.WHEN_FOCUSED, "F2", "editar", this::editar);
        acao(tabela, JComponent.WHEN_FOCUSED, "DELETE", "excluir", this::excluir);
        acao(tabela, JComponent.WHEN_FOCUSED, "TAB", "proximo", () -> KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent());
        acao(tabela, JComponent.WHEN_FOCUSED, "shift TAB", "anterior", () -> KeyboardFocusManager.getCurrentKeyboardFocusManager().focusPreviousComponent());
    }

    private JPopupMenu menuContexto(Tema t) {
        JPopupMenu m = new JPopupMenu();
        m.setBorder(BorderFactory.createLineBorder(t.ink, 1));
        m.setBackground(t.bg);
        for (Object[] item : new Object[][] { { "Avaliar", (Runnable) this::abrir }, { "Editar", (Runnable) this::editar }, { "Excluir", (Runnable) this::excluir } }) {
            JMenuItem mi = new JMenuItem((String) item[0]);
            mi.setFont(Estilo.BODY.fonte());
            mi.setBackground(t.bg);
            mi.setForeground(t.ink);
            mi.setBorder(Ui.margem(8, 16, 8, 16));
            Runnable r = (Runnable) item[1];
            mi.addActionListener(e -> r.run());
            m.add(mi);
        }
        return m;
    }

    private void atalhos() {
        acao(this, JComponent.WHEN_IN_FOCUSED_WINDOW, "ctrl N", "novo", () -> nav.irParaAlbum(null));
        acao(this, JComponent.WHEN_IN_FOCUSED_WINDOW, "ctrl F", "buscar", () -> {
            busca.requestFocusInWindow();
            busca.selectAll();
        });
    }

    static void acao(JComponent c, int quando, String tecla, String nome, Runnable r) {
        c.getInputMap(quando).put(KeyStroke.getKeyStroke(tecla), nome);
        c.getActionMap().put(nome, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                r.run();
            }
        });
    }

    // ---------------------------------------------------------------- dados

    private void atualizar() {
        String g = genero.getSelectedIndex() <= 0 ? "" : String.valueOf(genero.getSelectedItem());
        filtrados = new ArrayList<>(ctx.catalogo.buscar(busca.getText(), g));
        notasGerais.clear();
        Map<Integer, NotaGeral> geral = ctx.albuns.notasGerais(); // uma consulta à view vw_nota_geral_album
        for (Album a : filtrados) {
            NotaGeral n = geral.get(a.getId());
            notasGerais.put(a, n == null ? "—" : Formatos.nota2(n.valor()));
        }
        filtrados.sort(comparador());
        int total = ctx.catalogo.todos().size();
        contagem.setText(filtrados.size() == total ? total + (total == 1 ? " álbum" : " álbuns") : filtrados.size() + " de " + total + " álbuns");
        int paginas = Math.max(1, (int) Math.ceil(filtrados.size() / (double) POR_PAGINA));
        pagina = Math.max(0, Math.min(pagina, paginas - 1));
        int de = pagina * POR_PAGINA;
        int ate = Math.min(filtrados.size(), de + POR_PAGINA);
        modelo.definir(filtrados.subList(de, ate));
        resumo.setText(filtrados.isEmpty() ? "0 álbuns" : (de + 1) + "–" + ate + " de " + filtrados.size());
        paginaInfo.setText("Página " + (pagina + 1) + " de " + paginas);
        anterior.setEnabled(pagina > 0);
        proxima.setEnabled(pagina < paginas - 1);
        configurarLarguras();
        tabela.getTableHeader().repaint();
        if (filtrados.isEmpty()) {
            mostrarVazio(total == 0);
        } else {
            cartas.show(area, "tabela");
            if (tabela.getRowCount() > 0 && tabela.getSelectedRow() < 0) {
                tabela.setRowSelectionInterval(0, 0);
            }
        }
    }

    private void configurarLarguras() {
        int largura = Math.max(600, tabela.getParent() != null ? tabela.getParent().getWidth() : 1200);
        for (int i = 0; i < COLUNAS.length; i++) {
            tabela.getColumnModel().getColumn(i).setPreferredWidth((int) (PROPORCAO[i] * largura));
        }
    }

    private void mostrarVazio(boolean catalogoVazio) {
        Tema t = Tema.atual();
        vazio.removeAll();
        JLabel l1 = Ui.texto(catalogoVazio ? "Nenhum álbum cadastrado ainda." : "Nenhum álbum encontrado para esta busca.", Estilo.BODY_BOLD, t.ink);
        JLabel l2 = Ui.texto(catalogoVazio ? "Cadastre o primeiro álbum para começar a avaliar faixa por faixa."
                : "Tente outro termo ou limpe os filtros.", Estilo.BODY, t.ink2);
        Botao b = Botao.secundario(catalogoVazio ? "Cadastrar álbum" : "Limpar filtros");
        b.addActionListener(e -> {
            if (catalogoVazio) {
                nav.irParaAlbum(null);
            } else {
                busca.setText("");
                genero.setSelectedIndex(0);
                atualizar();
            }
        });
        JPanel col = Ui.coluna(l1, Ui.espaco(0, 4), l2, Ui.espaco(0, 20), b);
        vazio.add(col, BorderLayout.NORTH);
        cartas.show(area, "vazio");
        vazio.revalidate();
        vazio.repaint();
    }

    private Comparator<Album> comparador() {
        Comparator<Album> c = switch (colunaOrdem) {
            case 0 -> Comparator.comparing(a -> Formatos.semAcentos(a.getTitulo()));
            case 1 -> Comparator.comparing(a -> Formatos.semAcentos(a.getArtista()));
            case 2 -> Comparator.comparingInt(Album::getAnoLancamento);
            case 3 -> Comparator.comparing(a -> Formatos.semAcentos(a.getGenero() == null ? "" : a.getGenero()));
            case 4 -> Comparator.comparingInt(Album::getTotalFaixas);
            case 5 -> Comparator.comparingInt(Album::getDuracaoTotalSegundos);
            default -> Comparator.comparingDouble(a -> {
                String s = notasGerais.getOrDefault(a, "—");
                return "—".equals(s) ? -1 : Double.parseDouble(s.replace(',', '.'));
            });
        };
        return crescente ? c : c.reversed();
    }

    private void mudarPagina(int delta) {
        pagina += delta;
        atualizar();
    }

    private Album selecionado() {
        int r = tabela.getSelectedRow();
        return r < 0 ? null : modelo.album(r);
    }

    // ---------------------------------------------------------------- ações

    private void abrir() {
        Album a = selecionado();
        if (a != null) {
            nav.irParaAvaliacao(a);
        }
    }

    private void editar() {
        Album a = selecionado();
        if (a != null) {
            nav.irParaAlbum(a);
        }
    }

    private void excluir() {
        Album a = selecionado();
        if (a == null) {
            return;
        }
        String msg = "Serão excluídas as " + a.getTotalFaixas() + " faixas, as " + ctx.catalogo.quantidadeAvaliacoes(a)
                + " avaliações dessas faixas e as " + ctx.catalogo.quantidadeEntradas(a)
                + " entradas de diário deste álbum. Esta ação não pode ser desfeita.";
        if (Dialogos.confirmar(nav.janela(), "Excluir “" + a.getTitulo() + "”?", msg, "Excluir álbum", "Cancelar")) {
            ctx.catalogo.excluir(a);
            nav.status("Álbum “" + a.getTitulo() + "” excluído.");
            atualizar();
            tabela.requestFocusInWindow();
        }
    }

    @Override
    public void aoExibir() {
        if (tabela.getRowCount() > 0) {
            tabela.requestFocusInWindow();
        } else {
            busca.requestFocusInWindow();
        }
    }

    // ---------------------------------------------------------------- tabela

    private final class Modelo extends AbstractTableModel {
        private List<Album> linhas = new ArrayList<>();

        void definir(List<Album> novas) {
            linhas = new ArrayList<>(novas);
            fireTableDataChanged();
        }

        Album album(int i) {
            return linhas.get(i);
        }

        @Override
        public int getRowCount() {
            return linhas.size();
        }

        @Override
        public int getColumnCount() {
            return COLUNAS.length;
        }

        @Override
        public String getColumnName(int c) {
            return COLUNAS[c];
        }

        @Override
        public Object getValueAt(int r, int c) {
            Album a = linhas.get(r);
            return switch (c) {
                case 0 -> a.getTitulo();
                case 1 -> a.getArtista();
                case 2 -> String.valueOf(a.getAnoLancamento());
                case 3 -> a.getGenero() == null ? "" : a.getGenero();
                case 4 -> String.valueOf(a.getTotalFaixas());
                case 5 -> a.getDuracaoTotalFormatada();
                default -> notasGerais.getOrDefault(a, "—");
            };
        }
    }

    private final class Tabela extends JTable {
        Tabela(AbstractTableModel m) {
            super(m);
            Tema t = Tema.atual();
            setRowHeight(px(44));
            setShowGrid(false);
            setIntercellSpacing(new Dimension(0, 0));
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            setBackground(t.bg);
            setForeground(t.ink);
            setSelectionBackground(t.surface);
            setSelectionForeground(t.ink);
            setFillsViewportHeight(false);
            setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS);
            setDefaultRenderer(Object.class, new Celula());
            getTableHeader().setDefaultRenderer(new Cabecalho());
            getTableHeader().setReorderingAllowed(false);
            getTableHeader().setPreferredSize(new Dimension(1, px(36)));
            getAccessibleContext().setAccessibleName("Tabela de álbuns");
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            int r = getSelectedRow();
            if (r >= 0 && isFocusOwner()) {
                Tema t = Tema.atual();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(t.ink);
                java.awt.Rectangle rc = getCellRect(r, 0, true);
                Ui.borda(g2, 0, rc.y, getWidth(), rc.height, t.foco);
                g2.dispose();
            }
        }
    }

    private final class Celula extends DefaultTableCellRenderer {
        private boolean barra;

        @Override
        public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel, boolean foco, int r, int c) {
            super.getTableCellRendererComponent(tb, v, false, false, r, c);
            Tema t = Tema.atual();
            setOpaque(true);
            setBackground(sel ? t.surface : t.bg);
            boolean semNota = "—".equals(v);
            setForeground(c == 6 && !semNota ? t.accent : t.ink);
            setFont(c == 0 ? Estilo.BODY_BOLD.fonte() : NUMERICA[c] ? Estilo.MONO.fonte() : Estilo.BODY.fonte());
            setHorizontalAlignment(NUMERICA[c] ? RIGHT : LEFT);
            int esq = c == 0 ? px(16) : c == 3 ? px(32) : px(12);
            setBorder(BorderFactory.createCompoundBorder(Ui.filete(t.ruleSoft, 0, 0, 1, 0), Ui.margem(0, 0, 0, 0)));
            setBorder(BorderFactory.createCompoundBorder(getBorder(), BorderFactory.createEmptyBorder(0, esq, 0, px(12))));
            barra = sel && c == 0;
            return this;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            Ui.suavizar(g);
            super.paintComponent(g);
            if (barra) {
                g.setColor(Tema.atual().ink);
                g.fillRect(0, 0, px(4), getHeight());
            }
            g.dispose();
        }
    }

    private final class Cabecalho extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tb, Object v, boolean sel, boolean foco, int r, int c) {
            Tema t = Tema.atual();
            String seta = c == colunaOrdem ? (crescente ? "  ▲" : "  ▼") : "";
            super.getTableCellRendererComponent(tb, Estilo.LABEL.texto(String.valueOf(v)) + seta, false, false, r, c);
            setOpaque(true);
            setBackground(t.surface);
            setForeground(t.ink2);
            setFont(Estilo.LABEL.fonte());
            setHorizontalAlignment(NUMERICA[c] ? RIGHT : LEFT);
            int esq = c == 0 ? px(16) : c == 3 ? px(32) : px(12);
            setBorder(BorderFactory.createCompoundBorder(Ui.filete(t.ink, 0, 0, 2, 0), BorderFactory.createEmptyBorder(0, esq, 0, px(12))));
            return this;
        }
    }
}
