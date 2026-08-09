# Roteiro de Laboratório — Threads em Java

**PUC Minas · Engenharia de Software · LabDAMD · Unidade 0 — Revisão de SO e Concorrência**

Implementação das cinco partes (A–E) do roteiro "Threads em Java", usando **Java 21+**.
O domínio é propositalmente simples — um **guichê de atendimento** — para manter o foco
no *mecanismo de concorrência*, não na lógica de negócio.

O relatório com as respostas às perguntas de cada parte, os exercícios de fixação e a
análise crítica dos resultados medidos está em **[`RELATORIO.md`](RELATORIO.md)**.

## Objetivos

Ao final do laboratório, deve-se ser capaz de:

1. Explicar a diferença entre **processo** e **thread** do ponto de vista do SO.
2. Implementar threads em Java com **`Thread`** e **`Runnable`**.
3. Implementar a mesma solução com **`ExecutorService`** e **Virtual Threads (Java 21+)**.
4. Observar na prática o **custo real** de criar milhares de threads de SO.

## Pré-requisitos

- **JDK 21 ou superior** (a Parte E usa *Virtual Threads*, disponíveis a partir do Java 21).
  Confira com `java -version`.
- Um shell compatível com Bash para o script `run.sh` (opcional — dá para usar `javac`/`java` direto).

Não há dependências externas nem build tool: apenas o JDK.

## Estrutura do projeto

```
src/br/pucminas/labdamd/threads/
├── comum/
│   ├── Cronometro.java          # medição de tempo (nanoTime)
│   └── Guiche.java              # domínio compartilhado: "atender" um cliente (~1s)
├── partea/                      # Parte A — extends Thread
│   ├── AtendimentoThread.java
│   └── MainParteA.java
├── parteb/                      # Parte B — implements Runnable
│   ├── AtendimentoRunnable.java
│   └── MainParteB.java
├── partec/                      # Parte C — muitas threads de SO (limite do modelo clássico)
│   └── MainParteC.java
├── parted/                      # Parte D — ExecutorService (pool de threads)
│   └── MainParteD.java
└── partee/                      # Parte E — Virtual Threads (Java 21+)
    └── MainParteE.java
```

## Como compilar e executar

O script `run.sh` compila tudo em `./out` e executa cada parte:

```bash
./run.sh compile          # compila todas as fontes em ./out
./run.sh a                # Parte A (extends Thread)      — 5 clientes
./run.sh b                # Parte B (implements Runnable)  — 5 clientes
./run.sh c                # Parte C — 10.000 threads de SO
./run.sh c 20000          # Parte C com N threads (aumente para provocar o limite)
./run.sh d                # Parte D — pool fixo de 4, 10 clientes
./run.sh d --cached       # Parte D — newCachedThreadPool() (exercício de fixação)
./run.sh e                # Parte E — 100.000 virtual threads
./run.sh all              # A, B e D em sequência (C e E são pesadas; rode à parte)
./run.sh clean            # remove ./out
```

### Sem o `run.sh` (apenas `javac`/`java`)

```bash
# compilar
mkdir -p out && find src -name '*.java' | xargs javac -d out

# executar (ex.: Parte A)
java -Dstdout.encoding=UTF-8 -cp out br.pucminas.labdamd.threads.partea.MainParteA
```

> **Acentuação:** as classes `Main` passam `-Dstdout.encoding=UTF-8` via `run.sh`.
> Ao rodar `java` direto, inclua esse parâmetro se o console cortar os acentos.

### Observando as threads no SO (Parte C)

Enquanto a Parte C está rodando, em **outro terminal**:

```bash
ps -o nlwp= -p <PID>          # Linux: nº de threads (LWPs) do processo
cat /proc/<PID>/status | grep Threads   # Linux: idem, via /proc
# Windows: Gerenciador de Tarefas → Detalhes → coluna "Threads"
```

O `<PID>` é impresso pela própria Parte C ao iniciar.

## Resultados medidos (resumo)

Medições feitas neste ambiente (OpenJDK 21, Linux). Números variam conforme a máquina —
o que importa é a **ordem de grandeza** e a comparação entre as abordagens.

| Parte | Abordagem | Carga | Tempo total | Observação |
|-------|-----------|-------|-------------|------------|
| A | `extends Thread` | 5 clientes | ~1,0 s | atendimentos em paralelo |
| B | `implements Runnable` | 5 clientes | ~1,0 s | idem, sem prender a herança |
| C | threads de plataforma | 10.000 | ~5,3 s | custo de criação visível; OOM sob limite de recursos |
| D | `newFixedThreadPool(4)` | 10 clientes | ~3,0 s | 3 ondas de 4/4/2 threads |
| D | `newCachedThreadPool()` | 10 clientes | ~1,0 s | ~10 threads criadas sob demanda |
| E | Virtual Threads | 100.000 | ~2,4 s | 10× a carga da Parte C, sem OOM |

Análise completa e respostas às perguntas: **[`RELATORIO.md`](RELATORIO.md)**.
