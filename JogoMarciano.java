import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.Random;

public class JogoMarciano extends JPanel implements KeyListener {

    // ==========================================
    // 1. ASSETS (IMAGENS E MÍDIA)
    // ==========================================
    private Image imgAlien, imgArvore, imgUfo;

    // ==========================================
    // 2. ESTADO DO JOGO (VARIÁVEIS GLOBAIS)
    // ==========================================
    private int telaAtual = 0; // 0: Menu | 1: Jogo | 2: Vitória | 3: Game Over
    private int numeroSecreto;
    private int tentativas = 0;
    private int maxTentativas = 7;
    private int tempoTremor = 0;
    private int frameCount = 0;

    private String palpiteDigitado = "";
    private String feedbackVisual = "Aguardando varredura...";
    private Random random = new Random();

    public JogoMarciano() {
        // Configuração inicial da janela do painel
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);
        addKeyListener(this);

        // Carregamento de Imagens e Redimensionamento
        try {
            BufferedImage alienRAW = ImageIO.read(new File("Alien.png"));
            imgAlien = alienRAW.getScaledInstance(-1, 200, Image.SCALE_SMOOTH);
            
            BufferedImage arvoreRAW = ImageIO.read(new File("Arvore.png"));
            imgArvore = arvoreRAW.getScaledInstance(-1, 250, Image.SCALE_SMOOTH);
            
            BufferedImage ufoRAW = ImageIO.read(new File("UFO.png")); 
            imgUfo = ufoRAW.getScaledInstance(350, -1, Image.SCALE_SMOOTH);
        } catch (Exception e) {
            System.out.println("Erro ao carregar imagens. Verifique se estão na pasta correta.");
        }

        iniciarRodada();

