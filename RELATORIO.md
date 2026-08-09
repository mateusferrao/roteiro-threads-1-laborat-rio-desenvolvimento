# Relatório - Threads em Java

Laboratório de Threads em Java (Roteiro 1) - PUC Minas, Engenharia de Software.

Aqui eu respondo as perguntas de cada parte do roteiro, faço os exercícios do final e
comento o que observei rodando cada exemplo. Rodei tudo no meu ambiente com o OpenJDK 21
no Linux, então os tempos que aparecem aqui são os que deram na minha máquina - em outro
computador os números mudam um pouco, mas a comparação entre as abordagens continua igual.

Para rodar é só usar o `run.sh` (expliquei no README).

## Antes das partes: processo x thread

Só pra fixar o que a gente revisou no começo da aula, porque isso é a base de tudo:

O sistema operacional cria um **processo** pra cada programa - no nosso caso, a JVM. Cada
processo tem seu próprio espaço de memória, separado dos outros. Criar um processo é caro
(o SO tem que montar uma porção de estrutura nova). Já a **thread** é um fluxo de execução
que roda dentro do processo e divide a memória com as outras threads dele. Por isso criar
thread é mais barato que criar processo, e por isso duas threads conseguem trocar dados só
usando uma variável em comum (enquanto dois processos precisariam de algo mais elaborado,
tipo pipe ou socket).

Uma coisa que achei importante: quem o SO enxerga e coloca pra rodar na CPU é a thread, não
o processo. O processo é mais a "caixa" que junta os recursos; a thread é o que de fato é
escalonado. E como as threads dividem memória, se uma bagunça alguma coisa que a outra tá
usando ao mesmo tempo, dá problema (condição de corrida) - é o preço de compartilhar tudo.

(A analogia do guichê que ajuda a entender isso eu deixei lá no exercício 3.)

## Ciclo de vida da thread

Toda thread passa por alguns estados até terminar. Os principais são:

- **NEW** - criei o objeto Thread mas ainda não chamei `start()`.
- **RUNNABLE** - tá pronta ou rodando (quem decide de fato é o escalonador do SO).
- **BLOCKED** - travada esperando liberar um lock pra entrar num bloco `synchronized`.
- **WAITING / TIMED_WAITING** - esperando alguma coisa: um `join()`, um `wait()`, ou um
  `sleep()` com tempo.
- **TERMINATED** - o `run()` acabou (ou estourou uma exceção).

O detalhe que faz diferença: quando a thread tá dormindo/esperando (TIMED_WAITING), ela
**não usa CPU**. Isso parece bobo mas é o que explica as partes D e E - dá pra ter um monte
de thread "parada" esperando I/O sem gastar processador, e é aí que os pools e as virtual
threads ganham.

---

## Parte A - extends Thread

Aqui a classe herda de `Thread` e a gente sobrescreve o `run()`. Criei 5 atendimentos, dei
`start()` em cada um e depois `join()` pra esperar todos.

Saída (resumida) rodando com 5 clientes:

```
Atendente-1 atendendo cliente 1
Atendente-4 atendendo cliente 4
Atendente-2 atendendo cliente 2
Atendente-3 atendendo cliente 3
Atendente-5 atendendo cliente 5
...
Tempo total: 1010 ms (~1.0 s)
```

**Pergunta: o tempo total ficou perto de 1s ou de 5s com 5 atendimentos? Por quê?**

Ficou perto de **1 segundo** (deu 1010 ms). Como as 5 threads começam quase juntas e cada
uma só fica 1s dormindo (simulando o atendimento), os tempos se sobrepõem em vez de somar.
Se fossem executadas uma depois da outra daria uns 5s, mas rodando em paralelo dá ~1s.

Uma coisa que reparei e que é fácil de errar: tem que chamar `start()`, e **não** `run()`.
Se eu chamar `run()` direto, ele não cria thread nenhuma - só executa o método ali mesmo, na
thread main, uma depois da outra (aí sim daria 5s). O `start()` é que pede pro SO uma thread
nova. Também usei `setName()` pra dar nome ("Atendente-1", etc), porque senão fica difícil
saber quem é quem no log - e repara que a ordem das linhas muda toda vez que roda, então não
dá pra confiar na ordem.

O problema dessa forma é a herança: como a classe já herdou de `Thread`, ela não pode herdar
de mais nada. Se eu precisasse que o atendimento herdasse de uma classe `Funcionario`, por
exemplo, já era. É justamente isso que a Parte B resolve.

---

## Parte B - implements Runnable

