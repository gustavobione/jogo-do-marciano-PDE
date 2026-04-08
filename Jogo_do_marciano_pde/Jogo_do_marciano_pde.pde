// ==========================================
// 1. ASSETS (IMAGENS E MÍDIA)
// ==========================================
PImage imgAlien, imgArvore, imgUfo;

// ==========================================
// 2. ESTADO DO JOGO (VARIÁVEIS GLOBAIS)
// ==========================================
int telaAtual = 0; // 0: Menu | 1: Jogo | 2: Vitória | 3: Game Over
int numeroSecreto;
int tentativas = 0;
int maxTentativas = 7;
int tempoTremor = 0; // Controla o tempo da animação de erro (Screen Shake)

String palpiteDigitado = "";
String feedbackVisual = "Aguardando varredura...";

// ==========================================
// 3. CONFIGURAÇÃO INICIAL (Roda 1x)
// ==========================================
void setup() {
  size(800, 600);
  imageMode(CENTER); // Facilita o alinhamento das imagens pelo eixo central
  
  // Carregamento dos arquivos e ajuste automático de proporção (o '0' calcula o eixo correspondente)
  imgAlien = loadImage("Alien.png");
  if (imgAlien != null) imgAlien.resize(0, 200);   
  
  imgArvore = loadImage("Arvore.png");
  if (imgArvore != null) imgArvore.resize(0, 250); 
  
  imgUfo = loadImage("UFO.png"); 
  if (imgUfo != null) imgUfo.resize(350, 0);       
  
  iniciarRodada();
}

// ==========================================
// 4. MOTOR DO JOGO (Roda 60x por segundo)
// ==========================================
void draw() {
  // Se houver um erro recente, a tela inteira sai do eixo normal (Tremor)
  if (tempoTremor > 0) {
    translate(random(-5, 5), random(-5, 5));
    tempoTremor--;
  }

  // Roteador de Telas: Decide o que desenhar com base no Estado Atual
  switch(telaAtual) {
    case 0: desenharMenu();      break;
    case 1: desenharJogo();      break;
    case 2: desenharVitoria();   break;
    case 3: desenharGameOver();  break;
  }
}

// ==========================================
// 5. COMPONENTES DE INTERFACE (Telas)
// ==========================================

void desenharMenu() {
  background(15, 20, 30); 
  
  if (imgArvore != null) {
    image(imgArvore, width/2, height/2 - 50); 
  }
  
  fill(0, 255, 100); 
  textAlign(CENTER, CENTER);
  textSize(40);
  text("CAÇADA AO MARCIANO", width/2, 80);
  
  fill(255);
  textSize(20);
  text("Aperte [ENTER] para iniciar a busca", width/2, height - 80);
}

void desenharJogo() {
  background(20, 20, 20); 

  // Fundo do Painel Central
  fill(40, 40, 50);
  rect(100, 100, width - 200, height - 200, 20);

  // Título do Painel
  fill(255);
  textAlign(CENTER, TOP);
  textSize(24);
  text("Sinal do Radar", width/2, 130);

  // Texto de Feedback de Distância
  fill(255, 200, 0);
  textSize(20);
  text(feedbackVisual, width/2, 200);

  // Caixa de Digitação do Jogador
  fill(10);
  rect(width/2 - 100, 300, 200, 60, 10);
  
  // Texto Digitado + Lógica do Cursor Piscando (Aparece metade dos frames de 1 segundo)
  fill(0, 255, 100); 
  textSize(32);
  String cursor = (frameCount % 60 < 30) ? "_" : "";
  text(palpiteDigitado + cursor, width/2, 315); 

  desenharBateria();
}

void desenharVitoria() {
  background(0, 50, 0); 
  
  if (imgAlien != null) {
    image(imgAlien, width/2, height/2); 
  }
  
  fill(255);
  textAlign(CENTER, CENTER);
  textSize(40);
  text("MARCIANO CAPTURADO!", width/2, 100);
  
  textSize(20);
  text("Pressione [ESC] para voltar ao terminal", width/2, height - 80);
}

