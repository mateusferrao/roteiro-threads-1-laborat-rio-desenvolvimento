package br.pucminas.labdamd.threads.parted;

import br.pucminas.labdamd.threads.comum.Cronometro;
import br.pucminas.labdamd.threads.comum.Guiche;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Parte D — forma atual #1: em vez de criar uma thread de SO por tarefa, um
 * <strong>pool</strong> reaproveita um número fixo de threads reais.
 *
 * <p>Você <em>submete</em> tarefas ({@code pool.submit(...)}) — quem decide como
 * e quando executá-las é o próprio pool. Com {@code newFixedThreadPool(4)},
 * 10 clientes são atendidos por apenas 4 threads reutilizadas: repare que os
 * nomes {@code pool-1-thread-1..4} se repetem entre as tarefas.</p>
 *
 * <p><strong>Exercício de fixação:</strong> passe o argumento {@code --cached}
 * para trocar o pool fixo por {@link Executors#newCachedThreadPool()} e observar
 * a diferença de comportamento (o cached cria threads sob demanda e as reaproveita
 * por até 60s ociosos — com 10 tarefas curtas ele tende a criar ~10 threads e
 * concluir em ~1s, em vez dos ~3s do pool fixo de 4).</p>
 *
 * <p>Uso: {@code java ...parted.MainParteD [--cached] [qtdClientes]}
 * (padrão: pool fixo de 4, 10 clientes).</p>
 */
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

        System.out.println("=== Parte D — ExecutorService (pool de threads) ===");
        System.out.printf("Estratégia: %s | clientes: %d%n%n",
            usarCached ? "newCachedThreadPool()" : "newFixedThreadPool(" + TAMANHO_POOL + ")", qtd);

        Cronometro cronometro = Cronometro.iniciar();

        ExecutorService pool = usarCached
            ? Executors.newCachedThreadPool()
            : Executors.newFixedThreadPool(TAMANHO_POOL);

        for (int i = 0; i < qtd; i++) {
            int idCliente = i + 1; // efetivamente final para uso no lambda
            pool.submit(() -> {
                System.out.printf("%s atendendo cliente %d%n",
                    Thread.currentThread().getName(), idCliente);
                Guiche.atender(idCliente);
            });
        }

        // shutdown(): impede novas submissões e deixa terminar as já aceitas.
        // Sem ele, o pool continuaria vivo esperando tarefas e o programa nunca
        // terminaria (as threads do pool não são daemon).
        pool.shutdown();
        boolean terminou = pool.awaitTermination(1, TimeUnit.MINUTES);
        if (!terminou) {
            System.err.println("Atenção: o pool não terminou dentro do tempo limite.");
        }

        System.out.printf("%nTempo total: %s%n", cronometro.decorridoFormatado());
        if (!usarCached) {
            int ondas = (int) Math.ceil((double) qtd / TAMANHO_POOL);
            System.out.printf(
                "Observação: %d clientes / %d threads = %d ondas de ~1s => ~%ds.%n",
                qtd, TAMANHO_POOL, ondas, ondas);
        }
    }

    private MainParteD() {
    }
}
