import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class LabirintoApp {
    static class LabirintoException extends Exception {
        LabirintoException(String mensagem) {
            super(mensagem);
        }
    }

    static class Coordenada {
        final int linha;
        final int coluna;

        Coordenada(int linha, int coluna) {
            this.linha = linha;
            this.coluna = coluna;
        }

        int getLinha() {
            return linha;
        }

        int getColuna() {
            return coluna;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Coordenada)) return false;
            Coordenada outra = (Coordenada) obj;
            return linha == outra.linha && coluna == outra.coluna;
        }

        @Override
        public int hashCode() {
            return Objects.hash(linha, coluna);
        }

        @Override
        public String toString() {
            return "(" + linha + "," + coluna + ")";
        }
    }

    static class Labirinto {
        private final char[][] mapa;
        private final int linhas;
        private final int colunas;

        Labirinto(String arquivo) throws LabirintoException {
            List<String> linhasArquivo = lerArquivo(arquivo);
            int nLinhas = parseInt(linhasArquivo.get(0));
            int nColunas = parseInt(linhasArquivo.get(1));
            validarDimensoes(nLinhas, nColunas, linhasArquivo.size());

            this.linhas = nLinhas;
            this.colunas = nColunas;
            this.mapa = new char[linhas][colunas];

            for (int i = 0; i < linhas; i++) {
                String linhaMapa = linhasArquivo.get(i + 2);
                for (int j = 0; j < colunas; j++) {
                    mapa[i][j] = j < linhaMapa.length() ? linhaMapa.charAt(j) : ' ';
                }
            }
            validarEntradasSaidas();
        }

        private List<String> lerArquivo(String arquivo) throws LabirintoException {
            try {
                return Files.readAllLines(Paths.get(arquivo));
            } catch (IOException e) {
                throw new LabirintoException("Erro ao ler arquivo: " + e.getMessage());
            }
        }

        private int parseInt(String valor) throws LabirintoException {
            try {
                return Integer.parseInt(valor.trim());
            } catch (NumberFormatException e) {
                throw new LabirintoException("Valor não é inteiro: " + valor);
            }
        }

        private void validarDimensoes(int nL, int nC, int totalLinhas) throws LabirintoException {
            if (nL < 1 || nC < 1 || totalLinhas < nL + 2) {
                throw new LabirintoException("Dimensões inválidas ou mapa incompleto");
            }
        }

        private void validarEntradasSaidas() throws LabirintoException {
            int entradas = 0;
            int saidas = 0;

            for (int i = 0; i < linhas; i++) {
                for (int j = 0; j < colunas; j++) {
                    char c = mapa[i][j];
                    if (c == 'E') {
                        boolean naBorda = i == 0 || i == linhas - 1 || j == 0 || j == colunas - 1;
                        if (!naBorda) {
                            throw new LabirintoException("Entrada fora da borda: (" + i + "," + j + ")");
                        }
                        entradas++;
                    } else if (c == 'S') {
                        saidas++;
                    }
                }
            }
            if (entradas != 1) throw new LabirintoException("Número de entradas inválido: " + entradas);
            if (saidas != 1)   throw new LabirintoException("Número de saídas inválido: " + saidas);
        }

        char getValor(int i, int j) throws LabirintoException {
            if (i < 0 || i >= linhas || j < 0 || j >= colunas) {
                throw new LabirintoException("Coordenada fora dos limites: (" + i + "," + j + ")");
            }
            return mapa[i][j];
        }

        void setValor(int i, int j, char v) throws LabirintoException {
            getValor(i, j);
            mapa[i][j] = v;
        }

        Coordenada getEntrada() {
            return find('E');
        }

        Coordenada getSaida() {
            return find('S');
        }

        private Coordenada find(char alvo) {
            for (int i = 0; i < linhas; i++) {
                for (int j = 0; j < colunas; j++) {
                    if (mapa[i][j] == alvo) return new Coordenada(i, j);
                }
            }
            return null;
        }

        void imprimir() {
            for (int i = 0; i < linhas; i++) {
                for (int j = 0; j < colunas; j++) {
                    System.out.print(mapa[i][j]);
                }
                System.out.println();
            }
        }
    }

    static class Pilha<T> {
        private final Object[] elementos;
        private int topo = -1;

        Pilha(int capacidade) {
            elementos = new Object[capacidade];
        }

        void push(T x) throws LabirintoException {
            if (topo + 1 >= elementos.length) throw new LabirintoException("Pilha cheia");
            elementos[++topo] = x;
        }

        @SuppressWarnings("unchecked")
        T pop() throws LabirintoException {
            if (topo < 0) throw new LabirintoException("Pilha vazia");
            return (T) elementos[topo--];
        }

        boolean isEmpty() {
            return topo < 0;
        }
    }

    static class Fila<T> {
        private final Object[] elementos;
        private int inicio = 0, fim = 0, tamanho = 0;

        Fila(int capacidade) {
            elementos = new Object[capacidade];
        }

        void enqueue(T x) throws LabirintoException {
            if (tamanho == elementos.length) throw new LabirintoException("Fila cheia");
            elementos[fim] = x;
            fim = (fim + 1) % elementos.length;
            tamanho++;
        }

        @SuppressWarnings("unchecked")
        T dequeue() throws LabirintoException {
            if (tamanho == 0) throw new LabirintoException("Fila vazia");
            T x = (T) elementos[inicio];
            elementos[inicio] = null;
            inicio = (inicio + 1) % elementos.length;
            tamanho--;
            return x;
        }

        boolean isEmpty() {
            return tamanho == 0;
        }
    }

    static class Solver {
        private final Labirinto labirinto;
        private final Pilha<Coordenada> caminho;
        private final Pilha<Fila<Coordenada>> bifurcacoes;
        private final boolean[][] visitado;
        private Coordenada atual;
        private final LabirintoAnimator animator;

        Solver(Labirinto lab, LabirintoAnimator anim) throws LabirintoException {
            this.labirinto = lab;
            int cap = lab.linhas * lab.colunas;
            this.caminho = new Pilha<>(cap);
            this.bifurcacoes = new Pilha<>(cap);
            this.visitado = new boolean[lab.linhas][lab.colunas];
            this.animator = anim;

            this.atual = lab.getEntrada();
            if (this.atual == null) throw new LabirintoException("Entrada não encontrada");
        }

        private void capturar() throws LabirintoException {
            if (animator != null) animator.captureFrame(labirinto);
        }

        private Fila<Coordenada> criarAdjacentes(Coordenada p) throws LabirintoException {
            Fila<Coordenada> fila = new Fila<>(4);
            int r = p.getLinha(), c = p.getColuna();
            int[][] deltas = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
            for (int[] d : deltas) {
                int nr = r + d[0], nc = c + d[1];
                if (nr >= 0 && nr < labirinto.linhas && nc >= 0 && nc < labirinto.colunas) {
                    char v = labirinto.getValor(nr, nc);
                    if ((v == ' ' || v == 'S') && !visitado[nr][nc]) {
                        visitado[nr][nc] = true;
                        fila.enqueue(new Coordenada(nr, nc));
                    }
                }
            }
            return fila;
        }

        boolean solve() throws LabirintoException {
            visitado[atual.getLinha()][atual.getColuna()] = true;
            caminho.push(atual);
            bifurcacoes.push(criarAdjacentes(atual));
            capturar();

            while (!caminho.isEmpty()) {
                char valor = labirinto.getValor(atual.getLinha(), atual.getColuna());
                if (valor == 'S') {
                    capturar();
                    return true;
                }
                Fila<Coordenada> fila = bifurcacoes.pop();
                if (fila.isEmpty()) {
                    Coordenada volta = caminho.pop();
                    if (labirinto.getValor(volta.getLinha(), volta.getColuna()) == '*') {
                        labirinto.setValor(volta.getLinha(), volta.getColuna(), ' ');
                    }
                    capturar();
                    if (!caminho.isEmpty()) {
                        atual = caminho.pop();
                        caminho.push(atual);
                    }
                } else {
                    bifurcacoes.push(fila);
                    Coordenada prox = fila.dequeue();
                    if (labirinto.getValor(prox.getLinha(), prox.getColuna()) != 'S') {
                        labirinto.setValor(prox.getLinha(), prox.getColuna(), '*');
                    }
                    caminho.push(prox);
                    bifurcacoes.push(criarAdjacentes(prox));
                    atual = prox;
                    capturar();
                }
            }
            capturar();
            return false;
        }
    }

    static class LabirintoAnimator {
        private final BufferedImage[] buffer;
        private final int capacidade = 200;
        private final int cellSize;
        private int start = 0;
        private int count = 0;

        LabirintoAnimator(Labirinto l) {
            this.buffer = new BufferedImage[capacidade];
            int maxDim = 1200;
            int csW = Math.max(1, maxDim / l.colunas);
            int csH = Math.max(1, maxDim / l.linhas);
            this.cellSize = Math.min(csW, csH);
        }

        void captureFrame(Labirinto lab) throws LabirintoException {
            int w = lab.colunas * cellSize;
            int h = lab.linhas * cellSize;
            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics g = img.getGraphics();
            for (int i = 0; i < lab.linhas; i++) {
                for (int j = 0; j < lab.colunas; j++) {
                    char c = lab.mapa[i][j];
                    Color col = switch (c) {
                        case '#' -> Color.BLACK;
                        case 'E' -> Color.GREEN;
                        case 'S' -> Color.BLUE;
                        case '*' -> Color.RED;
                        default -> Color.WHITE;
                    };
                    g.setColor(col);
                    g.fillRect(j * cellSize, i * cellSize, cellSize, cellSize);
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawRect(j * cellSize, i * cellSize, cellSize, cellSize);
                }
            }
            g.dispose();
            if (count < capacidade) {
                buffer[(start + count) % capacidade] = img;
            } else {
                buffer[start] = img;
                start = (start + 1) % capacidade;
            }
            if (count < capacidade) count++;
        }

        BufferedImage[] getAll() {
            BufferedImage[] out = new BufferedImage[count];
            for (int i = 0; i < count; i++) {
                out[i] = buffer[(start + i) % capacidade];
            }
            return out;
        }

        void animate(int delayMs) {
            BufferedImage[] frames = getAll();
            if (frames.length == 0) return;
            JFrame frame = new JFrame("Animação");
            AnimationPanel panel = new AnimationPanel(this);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            new Timer(delayMs, e -> {
                panel.nextFrame();
                panel.repaint();
            }).start();
        }
    }

    static class AnimationPanel extends JPanel {
        private final LabirintoAnimator animator;
        private int index = 0;

        AnimationPanel(LabirintoAnimator animator) {
            this.animator = animator;
            BufferedImage[] frames = animator.getAll();
            if (frames.length > 0) {
                setPreferredSize(new Dimension(frames[0].getWidth(), frames[0].getHeight()));
            }
        }

        void nextFrame() {
            BufferedImage[] frames = animator.getAll();
            if (frames.length > 0) {
                index = (index + 1) % frames.length;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            BufferedImage[] frames = animator.getAll();
            if (frames.length > 0) {
                g.drawImage(frames[index], 0, 0, null);
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String diretorio = "labirintos";

        while (true) {
            try {
                System.out.print("Arquivo ('sair' para encerrar): ");
                String nomeArquivo = scanner.nextLine().trim();
                if (nomeArquivo.equalsIgnoreCase("sair")) {
                    System.out.println("Até logo!");
                    return;
                }

                Path caminho = Paths.get(diretorio, nomeArquivo);
                if (!Files.exists(caminho)) {
                    System.err.println("Arquivo não encontrado: " + caminho + "\n");
                    continue;
                }

                Labirinto labirinto = new Labirinto(caminho.toString());
                long totalCelulas = (long) labirinto.linhas * labirinto.colunas;
                LabirintoAnimator animator = totalCelulas <= 10_000 ? new LabirintoAnimator(labirinto) : null;
                boolean encontrado = new Solver(labirinto, animator).solve();

                if (animator != null) {
                    animator.animate(40);
                } else {
                    System.out.println("Labirinto grande demais (" + totalCelulas + " células), sem animação.\n");
                }

                System.out.println(encontrado ? "Caminho encontrado!\n" : "Nenhum caminho.\n");
                labirinto.imprimir();

            } catch (LabirintoException e) {
                System.err.println("Erro: " + e.getMessage() + "\n");
            } catch (Exception e) {
                System.err.println("Erro inesperado: " + e.getMessage());
                e.printStackTrace();
                break;
            }
        }
    }
}
