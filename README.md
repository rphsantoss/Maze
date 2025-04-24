# LabirintoApp

Este app Java lê um arquivo de texto que descreve um labirinto e encontra um caminho da entrada (`E`) até a saída (`S`) usando busca em profundidade com backtracking. Se houver até 10.000 células, exibe animação gráfica em Swing.

---

## Componentes principais

- **LabirintoApp**  
  Interface de linha de comando: pede nome do arquivo e mostra resultado.

- **Labirinto**  
  Carrega mapa (linhas, colunas, grid) de arquivo  
  Valida dimensões e garante exatamente uma entrada (`E`) e uma saída (`S`)  
  Métodos: `getValor`, `setValor`, `getEntrada()`, `getSaida()`, `imprimir()`

- **LabirintoException**  
  Exceção customizada para erros de leitura e validação.

- **Coordenada**  
  Par (linha,coluna) com `equals`/`hashCode` para comparações.

- **Pilha<T>** e **Fila<T>**  
  Estruturas simples LIFO e FIFO para o solver.

- **Solver**  
  Busca em profundidade sem heurística  
  Usa pilha para caminho e pilha de filas para bifurcações  
  Marca células visitadas e faz *stepback* quando não há rota  
  Marca caminho parcial com `*`

- **LabirintoAnimator** e **AnimationPanel**  
  Cria frames coloridos de cada passo e mostra séries de imagens em janela Swing

---

## Como usar

1. Compile:  
   ```bash
   javac LabirintoApp.java
   ```
2. Prepare arquivo de entrada:  
   ```txt
   5    ← número de linhas
   7    ← número de colunas
   #######
   #E    #
   # ### #
   #    S#
   #######
   ```
3. Execute:  
   ```bash
   java LabirintoApp
   ```
   - Digite o nome do arquivo (ex.: `labirinto1.txt`).  
   - Se for grande, animação é desativada.  
   - Pode usar parâmetros opcionais (delay da animação).

---

## Tratamento de erros

- Arquivo ausente ou formato incorreto lança `LabirintoException`.  
- Mensagens explicam linha inválida, dimensões ou múltiplas entradas/saídas.  
- Exceções não tratadas mostram stack trace para debug.

---

## Conceitos e complexidade

- **Busca em profundidade**: O(V+E), V=número de células, E=adjacências  
- **Backtracking**: retorna ao último ponto com alternativas  
- **Pilha**: última célula adicionada é explorada primeiro  
- **Fila**: mantém ordem para explorar vizinhos

---