Mesma ideia, mas agora a tarefa implementa `Runnable` e eu passo ela pra uma `Thread`. Deu o
mesmo tempo (~1s), a diferença é de organização mesmo.

```
Atendente-1 atendendo cliente 1
Atendente-2 atendendo cliente 2
...
Tempo total: 1009 ms (~1.0 s)
```

**Pergunta: qual das duas classes (Parte A ou B) você poderia fazer herdar de outra classe hoje?**

A da **Parte B**. Como ela só *implementa* `Runnable` (e dá pra implementar várias interfaces
sem problema), ela continua livre pra herdar de outra classe. Daria pra escrever
`class AtendimentoRunnable extends Funcionario implements Runnable` numa boa. Já a da Parte A
não, porque o "slot" de herança dela já foi gasto no `Thread`.

Por isso desde o Java 5 recomendam usar `Runnable` em vez de estender `Thread` direto: separa
a tarefa (o que fazer) de quem executa (a Thread), a classe fica livre pra herdar de outra
coisa, e dá pra reaproveitar a mesma tarefa. E, o mais útil, é essa separação que deixa a
gente jogar a tarefa num pool depois (Parte D) sem ela precisar saber como vai ser executada.

---

## Parte C - muitas threads (o problema de escala)

Aqui a ideia é criar um monte de thread de verdade (do SO) pra sentir o custo. O padrão é
10.000.

O que deu na minha máquina:

| Quantidade | Resultado |
|---|---|
| 10.000 threads | criou todas, mas levou **~5,3 s** |
| 20.000 threads | criou todas, **~6,4 s** |
| 500.000 (limitando a memória do processo de propósito) | estourou: `OutOfMemoryError` |

O que me chamou atenção não foi nem o estouro, foi o **tempo**: 5,3 segundos só pra criar as
threads, sendo que o trabalho de cada uma é dormir 1s. Ou seja, quase todo esse tempo foi
gasto criando thread, não fazendo o "atendimento".

O roteiro fala que acima de uns 50 mil costuma dar `OutOfMemoryError: unable to create new
native thread`. No meu ambiente, sem mexer em nada, 10 mil e até 20 mil criaram normal - o
limite exato depende da máquina (memória, tamanho da pilha, limites do SO). Pra ver o erro
acontecendo de fato eu tive que apertar os recursos do processo de propósito (limitei a
memória virtual), e aí sim ele estourou depois de poucas threads:

```
Failed to start thread - pthread_create failed (EAGAIN) ...
OutOfMemoryError após criar 33 threads:
  unable to create native thread: possibly out of memory or process/resource limits reached
```

Fiz o programa tratar esse erro em vez de deixar quebrar feio: ele avisa quantas conseguiu
criar e segue.

Pra confirmar que cada thread Java vira mesmo uma thread do SO, enquanto rodava eu olhei em
outro terminal com `ps -o nlwp= -p <PID>` (e também no `/proc/<PID>/status`), e o número de
threads do processo estava lá na casa dos milhares. Dá pra ver a mesma coisa no Gerenciador
de Tarefas no Windows.

**Pergunta: por que criar uma thread de SO é mais caro do que criar um objeto comum em Java?**

Um objeto comum é só um pedaço de memória no heap da JVM - alocar é rápido e o garbage
collector cuida de limpar depois, sem precisar falar com o sistema operacional. Já criar uma
thread de verdade envolve pedir pro SO (uma chamada de sistema), reservar uma pilha só pra
ela (uns 512KB a 1MB), o kernel montar as estruturas de controle dela, e ainda por cima ela
passa a disputar CPU, o que gera troca de contexto. É bem mais coisa - por isso é caro tanto
em memória quanto em tempo.

**Pergunta: o que esse limite sugere sobre usar 1 thread por requisição em um servidor web?**

Que não dá pra fazer isso com thread de SO se o servidor tiver muitas conexões. Se cada
requisição criar uma thread, com 10 mil conexões seriam 10 mil pilhas de ~1MB (uns 10GB só
de pilha!) e um monte de thread na maioria das vezes **parada esperando** banco ou rede,
mas ainda custando pro escalonador. Como requisição web é quase tudo espera, você teria um
monte de recurso caro sem fazer nada. Foi esse problema que levou aos pools (Parte D) e
depois às virtual threads (Parte E).

---

## Parte D - ExecutorService (pool de threads)

Em vez de criar uma thread por tarefa, aqui a gente cria um pool com um número fixo de
threads e vai *submetendo* as tarefas. O pool reaproveita as mesmas threads. Testei com pool
de 4 e 10 clientes.

