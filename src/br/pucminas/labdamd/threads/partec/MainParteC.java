package br.pucminas.labdamd.threads.partec;

import br.pucminas.labdamd.threads.comum.Cronometro;
import br.pucminas.labdamd.threads.comum.Guiche;

/**
 * Parte C — o problema de escala: forçamos a criação de muitas
 * <strong>threads nativas do SO</strong> (padrão: 10.000) para sentir, na
 * prática, o custo real do modelo clássico.
 *
 * <p>Cada thread de plataforma reserva tipicamente 512&nbsp;KB–1&nbsp;MB de
 * pilha, além de estruturas do kernel (descritor de thread, entrada de
 * escalonamento). Multiplicado por dezenas de milhares, isso esgota a memória
 * do processo e o SO recusa novas threads com:</p>
 *
 * <pre>OutOfMemoryError: unable to create new native thread</pre>
 *
 * <p><strong>Observando o SO</strong> enquanto o programa dorme:</p>
 * <ul>
 *   <li>Linux: {@code ps -o nlwp <pid>} ou {@code ps -L -p <pid> | wc -l}</li>
 *   <li>Windows: Gerenciador de Tarefas → aba Detalhes → coluna "Threads".</li>
 * </ul>
 *
 * <p>Uso: {@code java ...partec.MainParteC [qtdThreads]} (padrão: 10000).
 * Aumente o valor (ex.: 50000, 100000) para provocar o limite do SO.</p>
 */
public final class MainParteC {

    private static final int QTD_PADRAO = 10_000;

    public static void main(String[] args) throws InterruptedException {
        int total = args.length > 0 ? Integer.parseInt(args[0]) : QTD_PADRAO;

        System.out.println("=== Parte C — muitas threads de SO ===");
        System.out.printf("PID do processo (JVM): %d%n", ProcessHandle.current().pid());
        System.out.printf("Criando %,d threads de plataforma (cada uma dorme ~1s)...%n", total);
        System.out.println("Dica: rode `ps -o nlwp " + ProcessHandle.current().pid()
            + "` em outro terminal para ver o nº de threads do processo.");
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
            // Resultado didático esperado quando 'total' é grande o bastante.
            System.err.printf(
                "OutOfMemoryError após criar %,d threads: %s%n", criadas, erro.getMessage());
            System.err.println(
                "=> O SO não conseguiu criar mais nenhuma thread nativa. Este é "
                + "exatamente o limite que motivou a evolução do modelo (Partes D e E).");
        }

        // join() só nas threads que realmente conseguimos iniciar.
        for (int i = 0; i < criadas; i++) {
            threads[i].join();
        }

        System.out.printf("%nThreads criadas com sucesso: %,d%n", criadas);
        System.out.printf("Tempo total: %s%n", cronometro.decorridoFormatado());
    }

    private MainParteC() {
    }
}