        // 4. MOTOR DO JOGO (Roda a 60 FPS)
        Timer timer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frameCount++;
                repaint(); 
            }
        });
        timer.start();
    }

    // ==========================================
    // SUBSTITUTO DO 'draw()' NO JAVA SWING
    // ==========================================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Efeito de Tremor (Screen Shake)
        if (tempoTremor > 0) {
            int dx = random.nextInt(11) - 5; 
            int dy = random.nextInt(11) - 5; 
            g2d.translate(dx, dy);
            tempoTremor--;
        }

        // Roteador de Telas
        switch(telaAtual) {
            case 0: desenharMenu(g2d); break;
            case 1: desenharJogo(g2d); break;
            case 2: desenharVitoria(g2d); break;
            case 3: desenharGameOver(g2d); break;
        }
    }

    // ==========================================
    // 5. COMPONENTES DE INTERFACE (Telas)
    // ==========================================

    private void desenharMenu(Graphics2D g) {
        g.setColor(new Color(15, 20, 30));
        g.fillRect(0, 0, getWidth(), getHeight());

        if (imgArvore != null) {
            desenharImagemCentro(g, imgArvore, getWidth()/2, getHeight()/2 - 50);
        }

        g.setColor(new Color(0, 255, 100));
        desenharTextoCentro(g, "CAÇADA AO MARCIANO", getWidth()/2, 80, 40);

        g.setColor(Color.WHITE);
        desenharTextoCentro(g, "Aperte [ENTER] para iniciar a busca", getWidth()/2, getHeight() - 80, 20);
    }

    private void desenharJogo(Graphics2D g) {
        g.setColor(new Color(20, 20, 20));
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(new Color(40, 40, 50));
        g.fillRoundRect(100, 100, getWidth() - 200, getHeight() - 200, 20, 20);

        g.setColor(Color.WHITE);
        desenharTextoCentroTop(g, "Sinal do Radar", getWidth()/2, 130, 24);

        g.setColor(new Color(255, 200, 0));
        desenharTextoCentro(g, feedbackVisual, getWidth()/2, 200, 20);

        g.setColor(new Color(10, 10, 10));
        g.fillRoundRect(getWidth()/2 - 100, 300, 200, 60, 10, 10);

        g.setColor(new Color(0, 255, 100));
        String cursor = (frameCount % 60 < 30) ? "_" : "";
        desenharTextoCentro(g, palpiteDigitado + cursor, getWidth()/2, 335, 32);

        desenharBateria(g);
    }

    private void desenharVitoria(Graphics2D g) {
        g.setColor(new Color(0, 50, 0));
        g.fillRect(0, 0, getWidth(), getHeight());

        if (imgAlien != null) {
            desenharImagemCentro(g, imgAlien, getWidth()/2, getHeight()/2);
        }

        g.setColor(Color.WHITE);
        desenharTextoCentro(g, "MARCIANO CAPTURADO!", getWidth()/2, 100, 40);
        desenharTextoCentro(g, "Pressione [ESC] para voltar ao terminal", getWidth()/2, getHeight() - 80, 20);
    }

    private void desenharGameOver(Graphics2D g) {
        g.setColor(new Color(50, 0, 0));
        g.fillRect(0, 0, getWidth(), getHeight());

        if (imgUfo != null) {
            desenharImagemCentro(g, imgUfo, getWidth()/2, getHeight()/2 - 20);
        }

        g.setColor(Color.WHITE);
        desenharTextoCentro(g, "O ALVO ESCAPOU...", getWidth()/2, 100, 40);
        desenharTextoCentro(g, "O sinal vinha da árvore: " + numeroSecreto, getWidth()/2, getHeight() - 120, 20);
        desenharTextoCentro(g, "Pressione [ESC] para voltar ao terminal", getWidth()/2, getHeight() - 80, 20);
    }

    private void desenharBateria(Graphics2D g) {
        int larguraTotal = 300;
        int alturaBarra = 25;
        int posX = getWidth()/2 - larguraTotal/2;
        int posY = 430;

        float energiaRestante = (maxTentativas - tentativas) / (float)maxTentativas;
        int larguraAtual = (int)(larguraTotal * energiaRestante);

        g.setColor(new Color(200, 200, 200));
        desenharTextoCentroBot(g, "ENERGIA DO RADAR", getWidth()/2, posY - 10, 16);

        g.setColor(new Color(30, 30, 40));
        g.fillRoundRect(posX, posY, larguraTotal, alturaBarra, 10, 10);
        g.setColor(new Color(100, 100, 100));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(posX, posY, larguraTotal, alturaBarra, 10, 10);

        g.setColor(new Color(100, 100, 100));
        g.fillRoundRect(posX + larguraTotal + 2, posY + 5, 8, 15, 4, 4);

        if (energiaRestante > 0.5) g.setColor(new Color(0, 255, 100));
        else if (energiaRestante > 0.25) g.setColor(new Color(255, 200, 0));
        else g.setColor(new Color(255, 50, 50));

        int larguraPreenchimento = Math.max(0, larguraAtual - 4);
        g.fillRoundRect(posX + 2, posY + 2, larguraPreenchimento, alturaBarra - 4, 6, 6);
    }

    // ==========================================
    // MÉTODOS AUXILIARES DE DESENHO
    // ==========================================
    private void desenharImagemCentro(Graphics g, Image img, int x, int y) {
        int imgW = img.getWidth(null);
        int imgH = img.getHeight(null);
        g.drawImage(img, x - imgW/2, y - imgH/2, null);
    }

    private void desenharTextoCentro(Graphics g, String text, int x, int y, int size) {
        g.setFont(new Font("SansSerif", Font.BOLD, size));
        FontMetrics fm = g.getFontMetrics();
        int rx = x - fm.stringWidth(text) / 2;
        int ry = y + (fm.getAscent() - fm.getDescent()) / 2;
        
        if (text.contains("\n")) {
            String[] linhas = text.split("\n");
            ry = y - (fm.getHeight() * linhas.length) / 2 + fm.getAscent();
            for (String linha : linhas) {
                rx = x - fm.stringWidth(linha) / 2;
                g.drawString(linha, rx, ry);
                ry += fm.getHeight();
            }
        } else {
            g.drawString(text, rx, ry);
        }
    }

    private void desenharTextoCentroTop(Graphics g, String text, int x, int y, int size) {
        g.setFont(new Font("SansSerif", Font.BOLD, size));
        FontMetrics fm = g.getFontMetrics();
        int rx = x - fm.stringWidth(text) / 2;
        g.drawString(text, rx, y + fm.getAscent());
    }

    private void desenharTextoCentroBot(Graphics g, String text, int x, int y, int size) {
        g.setFont(new Font("SansSerif", Font.BOLD, size));
        FontMetrics fm = g.getFontMetrics();
        int rx = x - fm.stringWidth(text) / 2;
        g.drawString(text, rx, y - fm.getDescent());
    }

    // ==========================================
    // 6. LÓGICA E REGRAS DE NEGÓCIO
    // ==========================================
    private void iniciarRodada() {
        numeroSecreto = random.nextInt(100) + 1;
        tentativas = 0;
        palpiteDigitado = "";
        feedbackVisual = "Aguardando varredura das árvores...";
    }

    private void verificarPalpite() {
        int palpite = Integer.parseInt(palpiteDigitado);
        tentativas++;

        if (palpite == numeroSecreto) {
            telaAtual = 2; 
        } else {
            int distancia = Math.abs(palpite - numeroSecreto);

            if (distancia <= 10) feedbackVisual = "ALERTA: SINAL MUITO FORTE!";
            else if (distancia <= 30) feedbackVisual = "Sinal moderado detectado.";
            else feedbackVisual = "Sinal fraco. Procure em outra área.";

            if (palpite > numeroSecreto) feedbackVisual += "\nTente um setor MENOR.";
            else feedbackVisual += "\nTente um setor MAIOR.";

            tempoTremor = 20;
            palpiteDigitado = "";

            if (tentativas >= maxTentativas) {
                telaAtual = 3;
            }
        }
    }

    // ==========================================
    // 7. EVENTOS DE HARDWARE (Teclado)
    // ==========================================
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        char keyChar = e.getKeyChar();

        if (telaAtual == 0 && keyCode == KeyEvent.VK_ENTER) {
            iniciarRodada();
            telaAtual = 1;
        } 
        else if (telaAtual == 1) {
            if (keyChar >= '0' && keyChar <= '9') {
                if (palpiteDigitado.length() < 3) palpiteDigitado += keyChar;
            } 
            else if (keyCode == KeyEvent.VK_BACK_SPACE && palpiteDigitado.length() > 0) {
                palpiteDigitado = palpiteDigitado.substring(0, palpiteDigitado.length() - 1);
            } 
            else if (keyCode == KeyEvent.VK_ENTER && palpiteDigitado.length() > 0) {
                verificarPalpite();
            }
        } 
        else if ((telaAtual == 2 || telaAtual == 3) && keyCode == KeyEvent.VK_ESCAPE) {
            telaAtual = 0;
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}

    // ==========================================
    // BOOT DO JAVA
    // ==========================================
    public void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Caçada ao Marciano - Java Pro");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            
            JogoMarciano jogo = new JogoMarciano();
            frame.add(jogo);
            frame.pack();
            frame.setLocationRelativeTo(null); 
            frame.setVisible(true);
        });
    }
}