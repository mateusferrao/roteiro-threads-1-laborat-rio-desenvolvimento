package br.pucminas.labdamd.threads.partec;

import br.pucminas.labdamd.threads.comum.Cronometro;
import br.pucminas.labdamd.threads.comum.Guiche;

// Parte C - o problema de escala. Aqui eu crio um monte de thread de verdade
// (do SO), padrao 10.000, so pra sentir o custo. Cada thread reserva uma pilha
// de uns 512KB-1MB, entao com muitas threads a memoria acaba e da:
//   OutOfMemoryError: unable to create new native thread
//
// Pra ver quantas threads o processo criou, rode em outro terminal enquanto ele
// dorme:  ps -o nlwp <pid>   (Linux)  ou o Gerenciador de Tarefas (Windows)
//
// Uso: java ...MainParteC [qtdThreads]  (padrao 10000; aumente pra forcar o limite)
public final class MainParteC {

    private static final int QTD_PADRAO = 10_000;

    public static void main(String[] args) throws InterruptedException {
        int total = args.length > 0 ? Integer.parseInt(args[0]) : QTD_PADRAO;

        System.out.println("=== Parte C - muitas threads de SO ===");
        System.out.printf("PID do processo (JVM): %d%n", ProcessHandle.current().pid());
        System.out.printf("Criando %,d threads (cada uma dorme ~1s)...%n", total);
        System.out.println("Dica: rode `ps -o nlwp " + ProcessHandle.current().pid()
            + "` em outro terminal pra ver o nº de threads.");
        System.out.println();

        Cronometro cronometro = Cronometro.iniciar();

        Thread[] threads = new Thread[total];
        int criadas = 0;
        try {
            for (int i = 0; i < total; i++) {
                threads[i] = new Thread(() -> Guiche.atender(0));
                threads[i].start();
                criadas++;
            }
        } catch (OutOfMemoryError erro) {
            // se o SO nao conseguir mais criar thread, aviso quantas deu e sigo
            System.err.printf("OutOfMemoryError depois de criar %,d threads: %s%n",
                criadas, erro.getMessage());
            System.err.println("=> foi o limite de threads do SO. E esse problema que "
                + "levou as Partes D e E.");
        }

        // join so nas que realmente comecaram
        for (int i = 0; i < criadas; i++) {
            threads[i].join();
        }

        System.out.printf("%nThreads criadas com sucesso: %,d%n", criadas);
        System.out.printf("Tempo total: %s%n", cronometro.decorridoFormatado());
    }

    private MainParteC() {
    }
}
