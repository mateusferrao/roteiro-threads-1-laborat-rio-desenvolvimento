package br.pucminas.labdamd.threads.parteb;

import br.pucminas.labdamd.threads.comum.Cronometro;

/**
 * Executa a Parte B: cria N tarefas {@link AtendimentoRunnable} e as entrega a
 * {@link Thread}s nomeadas no momento da criação
 * ({@code new Thread(tarefa, "Atendente-i")}).
 *
 * <p>Uso: {@code java ...parteb.MainParteB [qtdClientes]} (padrão: 5).</p>
 */
public final class MainParteB {

    private static final int QTD_PADRAO = 5;

    public static void main(String[] args) throws InterruptedException {
        int qtd = args.length > 0 ? Integer.parseInt(args[0]) : QTD_PADRAO;

        System.out.println("=== Parte B — implements Runnable ===");
        System.out.printf("Atendendo %d clientes (cada atendimento leva ~1s)%n%n", qtd);

        Cronometro cronometro = Cronometro.iniciar();

        Thread[] atendentes = new Thread[qtd];
        for (int i = 0; i < qtd; i++) {
            Runnable tarefa = new AtendimentoRunnable(i + 1);
            // A Thread apenas executa a tarefa; o nome é dado na construção.
            atendentes[i] = new Thread(tarefa, "Atendente-" + (i + 1));
        }

        for (Thread t : atendentes) {
            t.start();
        }
        for (Thread t : atendentes) {
            t.join();
        }

        System.out.printf("%nTempo total: %s%n", cronometro.decorridoFormatado());
        System.out.println(
            "Observação: mesmo comportamento paralelo da Parte A (~1s), mas agora "
            + "a classe da tarefa continua livre para herdar de outra classe.");
    }

    private MainParteB() {
    }
}
