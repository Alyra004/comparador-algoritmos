package com.trabalhoav3;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class MainFrame extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel painelPrincipal;

    private static final String TELA_BUSCA = "Busca";
    private static final String TELA_RESULTADOS = "Resultados";

    public MainFrame() {
        setTitle("Buscador de Padrões - CPU vs GPU");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        cardLayout = new CardLayout();
        painelPrincipal = new JPanel(cardLayout);
        painelPrincipal.add(criarTelaBusca(), TELA_BUSCA);
        painelPrincipal.add(criarTelaResultados(), TELA_RESULTADOS);
        add(painelPrincipal);
    }

    private JPanel criarTelaBusca() {
        JPanel painel = new JPanel(new BorderLayout(20, 20));
        painel.setBackground(new Color(39, 40, 34));
        painel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        JPanel painelControles = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        painelControles.setOpaque(false);

        JLabel lblPalavra = new JLabel("Palavra:");
        lblPalavra.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblPalavra.setForeground(new Color(248, 248, 242));

        JTextField txtPalavra = new JTextField(15);
        txtPalavra.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        JCheckBox chkBuscaExata = new JCheckBox("Busca Exata");
        chkBuscaExata.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        chkBuscaExata.setOpaque(false);
        chkBuscaExata.setForeground(new Color(248, 248, 242));

        JButton btnAdicionarLivro = new JButton("+ Adicionar Livros");
        btnAdicionarLivro.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAdicionarLivro.setFocusPainted(false);

        painelControles.add(lblPalavra);
        painelControles.add(txtPalavra);
        painelControles.add(chkBuscaExata);
        painelControles.add(btnAdicionarLivro);
        JPanel estantePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        estantePanel.setBackground(new Color(30, 31, 28));
        carregarLivrosPadrao(estantePanel);
        JScrollPane scrollEstante = new JScrollPane(estantePanel);
        scrollEstante.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), 
                "Sua Biblioteca (Arquivos .txt)", 
                0, 0, new Font("Segoe UI", Font.BOLD, 14), Color.DARK_GRAY));
        scrollEstante.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        estantePanel.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                
                try {
                    @SuppressWarnings("unchecked")
                    java.util.List<File> arquivosArrastados = (java.util.List<File>) support.getTransferable().getTransferData(java.awt.datatransfer.DataFlavor.javaFileListFlavor);
                    
                    boolean adicionouNovo = false;
                    for (File f : arquivosArrastados) {
                        if (f.getName().toLowerCase().endsWith(".txt")) {
                            estantePanel.add(new BookWidget(f));
                            adicionouNovo = true;
                        }
                    }
                    
                    if (adicionouNovo) {
                        estantePanel.revalidate();
                        estantePanel.repaint();
                    }
                    return true;
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return false;
                }
            }
        });

        btnAdicionarLivro.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true);
            
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File[] arquivosSelecionados = fileChooser.getSelectedFiles();
                for (File f : arquivosSelecionados) {
                    if (f.getName().toLowerCase().endsWith(".txt")) {
                        estantePanel.add(new BookWidget(f));
                    }
                }
                estantePanel.revalidate();
                estantePanel.repaint();
            }
        });
        JButton btnBuscar = new JButton("Processar Busca (CPU & GPU)");
        btnBuscar.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnBuscar.setBackground(new Color(166, 226, 46));
        btnBuscar.setForeground(new Color(39, 40, 34));
        btnBuscar.setFocusPainted(false);
        btnBuscar.setPreferredSize(new Dimension(0, 50));

        btnBuscar.addActionListener(e -> {
            String palavra = txtPalavra.getText().trim();
            boolean buscaExata = chkBuscaExata.isSelected();

            if (palavra.isEmpty()) {
                JOptionPane.showMessageDialog(painel, "Por favor, digite uma palavra para buscar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            java.util.List<File> arquivosSelecionados = new java.util.ArrayList<>();
            for (Component comp : estantePanel.getComponents()) {
                if (comp instanceof BookWidget) {
                    BookWidget widget = (BookWidget) comp;
                    if (widget.isSelecionado()) {
                        arquivosSelecionados.add(widget.getArquivoReal());
                    }
                }
            }

            if (arquivosSelecionados.isEmpty()) {
                JOptionPane.showMessageDialog(painel, "A sua estante está vazia. Adicione arquivos .txt!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            btnBuscar.setText("Processando... Por favor, aguarde.");
            btnBuscar.setEnabled(false);
            txtPalavra.setEnabled(false);
            new SwingWorker<Void, Void>() {
                java.util.List<BenchmarkResult> resultadosGerais;
                java.util.Map<String, Integer> ocorrenciasPorLivro = new java.util.LinkedHashMap<>();
                java.util.Map<String, java.util.List<BenchmarkResult>> performancePorLivro = new java.util.LinkedHashMap<>();

                @Override
                protected Void doInBackground() throws Exception {
                    byte[] palavraEmBytes = palavra.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    int maxThreads = Runtime.getRuntime().availableProcessors();
                    int halfThreads = Math.max(1, maxThreads / 2);

                    java.util.List<java.nio.ByteBuffer> buffersGerais = new java.util.ArrayList<>();
                    for (File f : arquivosSelecionados) {
                        try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(f, "r");
                             java.nio.channels.FileChannel channel = raf.getChannel()) {
                            buffersGerais.add(channel.map(java.nio.channels.FileChannel.MapMode.READ_ONLY, 0, channel.size()));
                        }
                    }

                    resultadosGerais = new java.util.ArrayList<>();
                    resultadosGerais.add(BenchmarkRunner.runTest("Serial CPU", () -> SerialCpuSearcher.search(buffersGerais, palavraEmBytes, buscaExata)));
                    resultadosGerais.add(BenchmarkRunner.runTest("Parallel CPU (Max)", () -> ParallelCpu.search(buffersGerais, palavraEmBytes, buscaExata, maxThreads)));
                    resultadosGerais.add(BenchmarkRunner.runTest("Parallel CPU (" + halfThreads + " Threads)", () -> ParallelCpu.search(buffersGerais, palavraEmBytes, buscaExata, halfThreads)));
                    resultadosGerais.add(BenchmarkRunner.runTest("Parallel GPU", () -> ParallelGpu.search(buffersGerais, palavraEmBytes, buscaExata)));

                    if (arquivosSelecionados.size() > 1) {
                        for (File f : arquivosSelecionados) {
                            String nomeLivro = f.getName().replaceFirst("[.][^.]+$", "");
                            
                            java.util.List<java.nio.ByteBuffer> bufferUnico = new java.util.ArrayList<>();
                            try (java.io.RandomAccessFile raf = new java.io.RandomAccessFile(f, "r");
                                 java.nio.channels.FileChannel channel = raf.getChannel()) {
                                bufferUnico.add(channel.map(java.nio.channels.FileChannel.MapMode.READ_ONLY, 0, channel.size()));
                            }

                            int contagem = SerialCpuSearcher.search(bufferUnico, palavraEmBytes, buscaExata);
                            ocorrenciasPorLivro.put(nomeLivro, contagem);
                            bufferUnico.get(0).rewind();

                            java.util.List<BenchmarkResult> perfLivro = new java.util.ArrayList<>();
                            perfLivro.add(BenchmarkRunner.runTest("Serial CPU", () -> SerialCpuSearcher.search(bufferUnico, palavraEmBytes, buscaExata)));
                            perfLivro.add(BenchmarkRunner.runTest("Parallel CPU", () -> ParallelCpu.search(bufferUnico, palavraEmBytes, buscaExata, maxThreads)));
                            perfLivro.add(BenchmarkRunner.runTest("Parallel GPU", () -> ParallelGpu.search(bufferUnico, palavraEmBytes, buscaExata)));
                            
                            performancePorLivro.put(nomeLivro, perfLivro);
                        }
                    }
                    CsvExporter.exportCompleto(resultadosGerais, ocorrenciasPorLivro, performancePorLivro, "resultados_completos.csv");
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        
                        btnBuscar.setText("Processar Busca (CPU & GPU)");
                        btnBuscar.setEnabled(true);
                        txtPalavra.setEnabled(true);

                        montarTelaResultadosComDados(resultadosGerais, ocorrenciasPorLivro, performancePorLivro, palavra);
                        cardLayout.show(painelPrincipal, TELA_RESULTADOS);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(painel, "Erro durante a busca: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                        btnBuscar.setText("Processar Busca (CPU & GPU)");
                        btnBuscar.setEnabled(true);
                        txtPalavra.setEnabled(true);
                    }
                }
            }.execute();
        });

        painel.add(painelControles, BorderLayout.NORTH);
        painel.add(scrollEstante, BorderLayout.CENTER);
        painel.add(btnBuscar, BorderLayout.SOUTH);

        return painel;
    }

    private JPanel criarTelaResultados() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(new Color(39, 40, 34));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        topPanel.setOpaque(false);

        JButton btnVoltar = new JButton("← Voltar para Busca");
        btnVoltar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnVoltar.setFocusPainted(false);
        
        btnVoltar.addActionListener(e -> cardLayout.show(painelPrincipal, TELA_BUSCA));
        
        topPanel.add(btnVoltar);

        JLabel labelTemporaria = new JLabel("Aqui entrarão os gráficos do JFreeChart", SwingConstants.CENTER);
        labelTemporaria.setFont(new Font("Segoe UI", Font.ITALIC, 18));
        labelTemporaria.setForeground(Color.GRAY);

        painel.add(topPanel, BorderLayout.NORTH);
        painel.add(labelTemporaria, BorderLayout.CENTER);

        painel.setName(TELA_RESULTADOS);

        return painel;
    }

    private void montarTelaResultadosComDados(
            java.util.List<BenchmarkResult> resGerais, 
            java.util.Map<String, Integer> ocorrenciasPorLivro,
            java.util.Map<String, java.util.List<BenchmarkResult>> perfPorLivro,
            String palavraBuscada) {
        
        for (Component comp : painelPrincipal.getComponents()) {
            if (comp.getName() != null && comp.getName().equals(TELA_RESULTADOS)) {
                JPanel telaRes = (JPanel) comp;
                
                BorderLayout layout = (BorderLayout) telaRes.getLayout();
                Component centroAntigo = layout.getLayoutComponent(BorderLayout.CENTER);
                if (centroAntigo != null) telaRes.remove(centroAntigo);

                JPanel painelSplit = new JPanel(new BorderLayout(10, 10));
                painelSplit.setOpaque(false);

                JPanel menuLateral = new JPanel();
                menuLateral.setLayout(new BoxLayout(menuLateral, BoxLayout.Y_AXIS));
                menuLateral.setOpaque(false);
                menuLateral.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 10));

                JButton btnGraficoGeral = new JButton("Comparativo Geral");
                JButton btnGraficoThreads = new JButton("Análise de Threads");
                JButton btnOcorrenciasLivro = new JButton("Ocorrências por Livro");
                JButton btnPerfLivro = new JButton("Desempenho por Livro");
                
                Dimension dim = new Dimension(200, 40);
                JButton[] botoes = {btnGraficoGeral, btnGraficoThreads, btnOcorrenciasLivro, btnPerfLivro};
                for(JButton b : botoes) {
                    b.setMaximumSize(dim);
                    b.setUI(new javax.swing.plaf.basic.BasicButtonUI()); 
                    
                    b.setBackground(new Color(73, 72, 62));
                    b.setForeground(new Color(248, 248, 242));
                    b.setFocusPainted(false);
                    b.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    b.setCursor(new Cursor(Cursor.HAND_CURSOR));
                }

                menuLateral.add(btnGraficoGeral);
                menuLateral.add(Box.createRigidArea(new Dimension(0, 10)));
                menuLateral.add(btnGraficoThreads);
                
                if (!ocorrenciasPorLivro.isEmpty()) {
                    menuLateral.add(Box.createRigidArea(new Dimension(0, 10)));
                    menuLateral.add(btnOcorrenciasLivro);
                    menuLateral.add(Box.createRigidArea(new Dimension(0, 10)));
                    menuLateral.add(btnPerfLivro);
                }

                CardLayout chartCardLayout = new CardLayout();
                JPanel painelGraficosCard = new JPanel(chartCardLayout);
                painelGraficosCard.setOpaque(false);

                painelGraficosCard.add(ChartGenerator.createPerformanceChart(resGerais), "GERAL");
                painelGraficosCard.add(ChartGenerator.createScalabilityChart(resGerais), "THREADS");
                
                if (!ocorrenciasPorLivro.isEmpty()) {
                    painelGraficosCard.add(ChartGenerator.createOccurrencesPerBookChart(ocorrenciasPorLivro), "OCORRENCIAS");
                    painelGraficosCard.add(ChartGenerator.createPerformancePerBookChart(perfPorLivro), "PERF_LIVRO");
                }

                btnGraficoGeral.addActionListener(e -> chartCardLayout.show(painelGraficosCard, "GERAL"));
                btnGraficoThreads.addActionListener(e -> chartCardLayout.show(painelGraficosCard, "THREADS"));
                btnOcorrenciasLivro.addActionListener(e -> chartCardLayout.show(painelGraficosCard, "OCORRENCIAS"));
                btnPerfLivro.addActionListener(e -> chartCardLayout.show(painelGraficosCard, "PERF_LIVRO"));

                int qtdEncontrada = resGerais.get(0).getOccurrences();
                JLabel lblResumo = new JLabel(String.format(" A palavra '%s' foi encontrada %d vezes (Total).", palavraBuscada, qtdEncontrada));
                lblResumo.setFont(new Font("Consolas", Font.BOLD, 22));
                lblResumo.setForeground(new Color(249, 38, 114)); // Rosa Monokai
                lblResumo.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 0));

                painelSplit.add(lblResumo, BorderLayout.NORTH);
                painelSplit.add(menuLateral, BorderLayout.WEST);
                painelSplit.add(painelGraficosCard, BorderLayout.CENTER);

                telaRes.add(painelSplit, BorderLayout.CENTER);
                telaRes.revalidate();
                telaRes.repaint();
                break;
            }
        }
    }

    private void carregarLivrosPadrao(JPanel estantePanel) {
        File pastaSamples = new File("samples"); 

        if (pastaSamples.exists() && pastaSamples.isDirectory()) {
            File[] arquivos = pastaSamples.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
            
            if (arquivos != null) {
                for (File f : arquivos) {
                    estantePanel.add(new BookWidget(f));
                }
            }
        } else {
            System.out.println("Aviso: Pasta 'samples' não encontrada na raiz do projeto. A estante iniciará vazia.");
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}