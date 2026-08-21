# Roteiro de Threads em Java

Roteiro 1 de laboratório (Threads em Java) - PUC Minas, Engenharia de Software.

São as 5 partes do roteiro (A a E) feitas em Java 21. O "problema" é bem simples de
propósito, um guichê de atendimento, porque o foco é a parte de concorrência e não a lógica.

As respostas das perguntas de cada parte e os exercícios estão no [`RELATORIO.md`](RELATORIO.md).

> **Roteiro 2 (Lab de Redes)** está na pasta [`lab-redes/`](lab-redes/) — TCP, UDP, Multicast e
> WebSocket em Java e Python, com seu próprio README e RESPOSTAS.
>
> **Roteiro 3 (Transparências e gRPC)** está na pasta [`lab-grpc/`](lab-grpc/) — contrato `.proto`
> e RPC unário + streaming em Java e Python, também com README e RESPOSTAS próprios.

## O que precisa pra rodar

- JDK 21 ou mais novo (a Parte E usa Virtual Threads, que só tem do Java 21 pra frente).
  Confere com `java -version`.
- Um terminal com bash pra usar o `run.sh` (ou dá pra chamar `javac`/`java` na mão).

Não usei Maven nem Gradle nem nenhuma biblioteca externa, só o JDK.

## Organização

```
src/br/pucminas/labdamd/threads/
├── comum/          -> Cronometro (mede o tempo) e Guiche (o "atender" que dorme 1s)
├── partea/         -> Parte A: extends Thread
├── parteb/         -> Parte B: implements Runnable
├── partec/         -> Parte C: muitas threads de SO (o limite)
├── parted/         -> Parte D: ExecutorService (pool)
└── partee/         -> Parte E: Virtual Threads
```

## Como rodar

Com o `run.sh` (ele compila em `./out` e executa):

```bash
./run.sh compile      # compila tudo
./run.sh a            # Parte A (5 clientes)
./run.sh b            # Parte B (5 clientes)
./run.sh c            # Parte C (10.000 threads)
./run.sh c 20000      # Parte C com outra quantidade
./run.sh d            # Parte D (pool fixo de 4, 10 clientes)
./run.sh d --cached   # Parte D usando newCachedThreadPool() (exercício 1)
./run.sh e            # Parte E (100.000 virtual threads)
./run.sh all          # roda A, B e D em seguida
./run.sh clean        # apaga o ./out
```

Se quiser rodar sem o script:

```bash
mkdir -p out && find src -name '*.java' | xargs javac -d out
java -Dstdout.encoding=UTF-8 -cp out br.pucminas.labdamd.threads.partea.MainParteA
```

(o `-Dstdout.encoding=UTF-8` é só pra não cortar os acentos quando a saída é redirecionada)

## Vendo as threads no SO (Parte C)

Enquanto a Parte C tá rodando, em outro terminal dá pra ver quantas threads o processo criou
(o PID ele imprime na tela):

```bash
ps -o nlwp= -p <PID>              # Linux
cat /proc/<PID>/status | grep Threads
# no Windows: Gerenciador de Tarefas > Detalhes > coluna Threads
```

## Tempos que deram na minha máquina

Rodei tudo no OpenJDK 21 no Linux. Em outra máquina os números mudam, mas a comparação é a
mesma:

| Parte | Abordagem | Carga | Tempo |
|-------|-----------|-------|-------|
| A | extends Thread | 5 clientes | ~1,0 s |
| B | implements Runnable | 5 clientes | ~1,0 s |
| C | threads de SO | 10.000 | ~5,3 s |
| D | pool fixo de 4 | 10 clientes | ~3,0 s |
| D | cached | 10 clientes | ~1,0 s |
| E | virtual threads | 100.000 | ~2,4 s |

O comentário sobre cada um tá no [`RELATORIO.md`](RELATORIO.md).
