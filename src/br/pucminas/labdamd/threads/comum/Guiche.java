package br.pucminas.labdamd.threads.comum;

// O "problema" do roteiro e bem simples de proposito: um guiche que atende
// clientes. Atender = esperar 1 segundo. Uso essa espera pra simular uma
// operacao que fica bloqueada (banco, rede, etc), que e o caso onde a thread
// passa o tempo esperando. Deixei essa logica num lugar so pra todas as partes
// (A a E) fazerem exatamente o mesmo "trabalho".
public final class Guiche {

    // quanto dura um atendimento (1 segundo)
    public static final long DURACAO_ATENDIMENTO_MS = 1000;

    private Guiche() {
    }

    // simula o atendimento do cliente: a thread atual fica 1s parada
    public static void atender(int idCliente) {
        atender(idCliente, DURACAO_ATENDIMENTO_MS);
    }

    public static void atender(int idCliente, long duracaoMs) {
        try {
            Thread.sleep(duracaoMs);
        } catch (InterruptedException e) {
            // se interromperem a thread, marco de novo o interrompido em vez de
            // ignorar o erro (assim o resto do programa consegue perceber)
            Thread.currentThread().interrupt();
        }
    }
}
