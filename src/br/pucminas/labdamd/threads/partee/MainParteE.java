package br.pucminas.labdamd.threads.partee;

import br.pucminas.labdamd.threads.comum.Cronometro;
import br.pucminas.labdamd.threads.comum.Guiche;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

// Parte E - Virtual Threads (Java 21+). Sao gerenciadas pela JVM, nao pelo SO,
// entao da pra criar um monte. Aqui crio 100.000, dez vezes mais que a Parte C,
// e roda numa boa.
//
// Por baixo, poucas threads de SO de verdade (as "carrier threads") executam o
// codigo. Quando uma virtual thread bloqueia (sleep/IO), a JVM tira ela da
// carrier e coloca outra pra rodar. Por isso 100 mil tarefas que ficam so
// esperando cabem em pouquissimas threads reais.
//
// Uso: java ...MainParteE [qtdTarefas]  (padrao 100000)
public final class MainParteE {

    private static final int QTD_PADRAO = 100_000;

    public static void main(String[] args) throws InterruptedException {
        int total = args.length > 0 ? Integer.parseInt(args[0]) : QTD_PADRAO;

        System.out.println("=== Parte E - Virtual Threads (Java 21+) ===");
        System.out.printf("Criando %,d virtual threads (cada uma dorme ~1s)...%n%n", total);

        // so imprimo a primeira tarefa pra nao jogar 100 mil linhas na tela
        AtomicBoolean amostraImpressa = new AtomicBoolean(false);

        Cronometro cronometro = Cronometro.iniciar();

        // try-with-resources: o close() do executor (Java 21) ja espera todas as
        // tarefas terminarem, entao nao preciso de join manual
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < total; i++) {
                int idCliente = i + 1;
                executor.submit(() -> {
                    if (amostraImpressa.compareAndSet(false, true)) {
                        // exercicio: confirmar que e mesmo uma VirtualThread
                        Thread atual = Thread.currentThread();
                        System.out.printf("Amostra -> cliente %d executado por: %s%n",
                            idCliente, atual);
                        System.out.printf("  isVirtual()? %b%n", atual.isVirtual());
                    }
                    Guiche.atender(idCliente);
                });
            }
        } // aqui espera as 100.000 terminarem

        System.out.printf("%n%,d atendimentos concluidos sem OutOfMemoryError.%n", total);
        System.out.printf("Tempo total: %s%n", cronometro.decorridoFormatado());
        System.out.println("Mesmo com 100 mil tarefas o tempo ficou baixo, porque as "
            + "virtual threads que dormem liberam as threads de SO pra rodar as outras.");
    }

    private MainParteE() {
    }
}