```
pool-1-thread-1 atendendo cliente 1
pool-1-thread-4 atendendo cliente 4
pool-1-thread-2 atendendo cliente 2
pool-1-thread-3 atendendo cliente 3
pool-1-thread-2 atendendo cliente 5     <- a thread 2 pegou outro cliente
pool-1-thread-4 atendendo cliente 6
...
Tempo total: 3008 ms (~3.0 s)
```

Repara que os nomes `pool-1-thread-1` até `4` se repetem: são só 4 threads atendendo os 10
clientes.

**Pergunta: com 4 threads atendendo 10 clientes, o tempo total ficou perto de 1s, 2s ou 3s?**

Perto de **3 segundos** (deu 3008 ms). Faz sentido: com 4 threads e 10 tarefas de 1s, dá pra
fazer em 3 "levas" - 4 clientes, mais 4, mais os 2 que sobraram. Cada leva leva ~1s, então
~3s no total. É mais lento do que dar uma thread pra cada um (que daria 1s), mas em troca
você não deixa o número de threads explodir. É esse o ponto do pool: controlar quantas
threads existem.

Dois detalhes que aprendi aqui: precisa chamar `shutdown()` no final, senão as threads do
pool ficam vivas esperando mais tarefa e o programa nunca fecha. E o `awaitTermination()`
segura o programa até o pool terminar tudo.

---

## Parte E - Virtual Threads (Java 21+)

Essa é a parte nova. As virtual threads são gerenciadas pela própria JVM, não pelo SO, então
dá pra criar um montão delas. Criei 100.000 (dez vezes mais que a Parte C).

```
Amostra -> cliente 2 executado por: VirtualThread[#21]/runnable@ForkJoinPool-1-worker-2
  isVirtual()? true
100.000 atendimentos concluídos sem OutOfMemoryError.
Tempo total: 2377 ms (~2.4 s)
```

Isso me impressionou: 100 mil virtual threads rodaram em ~2,4s, enquanto lá na Parte C só
**criar** 10 mil threads normais já tinha levado 5,3s. Dez vezes mais tarefas, em menos
tempo, e sem estourar memória.

**Pergunta: uma Virtual Thread é uma thread de Sistema Operacional? Se não, o que ela é?**

Não é. A virtual thread é uma thread "leve" que a JVM gerencia por conta própria. Por baixo
existem poucas threads de SO de verdade (chamam de *carrier threads*, dá pra ver no meu
resultado o `ForkJoinPool-1-worker-2`), normalmente uma por núcleo. O truque é: quando uma
virtual thread bloqueia (tipo no `sleep` ou esperando I/O), a JVM tira ela da carrier e
guarda o estado dela, deixando a carrier livre pra rodar outra virtual thread. Quando o
bloqueio acaba, ela volta a rodar em alguma carrier. Por isso 100 mil tarefas que passam o
tempo esperando cabem em pouquíssimas threads reais - a thread de SO, que é o recurso caro,
só é usada quando tem realmente código pra rodar. Confirmei com `isVirtual()`, que deu
`true`, e o nome apareceu como `VirtualThread`.

---

## Resumão das abordagens

Juntando tudo numa tabela pra comparar:

| | extends Thread (A) | Runnable (B) | Pool (D) | Virtual Threads (E) |
|---|---|---|---|---|
| Desde | Java 1.0 | Java 1.0 | Java 5 | Java 21 |
| Vira thread do SO? | sim, 1 pra 1 | sim, 1 pra 1 | sim, mas poucas e reusadas | não (várias sobre poucas carriers) |
| A classe pode herdar de outra? | não | sim | sim | sim |
| Custo | alto | alto | diluído (reusa) | baixíssimo |
| Escala | milhares | milhares | limitada pelo pool | milhões |
| Boa pra | aprender | aprender / base pro pool | tarefa de CPU | muita conexão / I/O |

A sacada é que cada versão resolveu o problema da anterior: o `Runnable` separou tarefa de
executor, o pool parou de criar uma thread por tarefa, e as virtual threads deixaram a thread
tão barata que dá pra voltar ao esquema simples de "uma thread por tarefa" sem medo.

---

## Exercícios do final

### 1. Trocar o pool fixo por newCachedThreadPool() - muda o comportamento?

Muda sim. Rodei os dois (é só passar `--cached` no meu programa):

| Pool | Threads criadas (10 clientes) | Tempo |
|---|---|---|
| newFixedThreadPool(4) | 4 (reusadas em 3 levas) | ~3,0 s |
| newCachedThreadPool() | ~10 (uma por tarefa) | ~1,0 s |

