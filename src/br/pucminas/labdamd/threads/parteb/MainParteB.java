package br.pucminas.labdamd.threads.parteb;

import br.pucminas.labdamd.threads.comum.Cronometro;

// Roda a Parte B: cria as tarefas (Runnable) e passa cada uma pra uma Thread,
// ja dando um nome ("Atendente-i") na criacao.
// Uso: java ...MainParteB [qtdClientes]  (padrao 5)
public final class MainParteB {

    private static final int QTD_PADRAO = 5;

    public static void main(String[] args) throws InterruptedException {
        int qtd = args.length > 0 ? Integer.parseInt(args[0]) : QTD_PADRAO;

        System.out.println("=== Parte B - implements Runnable ===");
        System.out.printf("Atendendo %d clientes (cada atendimento leva ~1s)%n%n", qtd);

        Cronometro cronometro = Cronometro.iniciar();

        Thread[] atendentes = new Thread[qtd];
        for (int i = 0; i < qtd; i++) {
            Runnable tarefa = new AtendimentoRunnable(i + 1);
            // a Thread so executa a tarefa; o nome vai no construtor
            atendentes[i] = new Thread(tarefa, "Atendente-" + (i + 1));
        }

        for (Thread t : atendentes) {
            t.start();
        }
        for (Thread t : atendentes) {
            t.join();
        }

        System.out.printf("%nTempo total: %s%n", cronometro.decorridoFormatado());
        System.out.println("Mesmo tempo da Parte A (~1s), mas agora a classe da tarefa "
            + "pode herdar de outra classe.");
    }

    private MainParteB() {
    }
}
