package br.pucminas.labdamd.threads.parted;

import br.pucminas.labdamd.threads.comum.Cronometro;
import br.pucminas.labdamd.threads.comum.Guiche;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// Parte D - em vez de criar uma thread por tarefa, uso um pool com um numero
// fixo de threads e vou submetendo as tarefas. O pool reaproveita as threads
// (repare que os nomes pool-1-thread-1..4 se repetem).
//
// Exercicio do final: passar --cached troca o pool fixo por um cached, que cria
// thread por demanda (com 10 tarefas curtas ele cria ~10 e termina em ~1s, em
// vez dos ~3s do fixo de 4).
//
// Uso: java ...MainParteD [--cached] [qtdClientes]  (padrao: fixo de 4, 10 clientes)
public final class MainParteD {

    private static final int TAMANHO_POOL = 4;
    private static final int QTD_PADRAO = 10;

    public static void main(String[] args) throws InterruptedException {
        boolean usarCached = false;
        int qtd = QTD_PADRAO;
        for (String arg : args) {
            if ("--cached".equals(arg)) {
                usarCached = true;
            } else {
                qtd = Integer.parseInt(arg);
            }
        }

        System.out.println("=== Parte D - ExecutorService (pool de threads) ===");
        System.out.printf("Pool: %s | clientes: %d%n%n",
            usarCached ? "newCachedThreadPool()" : "newFixedThreadPool(" + TAMANHO_POOL + ")", qtd);

        Cronometro cronometro = Cronometro.iniciar();

        ExecutorService pool = usarCached
            ? Executors.newCachedThreadPool()
            : Executors.newFixedThreadPool(TAMANHO_POOL);

        for (int i = 0; i < qtd; i++) {
            int idCliente = i + 1; // precisa ser final pra usar no lambda
            pool.submit(() -> {
                System.out.printf("%s atendendo cliente %d%n",
                    Thread.currentThread().getName(), idCliente);
                Guiche.atender(idCliente);
            });
        }

        // shutdown(): nao aceita mais tarefa e deixa terminar as que ja entraram.
        // Sem isso o programa nao fecha (as threads do pool ficam esperando tarefa).
        pool.shutdown();
        boolean terminou = pool.awaitTermination(1, TimeUnit.MINUTES);
        if (!terminou) {
            System.err.println("Atencao: o pool nao terminou no tempo limite.");
        }

        System.out.printf("%nTempo total: %s%n", cronometro.decorridoFormatado());
        if (!usarCached) {
            int ondas = (int) Math.ceil((double) qtd / TAMANHO_POOL);
            System.out.printf("%d clientes / %d threads = %d ondas de ~1s => ~%ds.%n",
                qtd, TAMANHO_POOL, ondas, ondas);
        }
    }

    private MainParteD() {
    }
}
