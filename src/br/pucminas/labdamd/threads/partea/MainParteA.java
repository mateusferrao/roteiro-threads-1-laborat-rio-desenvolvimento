package br.pucminas.labdamd.threads.partea;

import br.pucminas.labdamd.threads.comum.Cronometro;

/**
 * Executa a Parte A: cria N atendimentos como threads que herdam de
 * {@link Thread}, inicia todas com {@link Thread#start()} e espera cada uma com
 * {@link Thread#join()}.
 *
 * <p>Uso: {@code java ...partea.MainParteA [qtdClientes]} (padrão: 5).</p>
 */
public final class MainParteA {

    private static final int QTD_PADRAO = 5;

    public static void main(String[] args) throws InterruptedException {
        int qtd = args.length > 0 ? Integer.parseInt(args[0]) : QTD_PADRAO;

        System.out.println("=== Parte A — extends Thread ===");
        System.out.printf("Atendendo %d clientes (cada atendimento leva ~1s)%n%n", qtd);

        Cronometro cronometro = Cronometro.iniciar();

        // 1) Criar as threads.
        AtendimentoThread[] atendimentos = new AtendimentoThread[qtd];
        for (int i = 0; i < qtd; i++) {
            atendimentos[i] = new AtendimentoThread(i + 1);
        }

        // 2) start() cria uma NOVA thread de SO e nela executa run().
        //    ATENÇÃO: chamar run() diretamente NÃO cria thread nenhuma — apenas
        //    executaria o método na thread main, de forma sequencial (~5s).
        for (AtendimentoThread t : atendimentos) {
            t.start(); // nunca t.run()
        }

        // 3) join() bloqueia a main até cada thread terminar. Sem ele, a main
        //    poderia encerrar o programa antes de os atendimentos concluírem.
        for (AtendimentoThread t : atendimentos) {
            t.join();
        }

        System.out.printf("%nTempo total: %s%n", cronometro.decorridoFormatado());
        System.out.println(
            "Observação: com as threads rodando em paralelo, o tempo total fica "
            + "perto de 1s (e não de " + qtd + "s), pois os atendimentos se sobrepõem.");
    }

    private MainParteA() {
    }
}
