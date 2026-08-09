package br.pucminas.labdamd.threads.partea;

import br.pucminas.labdamd.threads.comum.Cronometro;

// Roda a Parte A: cria N atendimentos (threads), da start em todos e espera
// todos terminarem com join.
// Uso: java ...MainParteA [qtdClientes]  (padrao 5)
public final class MainParteA {

    private static final int QTD_PADRAO = 5;

    public static void main(String[] args) throws InterruptedException {
        int qtd = args.length > 0 ? Integer.parseInt(args[0]) : QTD_PADRAO;

        System.out.println("=== Parte A - extends Thread ===");
        System.out.printf("Atendendo %d clientes (cada atendimento leva ~1s)%n%n", qtd);

        Cronometro cronometro = Cronometro.iniciar();

        // cria as threads
        AtendimentoThread[] atendimentos = new AtendimentoThread[qtd];
        for (int i = 0; i < qtd; i++) {
            atendimentos[i] = new AtendimentoThread(i + 1);
        }

        // start() cria a thread nova de verdade. CUIDADO: se eu chamasse run()
        // aqui em vez de start(), ia rodar tudo na main, uma depois da outra (~5s).
        for (AtendimentoThread t : atendimentos) {
            t.start();
        }

        // join() segura a main ate cada thread acabar. Sem isso o programa
        // poderia terminar antes dos atendimentos.
        for (AtendimentoThread t : atendimentos) {
            t.join();
        }

        System.out.printf("%nTempo total: %s%n", cronometro.decorridoFormatado());
        System.out.println("Como rodaram em paralelo, o total ficou perto de 1s, nao de "
            + qtd + "s.");
    }

    private MainParteA() {
    }
}
