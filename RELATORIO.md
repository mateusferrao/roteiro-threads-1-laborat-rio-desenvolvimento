# Relatório — Threads em Java

**PUC Minas · Engenharia de Software · LabDAMD · Unidade 0**
**Roteiro 1 — Revisão de SO e Concorrência**

Este relatório responde às perguntas de cada parte (A–E), resolve os exercícios de
fixação do fechamento e discute criticamente os resultados **efetivamente medidos** neste
ambiente. As medições foram obtidas com **OpenJDK 21 (build 21.0.10)** em Linux; os tempos
absolutos variam conforme a máquina, mas as **ordens de grandeza** e as **relações entre as
abordagens** se mantêm.

> Reprodutibilidade: cada número abaixo pode ser reproduzido com o `run.sh` (ver `README.md`).
> Onde o valor depende da configuração da máquina (ex.: o teto de threads da Parte C), isso é
> dito explicitamente em vez de assumido.

---

## Sumário

- [Revisão conceitual: Processo × Thread](#revisão-conceitual-processo--thread)
- [Revisão conceitual: Ciclo de vida de uma thread](#revisão-conceitual-ciclo-de-vida-de-uma-thread)
- [Parte A — `extends Thread`](#parte-a--extends-thread)
- [Parte B — `implements Runnable`](#parte-b--implements-runnable)
- [Parte C — o problema de escala](#parte-c--o-problema-de-escala)
- [Parte D — `ExecutorService`](#parte-d--executorservice-pool-de-threads)
- [Parte E — Virtual Threads](#parte-e--virtual-threads-java-21)
- [Quadro comparativo](#quadro-comparativo)
- [Exercícios de fixação](#exercícios-de-fixação)
- [Checklist de entrega](#checklist-de-entrega)

---

## Revisão conceitual: Processo × Thread

O SO cria um **processo** para cada programa em execução — no nosso caso, a **JVM**. Toda
thread Java "clássica" (de plataforma) é mapeada **1:1** para uma **thread nativa do SO**.

| Aspecto | Processo | Thread |
|---|---|---|
| **Memória** | Espaço de endereçamento próprio (isolado) | Compartilha a memória do processo pai |
| **Criação** | Cara: nova tabela de páginas, novo PID | Mais leve, mas ainda envolve o kernel (pilha + descritor) |
| **Comunicação** | Precisa de IPC (pipes, sockets, memória compartilhada) | Variáveis compartilhadas (mesmo *heap*) |
| **Isolamento** | Falha em um processo não derruba outro | Exceção não tratada pode afetar o processo todo |
| **Escalonamento** | Unidade de **alocação de recursos** | Unidade que a CPU de fato **escalona** |

**Ponto-chave:** o SO enxerga e escalona *threads*, não "casos de uso". Cada thread roda
**dentro** da memória do processo que a criou. Compartilhar memória é o que torna a
comunicação entre threads barata — e, ao mesmo tempo, o que exige cuidado com
condições de corrida (dois fluxos mexendo no mesmo estado).

> A analogia do guichê, pedida no exercício 3, está detalhada
> [mais adiante](#exercício-3--explicar-processo--thread-com-a-analogia-do-guichê).

---

## Revisão conceitual: Ciclo de vida de uma thread

Uma thread Java transita por estados bem definidos (enum `Thread.State`):

- **NEW** — objeto `Thread` criado, mas `start()` ainda não foi chamado.
- **RUNNABLE** — pronta para executar **ou** executando; quem decide é o escalonador do SO.
- **BLOCKED** — aguardando liberar um *lock* (monitor) para entrar em um bloco `synchronized`.
- **WAITING / TIMED_WAITING** — aguardando um evento: `join()`, `wait()`, ou `sleep()`/espera com prazo.
- **TERMINATED** — `run()` retornou ou lançou uma exceção não tratada.

Entre RUNNABLE e TERMINATED, a thread pode passar por BLOCKED/WAITING **quantas vezes for
preciso**. Entender isso explica por que um programa às vezes "trava": ele está numa espera
(WAITING/TIMED_WAITING) — exatamente o que acontece durante o `sleep()` que simula o
atendimento no guichê. Essa observação é a base de tudo que vem depois: **thread que espera
não usa CPU**, e é justamente por isso que pools e virtual threads conseguem servir muito mais
trabalho do que o número de núcleos.

---

## Parte A — `extends Thread`

**Arquivos:** [`AtendimentoThread.java`](src/br/pucminas/labdamd/threads/partea/AtendimentoThread.java),
[`MainParteA.java`](src/br/pucminas/labdamd/threads/partea/MainParteA.java)

A forma mais antiga (Java 1.0): a classe herda de `Thread` e sobrescreve `run()`.

### Saída medida (5 clientes)

```
Atendente-1 atendendo cliente 1
Atendente-4 atendendo cliente 4
Atendente-2 atendendo cliente 2
Atendente-3 atendendo cliente 3
Atendente-5 atendendo cliente 5
... (finalizações em ordem não determinística) ...
Tempo total: 1010 ms (~1.0 s)
```

### Pergunta — *"O tempo total ficou perto de 1s ou de 5s com 5 atendimentos? Por quê?"*

**Perto de 1 s** (medido: **1010 ms**), e não de 5 s.

As 5 threads são iniciadas quase ao mesmo tempo e **rodam concorrentemente**. Cada uma passa
seu ~1 s **dormindo** (estado TIMED_WAITING) — ou seja, esperando, sem competir por CPU. Os
cinco `sleep(1000)` **se sobrepõem no tempo** em vez de somarem. Se em vez de `start()`
tivéssemos chamado `run()` diretamente, os métodos rodariam **na própria thread `main`, em
sequência**, e o total seria ~5 s.

### Pontos de engenharia observados

- **`start()` × `run()`** — `start()` pede ao SO uma **nova thread** e nela executa `run()`;
  chamar `run()` é uma simples invocação de método na thread atual (nenhuma concorrência). É o
  erro clássico de quem começa com threads.
- **`join()`** — a `main` chama `join()` em cada thread para **não encerrar o programa antes**
  de os atendimentos terminarem. Sem ele, a JVM poderia sair (ou seguir adiante) enquanto as
  threads ainda trabalham.
- **`setName()`** — nomear a thread (`Atendente-i`) torna os logs legíveis; em concorrência,
  onde a ordem não é determinística, isso é essencial para depurar.
- **Ordem não determinística** — repare que a ordem de impressão muda entre execuções. Não se
  deve **nunca** depender da ordem de escalonamento.

### Crítica / limitação

`AtendimentoThread` **gastou seu único slot de herança** em `Thread`. Se ela precisasse
herdar de, digamos, `Funcionario`, esta abordagem **não funcionaria** — Java não tem herança
múltipla de classes. É exatamente o que a Parte B resolve.

---

## Parte B — `implements Runnable`

**Arquivos:** [`AtendimentoRunnable.java`](src/br/pucminas/labdamd/threads/parteb/AtendimentoRunnable.java),
[`MainParteB.java`](src/br/pucminas/labdamd/threads/parteb/MainParteB.java)

Separa **a tarefa** (`Runnable` = *o que* executar) de **quem executa** (`Thread`).

### Saída medida (5 clientes)

```
Atendente-1 atendendo cliente 1
Atendente-2 atendendo cliente 2
...
Tempo total: 1009 ms (~1.0 s)
```

Mesmo comportamento paralelo da Parte A (~1 s) — a diferença está no **design**, não no tempo.

### Pergunta — *"Qual das duas classes (Parte A ou B) você poderia fazer herdar de outra classe hoje?"*

A **classe da Parte B** (`AtendimentoRunnable`). Como ela apenas **implementa** a interface
`Runnable` (e uma classe pode implementar várias interfaces), o "slot" de herança continua
**livre**: `class AtendimentoRunnable extends Funcionario implements Runnable` seria
perfeitamente válido. A `AtendimentoThread` da Parte A não tem essa liberdade, pois já herda
de `Thread`.

### Por que `Runnable` é a forma recomendada (desde o Java 5)

- **Sem herança presa** — a tarefa pode herdar de qualquer classe do domínio.
- **Papéis claros** — a `Thread` *executa* a tarefa; ela não *é* a tarefa. Isso separa
  "unidade de trabalho" de "mecanismo de execução".
- **Reuso e composição** — o mesmo `Runnable` pode ir para várias `Thread`s **ou**, melhor
  ainda, ser **submetido a um `ExecutorService`** (Parte D) sem que a tarefa saiba nada sobre
  como será executada. Essa é a peça que destrava as Partes D e E.

---

## Parte C — o problema de escala

**Arquivo:** [`MainParteC.java`](src/br/pucminas/labdamd/threads/partec/MainParteC.java)

Forçamos a criação de muitas **threads de plataforma** (padrão: 10.000) para *sentir* o custo
do modelo clássico.

### Resultados medidos

| Nº de threads | Resultado |
|---|---|
| 10.000 | criadas com sucesso — **~5,3 s** (o trabalho real é só ~1 s de `sleep`!) |
| 20.000 | criadas com sucesso — **~6,4 s** |
| 500.000 (com `-Xmx256m -Xss1m` e `ulimit -v` restrito) | **`OutOfMemoryError`** após 33 threads |

O `OutOfMemoryError` sob recursos limitados foi de fato reproduzido:

```
[warning][os,thread] Failed to start thread - pthread_create failed (EAGAIN) ...
OutOfMemoryError após criar 33 threads:
  unable to create native thread: possibly out of memory or process/resource limits reached
```

> **Observação honesta sobre o teto:** o "~50 mil" citado no roteiro é uma ordem de grandeza,
> não uma constante. O limite real depende de: tamanho de pilha por thread (`-Xss`), memória
> do processo, e limites do SO (`ulimit -u`, `ulimit -v`, cgroups). Neste ambiente, sem
> restrição artificial, 10 mil e 20 mil threads foram criadas normalmente — o que salta aos
> olhos ali não é o *estouro*, e sim o **tempo de criação**: ~5,3 s para 10 mil threads cujo
> trabalho útil dura apenas ~1 s. Para exibir o estouro de forma determinística, foi preciso
> **restringir os recursos** do processo (comando no `README.md`). O código trata o
> `OutOfMemoryError` graciosamente: registra quantas conseguiu criar e faz `join()` só nessas.

### Observando no SO

Durante a execução (Linux), o número de threads nativas do processo é visível:

```
$ ps -o nlwp= -p <PID>     # ~2.311 threads em um instante da rampa
$ grep Threads /proc/<PID>/status
Threads:  2303
```

Isso comprova que **cada thread Java = uma thread do SO**: o kernel realmente aloca milhares
de LWPs (*light-weight processes*).

### Pergunta — *"Por que criar uma thread de SO é mais caro do que criar um objeto comum em Java?"*

Um objeto comum é só um bloco no **heap** da JVM: alocar é essencialmente "andar um ponteiro",
e o *garbage collector* cuida do resto — nenhuma chamada ao SO. Criar uma **thread de
plataforma** envolve:

1. **Chamada de sistema** (`pthread_create`/`clone`), ou seja, entrar no **kernel**.
2. Reservar uma **pilha** dedicada — tipicamente **512 KB–1 MB** de espaço de endereçamento
   por thread.
3. Criar **estruturas do kernel** (descritor de thread, entrada na fila do **escalonador**).
4. Passar a **concorrer por CPU**, gerando **trocas de contexto** (salvar/restaurar registros,
   invalidar caches/TLB).

Ou seja: objeto = memória no heap gerenciada pela JVM; thread = **recurso do SO**, com custo
de memória *e* de tempo de kernel. A diferença é de **ordens de grandeza**.

### Pergunta — *"O que esse limite sugere sobre usar 1 thread por requisição em um servidor web?"*

Que o modelo **"1 thread de plataforma por requisição" não escala**. Um servidor que aloca uma
thread nativa por conexão esbarra em dois muros muito antes do que se imagina:

- **Memória** — 10 mil conexões × ~1 MB de pilha ≈ 10 GB só de pilhas, fora o resto.
- **Escalonamento** — milhares de threads em sua maioria **bloqueadas esperando I/O** ainda
  custam trocas de contexto e pressão no escalonador.

Como a maior parte do tempo de uma requisição web é **espera** (banco, outro serviço, rede),
teríamos milhares de threads caras **paradas**. Historicamente isso levou a duas saídas:
(1) **pools** de tamanho limitado (Parte D) e (2) programação **assíncrona/reativa** (mais
eficiente, porém mais difícil de escrever e depurar). As **Virtual Threads** (Parte E) trazem
de volta o modelo simples "1 thread por requisição" — só que barato.

---

## Parte D — `ExecutorService` (pool de threads)

**Arquivo:** [`MainParteD.java`](src/br/pucminas/labdamd/threads/parted/MainParteD.java)

Em vez de criar uma thread por tarefa, um **pool** reaproveita um número fixo de threads reais.
Você **submete** (`submit`) tarefas; o pool decide **como e quando** executá-las.

### Saída medida — `newFixedThreadPool(4)`, 10 clientes

```
pool-1-thread-1 atendendo cliente 1
pool-1-thread-4 atendendo cliente 4
pool-1-thread-2 atendendo cliente 2
pool-1-thread-3 atendendo cliente 3
pool-1-thread-2 atendendo cliente 5     <- thread REUTILIZADA
pool-1-thread-4 atendendo cliente 6
...
Tempo total: 3008 ms (~3.0 s)
```

Repare que os nomes `pool-1-thread-1..4` **se repetem** entre as tarefas: são só 4 threads de
SO servindo 10 clientes.

### Pergunta — *"Com 4 threads atendendo 10 clientes, o tempo total ficou perto de 1s, 2s ou 3s?"*

**Perto de 3 s** (medido: **3008 ms**).

Com 4 threads e 10 tarefas de ~1 s cada, o trabalho é feito em **ondas**:
`ceil(10 / 4) = 3` ondas (4 + 4 + 2 clientes). Como as tarefas de cada onda rodam em paralelo
(~1 s por onda), o total fica em **≈ 3 s**. O pool troca **latência por controle de recursos**:
é mais lento que dar uma thread a cada cliente, mas **não deixa a carga explodir** o número de
threads de SO — é o ponto de equilíbrio que o modelo clássico não oferecia.

### Pontos de engenharia observados

- **`shutdown()`** — sinaliza "não aceito mais tarefas"; sem ele, as threads do pool (não
  *daemon*) continuam vivas e **o programa nunca termina**.
- **`awaitTermination(...)`** — bloqueia até o pool esvaziar (ou estourar o prazo). Tratamos o
  retorno `false` (timeout) explicitamente, em vez de ignorá-lo.

---

## Parte E — Virtual Threads (Java 21+)

**Arquivo:** [`MainParteE.java`](src/br/pucminas/labdamd/threads/partee/MainParteE.java)

Threads **gerenciadas pela JVM**, não pelo SO. Dá para criar **milhões** sem esgotar a
memória do processo.

### Saída medida (100.000 tarefas)

```
Amostra -> cliente 2 executado por: VirtualThread[#21]/runnable@ForkJoinPool-1-worker-2
  isVirtual()? true
100,000 atendimentos concluídos sem OutOfMemoryError.
Tempo total: 2377 ms (~2.4 s)
```

**Contraste direto com a Parte C:** 100 mil virtual threads (**10× a carga**) terminaram em
**~2,4 s**, enquanto 10 mil threads de *plataforma* levaram **~5,3 s** só para serem criadas.
O mesmo tipo de experimento que precisou de recursos restritos para *estourar* na Parte C
roda aqui com folga.

### Pergunta — *"Uma Virtual Thread é uma thread de Sistema Operacional? Se não, o que ela é?"*

**Não.** Uma virtual thread **não** é uma thread do SO. Ela é uma thread **leve, gerenciada
pela JVM**: essencialmente uma tarefa cujo estado de execução (a pilha) a JVM guarda no
**heap** e agenda por conta própria.

Como funciona (confirmado pela saída `...@ForkJoinPool-1-worker-2`):

- A JVM mantém um pequeno pool de **carrier threads** — essas, sim, threads de SO reais
  (tipicamente uma por núcleo, sobre um `ForkJoinPool`).
- Uma virtual thread só ocupa uma carrier **enquanto está de fato executando código**.
- Quando ela **bloqueia** (ex.: `sleep`, I/O), a JVM a **desmonta** (*unmount*) da carrier e
  guarda sua pilha no heap; a carrier fica livre para rodar **outra** virtual thread. Quando o
  bloqueio termina, a virtual thread é **remontada** em alguma carrier para continuar.

Por isso 100 mil tarefas que passam o tempo **esperando** cabem em pouquíssimas threads de SO:
o recurso caro (thread nativa) só é usado nos instantes em que há trabalho de CPU a fazer.
Confirmação no código: `Thread.currentThread().isVirtual()` retornou **`true`**, e a
identificação foi `VirtualThread[...]`.

---

## Quadro comparativo

| Critério | `extends Thread` (A) | `implements Runnable` (B) | Pool / `ExecutorService` (D) | Virtual Threads (E) |
|---|---|---|---|---|
| **Desde** | Java 1.0 | Java 1.0 | Java 5 (2004) | Java 21 (2023) |
| **Mapeamento p/ SO** | 1:1 | 1:1 | 1:1 (poucas, reusadas) | M:N (muitas VTs sobre poucas carriers) |
| **Herança da classe** | presa (`Thread`) | livre | livre | livre |
| **Custo por unidade** | alto (thread de SO) | alto (thread de SO) | amortizado (reuso) | baixíssimo (heap) |
| **Escala prática** | milhares | milhares | limitada pelo pool | milhões |
| **Tarefa × executor** | acoplados | separados | separados | separados |
| **Melhor para** | didática / casos pontuais | didática / base p/ pools | CPU-bound; controle de recursos | I/O-bound; alta concorrência |

**Fio condutor da evolução:** cada geração resolveu o problema de escala da anterior **sem
descartar** os conceitos de SO. `Runnable` separou tarefa de executor; o `ExecutorService`
parou de criar uma thread por tarefa; as Virtual Threads tornaram a própria thread barata o
bastante para voltarmos ao modelo simples "1 thread por tarefa".

---

## Exercícios de fixação

### Exercício 1 — Trocar o pool fixo por `newCachedThreadPool()`. O comportamento muda?

**Sim, muda** — comprovado executando `./run.sh d --cached`:

| Estratégia | Threads criadas (10 clientes) | Tempo total |
|---|---|---|
| `newFixedThreadPool(4)` | 4 (reutilizadas em 3 ondas) | **~3,0 s** |
| `newCachedThreadPool()` | ~10 (uma por tarefa, sob demanda) | **~1,0 s** |

Saída com `--cached`:

```
pool-1-thread-1 atendendo cliente 1
pool-1-thread-2 atendendo cliente 2
...
pool-1-thread-10 atendendo cliente 10
Tempo total: 1008 ms (~1.0 s)
```

**Por quê:** o `newCachedThreadPool()` **não tem teto** de threads — ele cria uma nova thread
sempre que chega uma tarefa e não há thread ociosa disponível (e reaproveita, por até 60 s,
as que ficam ociosas). Com 10 tarefas curtas e simultâneas, ele cria ~10 threads e resolve
tudo em **uma onda** (~1 s).

**Análise crítica:** mais rápido aqui **não** significa melhor. O `cached` é ótimo para muitas
tarefas **curtas e esporádicas**, mas é **perigoso sob carga alta**: como não limita o número
de threads, uma enxurrada de tarefas pode fazê-lo criar milhares de threads de SO e cair
exatamente no problema da Parte C (`OutOfMemoryError`). O `fixed` sacrifica latência para
**proteger** o sistema com um teto previsível. A escolha depende do perfil da carga — e, hoje,
para tarefas de I/O, o executor de **virtual threads** costuma ser a melhor resposta para os
dois cenários.

### Exercício 2 — Imprimir `Thread.currentThread()` na Parte E. É uma `VirtualThread`?

**Sim.** A Parte E já imprime uma amostra (para não poluir a saída com 100 mil linhas):

```
Amostra -> cliente 2 executado por: VirtualThread[#21]/runnable@ForkJoinPool-1-worker-2
  isVirtual()? true
```

- A identificação começa com **`VirtualThread`** (e não `Thread`).
- `Thread.currentThread().isVirtual()` retorna **`true`**.
- O sufixo `@ForkJoinPool-1-worker-2` revela a **carrier thread** de SO que estava executando
  a virtual thread naquele instante.

### Exercício 3 — Explicar processo × thread com a analogia do guichê

Imagine uma **agência bancária**:

- A **agência inteira** (prédio, cofre, sistema, energia) é o **processo (a JVM)**. Ela tem
  seus próprios recursos, isolados de outras agências. Abrir uma nova agência é **caro**
  (equivale a criar um processo).
- Cada **guichê de atendimento** é uma **thread**. Todos os guichês ficam **dentro da mesma
  agência** e **compartilham** o mesmo cofre e o mesmo sistema (a memória do processo). Abrir
  mais um guichê é **mais barato** que abrir uma agência nova — mas **não é de graça**: precisa
  de um funcionário, espaço e um terminal (a pilha e o descritor no kernel).
- Vários guichês atendem **em paralelo** (por isso 5 clientes levam ~1 min, não 5) — é a
  concorrência das Partes A e B.
- Como todos usam o **mesmo cofre**, dois guichês mexendo na **mesma conta ao mesmo tempo**
  podem se atrapalhar — é a **condição de corrida**, o preço de compartilhar memória.
- Abrir **um guichê para cada cliente** que chega esgota o espaço da agência — é o limite da
  Parte C. Ter **um número fixo de guichês** com fila é o **pool** da Parte D.
- As **Virtual Threads** seriam guichês "virtuais": há **poucos atendentes reais** (as
  carriers), mas, sempre que um atendimento fica **em espera** (aguardando um documento), o
  atendente **larga aquele cliente e chama outro**, retomando o primeiro quando o documento
  chega. Assim, poucos atendentes servem uma multidão — desde que o gargalo seja **espera**,
  não trabalho de balcão.

### Exercício 4 — Abordagem ideal para um servidor com milhares de conexões (com justificativa)

**Recomendação: um executor de _Virtual Threads_** (`Executors.newVirtualThreadPerTaskExecutor()`),
no modelo "**uma virtual thread por requisição/conexão**".

**Justificativa:**

1. **Perfil da carga é I/O-bound.** Uma requisição web passa a maior parte do tempo
   **esperando** (banco, outros serviços, rede) — estado WAITING, sem usar CPU. As Partes C e
   E mostram exatamente isto: quando o trabalho é espera, virtual threads que bloqueiam
   **liberam** a carrier para outra tarefa, e milhares de conexões cabem em pouquíssimas
   threads de SO. Medimos 100 mil tarefas em ~2,4 s (Parte E) contra o custo já pesado de só
   **criar** 10 mil threads de plataforma (~5,3 s, Parte C).
2. **Simplicidade do código.** Recupera-se o modelo direto e legível "1 thread por requisição"
   (código **síncrono/bloqueante**, fácil de ler e depurar, com *stack traces* completos) sem
   o custo que antes o inviabilizava — e **sem** a complexidade do estilo assíncrono/reativo
   (callbacks, `CompletableFuture` encadeados), que era a alternativa histórica.
3. **Por que não as outras abordagens:**
   - **1 thread de plataforma por conexão (Partes A–C):** não escala — esbarra em memória e
     escalonamento com milhares de conexões (é *o* problema da Parte C).
   - **Pool fixo (Parte D):** protege contra excesso de threads, mas com milhares de conexões
     **I/O-bound** as threads do pool ficam **bloqueadas esperando**, e novas requisições
     **enfileiram** — o *throughput* despenca. Um pool fixo continua adequado para trabalho
     **CPU-bound**, onde o número ideal de threads ≈ número de núcleos.

**Ressalvas honestas:** virtual threads **não** aceleram trabalho **CPU-bound** (o limite ali
é o número de núcleos — use um pool dimensionado para a CPU). Deve-se também evitar **fixar
(pinning)** a carrier em seções longas de código `synchronized`/nativo, e continuar
**limitando recursos externos escassos** (ex.: conexões de banco) com semáforos/pools próprios
— criar a virtual thread é barato, mas o banco de dados por trás **não** é.

---

## Checklist de entrega

- [x] **As 5 partes (A a E) compilam e executam sem erro** — `./run.sh compile` compila tudo;
      cada parte foi executada e teve a saída registrada neste relatório.
- [x] **As perguntas de cada parte estão respondidas no relatório** — Partes A, B, C, D e E,
      cada uma com resposta e os números medidos.
- [x] **Exercícios de fixação resolvidos** — os 4 exercícios do fechamento, incluindo a
      comparação `fixed` × `cached` e a confirmação de `isVirtual()`.
- [x] **Consigo explicar processo × thread sem consultar o roteiro** — analogia do guichê +
      tabela comparativa consolidam o conceito.
- [x] **Escolha justificada da abordagem para um servidor com milhares de conexões** —
      exercício 4, com justificativa e ressalvas.