Com o cached deu 1s porque ele não tem limite de threads: chega tarefa e não tem thread
livre, ele cria uma na hora. Como as 10 tarefas chegaram juntas, ele criou ~10 threads e
resolveu tudo de uma vez.

Só que rápido aqui não quer dizer melhor. O cached é bom pra tarefas curtas e que aparecem
de vez em quando, mas é perigoso se chegar muita coisa de uma vez - como ele não limita, pode
acabar criando milhares de threads e cair no mesmo problema da Parte C (estourar). O fixo é
mais lento mas segura o número de threads, que é mais seguro. Depende do caso.

### 2. Imprimir Thread.currentThread() na Parte E - é uma VirtualThread?

É sim. Eu já deixei o programa imprimir uma amostra (não imprimi as 100 mil pra não poluir):

```
Amostra -> cliente 2 executado por: VirtualThread[#21]/runnable@ForkJoinPool-1-worker-2
  isVirtual()? true
```

Aparece como `VirtualThread`, o `isVirtual()` deu `true`, e ainda dá pra ver a carrier thread
que tava executando ela (o `ForkJoinPool-1-worker-2`).

### 3. Explicar processo x thread com a analogia do guichê

Pensa numa agência de banco:

- A **agência inteira** (o prédio, o cofre, o sistema) é o **processo (a JVM)**. Ela tem os
  recursos dela, separados das outras agências. Abrir uma agência nova é caro - isso é criar
  um processo.
- Cada **guichê** é uma **thread**. Os guichês ficam todos dentro da mesma agência e usam o
  mesmo cofre e o mesmo sistema (a memória compartilhada). Abrir mais um guichê é mais barato
  que abrir uma agência, mas não é de graça: precisa de um funcionário e um terminal.
- Vários guichês atendem ao mesmo tempo, por isso 5 clientes levam ~1 minuto e não 5 (é a
  concorrência das partes A e B).
- Como todos usam o mesmo cofre, se dois guichês mexerem na mesma conta ao mesmo tempo pode
  dar confusão - é a condição de corrida, o preço de compartilhar a memória.
- Abrir um guichê pra cada cliente que entra esgota a agência - é o limite da Parte C. Ter um
  número fixo de guichês com uma fila é o pool da Parte D.
- As virtual threads seriam tipo guichês virtuais: tem poucos atendentes de verdade, mas
  sempre que um atendimento fica esperando (o cliente foi buscar um documento), o atendente
  larga ele e chama outro, voltando depois. Assim poucos atendentes dão conta de muita gente
  - desde que a maior parte do tempo seja espera.

### 4. Melhor abordagem pra um servidor com milhares de conexões (com justificativa)

Eu iria de **virtual threads**, uma pra cada conexão (`newVirtualThreadPerTaskExecutor()`).

Motivo: servidor web fica quase o tempo todo esperando (banco, outro serviço, rede), então é
o cenário perfeito pra virtual thread - a thread que espera libera a carrier pra outra, e dá
pra ter milhares de conexões usando pouquíssimas threads de SO. Foi o que eu vi comparando a
Parte C com a E: 100 mil virtual threads correram numa boa, enquanto só criar 10 mil threads
normais já era pesado.

E tem a vantagem de o código ficar simples: você escreve do jeito normal, bloqueante, fácil
de ler e de debugar, sem precisar do esquema assíncrono cheio de callback que era a
alternativa antes.

As outras não encaixam tão bem aqui: uma thread de SO por conexão não escala (é o problema da
Parte C), e o pool fixo, com um monte de conexão esperando I/O, ia ficar com as threads todas
travadas esperando e as outras requisições na fila. (O pool fixo continua ótimo, mas pra
trabalho pesado de CPU, onde o número de threads perto do número de núcleos já resolve.)

Uma ressalva: virtual thread não deixa cálculo de CPU mais rápido (aí o limite é o número de
núcleos mesmo), e mesmo usando virtual thread ainda vale limitar recursos que são poucos, tipo
conexão com o banco - criar a virtual thread é barato, mas o banco atrás não é.

---

## Checklist

- [x] As 5 partes (A a E) compilam e rodam sem erro
- [x] As perguntas de cada parte estão respondidas
- [x] Os exercícios do final estão feitos
- [x] Consigo explicar processo x thread sem olhar o roteiro (analogia do guichê + tabela)
- [x] Escolhi e justifiquei a abordagem pra um servidor com muitas conexões
