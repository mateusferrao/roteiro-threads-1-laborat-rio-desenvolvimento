package br.pucminas.labdamd.threads.partee;

import br.pucminas.labdamd.threads.comum.Cronometro;
import br.pucminas.labdamd.threads.comum.Guiche;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Parte E — forma atual #2 (norma atual): <strong>Virtual Threads</strong>
 * (Java 21+), gerenciadas pela própria JVM e não pelo SO. É possível criar
 * milhões delas sem esgotar a memória do processo.
 *
 * <p>O mesmo experimento da Parte C — que quebrava com dezenas de milhares de
 * threads de plataforma — roda tranquilamente aqui com 100.000 tarefas.</p>
 *
 * <p><strong>Como funciona (carrier threads):</strong> por baixo, um pequeno
 * pool de threads de SO reais (as <em>carrier threads</em>, tipicamente uma por
 * núcleo) executa o código das virtual threads. Quando uma virtual thread
 * bloqueia (ex.: no {@code sleep}/I/O), ela é <em>desmontada</em> da carrier,
 * que fica livre para rodar outra virtual thread. Por isso 100 mil tarefas que
 * passam o tempo esperando cabem em pouquíssimas threads de SO.</p>
 *
 * <p><strong>Exercício de fixação:</strong> imprimimos
 * {@code Thread.currentThread()} de uma amostra para confirmar que se trata de
 * uma {@code VirtualThread}, e também qual é a <em>carrier</em> que a executa.</p>
 *
 * <p>Uso: {@code java ...partee.MainParteE [qtdTarefas]} (padrão: 100000).</p>
 */
public final class MainParteE {

    private static final int QTD_PADRAO = 100_000;

    public static void main(String[] args) throws InterruptedException {
        int total = args.length > 0 ? Integer.parseInt(args[0]) : QTD_PADRAO;

        System.out.println("=== Parte E — Virtual Threads (Java 21+) ===");
        System.out.printf("Criando %,d virtual threads (cada uma dorme ~1s)...%n%n", total);

        // Imprime apenas a primeira tarefa, para não poluir a saída com 100 mil linhas.
        AtomicBoolean amostraImpressa = new AtomicBoolean(false);

        Cronometro cronometro = Cronometro.iniciar();

        // try-with-resources: o close() do executor (Java 21) aguarda todas as
        // tarefas terminarem antes de prosseguir — não é preciso join manual.
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < total; i++) {
                int idCliente = i + 1;
                executor.submit(() -> {
                    if (amostraImpressa.compareAndSet(false, true)) {
                        Thread atual = Thread.currentThread();
                        System.out.printf("Amostra -> cliente %d executado por: %s%n",
                            idCliente, atual);
                        System.out.printf("  isVirtual()? %b%n", atual.isVirtual());
                    }
                    Guiche.atender(idCliente);
                });
            }
        } // aqui a JVM aguarda as 100.000 tarefas concluírem

        System.out.printf("%n%,d atendimentos concluídos sem OutOfMemoryError.%n", total);
        System.out.printf("Tempo total: %s%n", cronometro.decorridoFormatado());
        System.out.println(
            "Observação: mesmo com 100 mil tarefas, o tempo total fica na casa de "
            + "poucos segundos, pois as virtual threads que dormem liberam as "
            + "carrier threads de SO para executar as demais.");
    }

    private MainParteE() {
    }
}
