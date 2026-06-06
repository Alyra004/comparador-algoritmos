package com.trabalhoav3;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;

public class BookWidget extends JPanel {
    private final File arquivoReal;
    private final JTextField campoTitulo;
    private final JCheckBox chkSelecionado;
    private Image imagemCapa; // Variável para segurar o seu render 3D

    public BookWidget(File arquivo) {
        this.arquivoReal = arquivo;
        setLayout(new BorderLayout(0, 5));
        setOpaque(false);
        setPreferredSize(new Dimension(180, 180));

        // Tenta carregar a arte do livro (Sprite)
        try {
            imagemCapa = ImageIO.read(new File("capa.png"));
        } catch (Exception e) {
            imagemCapa = null; // Se não achar, usaremos o fallback de código
        }

        JPanel capaLivro = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (imagemCapa != null) {
                    // Desenha a sua imagem 3D renderizada!
                    g2.drawImage(imagemCapa, 15, 0, 140, 140, this);
                } else {
                    // Fallback: Desenho vetorial com cores Monokai (Azul Neon escurecido)
                    g2.setColor(new Color(102, 217, 239, 180)); 
                    g2.fillRoundRect(15, 0, 100, 140, 10, 10);
                    g2.setColor(new Color(102, 217, 239)); // Lombada mais clara
                    g2.fillRect(15, 0, 15, 140);
                }
                g2.dispose();
            }
        };
        capaLivro.setPreferredSize(new Dimension(130, 140));
        capaLivro.setOpaque(false);

        JPanel painelBase = new JPanel(new BorderLayout());
        painelBase.setOpaque(false);

        chkSelecionado = new JCheckBox();
        chkSelecionado.setSelected(true);
        chkSelecionado.setOpaque(false);
        chkSelecionado.setCursor(new Cursor(Cursor.HAND_CURSOR));

        String nomeSemExtensao = arquivo.getName().replaceFirst("[.][^.]+$", "");
        campoTitulo = new JTextField(nomeSemExtensao);
        campoTitulo.setHorizontalAlignment(JTextField.CENTER);
        campoTitulo.setFont(new Font("Consolas", Font.BOLD, 12)); // Fonte estilo código
        campoTitulo.setForeground(new Color(248, 248, 242)); // Texto claro Monokai
        campoTitulo.setBorder(new EmptyBorder(2, 2, 2, 2));
        campoTitulo.setOpaque(false);
        campoTitulo.setCaretColor(Color.WHITE);

        painelBase.add(chkSelecionado, BorderLayout.WEST);
        painelBase.add(campoTitulo, BorderLayout.CENTER);

        add(capaLivro, BorderLayout.CENTER);
        add(painelBase, BorderLayout.SOUTH);
    }

    public File getArquivoReal() { return arquivoReal; }
    public String getTituloVisual() { return campoTitulo.getText(); }
    public boolean isSelecionado() { return chkSelecionado.isSelected(); }
}