void desenharGameOver() {
  background(50, 0, 0); 
  
  if (imgUfo != null) {
    image(imgUfo, width/2, height/2 - 20); 
  }
  
  fill(255);
  textAlign(CENTER, CENTER);
  textSize(40);
  text("O ALVO ESCAPOU...", width/2, 100);
  
  textSize(20);
  text("O sinal vinha da árvore: " + numeroSecreto, width/2, height - 120);
  text("Pressione [ESC] para voltar ao terminal", width/2, height - 80);
}

void desenharBateria() {
  int larguraTotal = 300;
  int alturaBarra = 25;
  int posX = width/2 - larguraTotal/2; 
  int posY = 430; 

  // O cast (float) evita que a divisão de dois inteiros retorne sempre 0
  float energiaRestante = (maxTentativas - tentativas) / (float)maxTentativas;
  float larguraAtual = larguraTotal * energiaRestante;

  fill(200);
  textSize(16);
  textAlign(CENTER, BOTTOM);
  text("ENERGIA DO RADAR", width/2, posY - 10);

  // Carcaça da bateria
  fill(30, 30, 40);
  stroke(100);       
  strokeWeight(2);
  rect(posX, posY, larguraTotal, alturaBarra, 5); 

  // Pino positivo da bateria
  fill(100);
  noStroke();
  rect(posX + larguraTotal + 2, posY + 5, 8, 15, 2);

  // Sistema de Cores e Urgência
  if (energiaRestante > 0.5) fill(0, 255, 100);      // Seguro
  else if (energiaRestante > 0.25) fill(255, 200, 0); // Atenção
  else fill(255, 50, 50);                            // Crítico

  // O max() impede que o retângulo tente desenhar uma largura invertida (negativa)
  rect(posX + 2, posY + 2, max(0, larguraAtual - 4), alturaBarra - 4, 3);
}

// ==========================================
// 6. LÓGICA E REGRAS DE NEGÓCIO
// ==========================================

void iniciarRodada() {
  numeroSecreto = (int) random(1, 101);
  tentativas = 0;
  palpiteDigitado = "";
  feedbackVisual = "Aguardando varredura das árvores...";
}

void verificarPalpite() {
  int palpite = int(palpiteDigitado);
  tentativas++;
  
  if (palpite == numeroSecreto) {
    telaAtual = 2; // Acionou o estado de Vitória
  } else {
    // Calculamos o valor absoluto da diferença para saber o quão "quente" o palpite foi
    int distancia = abs(palpite - numeroSecreto);
    
    if (distancia <= 10) feedbackVisual = "ALERTA: SINAL MUITO FORTE!";
    else if (distancia <= 30) feedbackVisual = "Sinal moderado detectado.";
    else feedbackVisual = "Sinal fraco. Procure em outra área.";
    
    // Dica direcional
    if (palpite > numeroSecreto) feedbackVisual += "\nTente um setor MENOR.";
    else feedbackVisual += "\nTente um setor MAIOR.";
    
    tempoTremor = 20; 
    palpiteDigitado = ""; // Limpa o input para a próxima tentativa
    
    if (tentativas >= maxTentativas) {
      telaAtual = 3; // Acionou o estado de Derrota
    }
  }
}

// ==========================================
// 7. EVENTOS DE HARDWARE (Teclado)
// ==========================================

void keyPressed() {
  // Transição do Menu para o Jogo
  if (telaAtual == 0 && key == ENTER) {
    iniciarRodada();
    telaAtual = 1;
  } 
  
  // Regras de digitação apenas quando estiver na tela de Jogo
  else if (telaAtual == 1) {
    if (key >= '0' && key <= '9') {
      if (palpiteDigitado.length() < 3) palpiteDigitado += key;
    } 
    else if (key == BACKSPACE && palpiteDigitado.length() > 0) {
      palpiteDigitado = palpiteDigitado.substring(0, palpiteDigitado.length() - 1);
    } 
    else if (key == ENTER && palpiteDigitado.length() > 0) {
      verificarPalpite();
    }
  }
  
  // Prevenção de fechamento acidental e retorno ao Menu
  else if ((telaAtual == 2 || telaAtual == 3) && key == ESC) {
    key = 0; // Anula a função padrão do ESC de fechar a janela do Processing
    telaAtual = 0;
  }
}
