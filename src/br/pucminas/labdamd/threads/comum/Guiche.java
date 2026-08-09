package br.pucminas.labdamd.threads.comum;

import java.time.Duration;

/**
 * Domínio (propositalmente simples) usado em todo o roteiro: o
 * <strong>guichê de atendimento</strong>.
 *
 * <p>O foco do laboratório é o <em>mecanismo de concorrência</em>, não a lógica
 * de negócio. Por isso "atender um cliente" é modelado como uma espera de
 * duração fixa ({@link Thread#sleep}), que representa uma operação
 * <em>bloqueante</em> típica do mundo real (I/O de rede, consulta a banco,
 * chamada a outro serviço) — justamente o cenário em que threads passam a
 * maior parte do tempo esperando, e não usando CPU.</p>
 *
 * <p>Manter a "regra de negócio" em um único lugar garante que as cinco partes
 * (A–E) comparem exatamente o mesmo trabalho, mudando apenas a forma de
 * concorrência.</p>
 */
public final class Guiche {

    /** Duração padrão de um atendimento no guichê. */
    public static final Duration DURACAO_ATENDIMENTO = Duration.ofSeconds(1);

    private Guiche() {
        // classe utilitária: não deve ser instanciada
    }

    /**
     * Simula o atendimento de um cliente pela thread corrente, bloqueando-a
     * pela duração padrão.
     *
     * @param idCliente identificador do cliente atendido
     */
    public static void atender(int idCliente) {
        atender(idCliente, DURACAO_ATENDIMENTO);
    }

    /**
     * Simula o atendimento de um cliente pela thread corrente, bloqueando-a
     * pela duração informada.
     *
     * <p><strong>Boa prática:</strong> se a thread for interrompida durante o
     * {@code sleep}, o método <em>restaura</em> o <em>interrupt flag</em>
     * (chamando {@link Thread#interrupt()} novamente) em vez de engolir a
     * {@link InterruptedException}. Assim o resto do sistema ainda consegue
     * perceber o pedido de cancelamento.</p>
     *
     * @param idCliente identificador do cliente atendido
     * @param duracao   quanto tempo o atendimento leva
     */
    public static void atender(int idCliente, Duration duracao) {
        try {
            Thread.sleep(duracao.toMillis());
        } catch (InterruptedException e) {
            // Não engolir a exceção: restaurar o sinal de interrupção.
            Thread.currentThread().interrupt();
        }
    }
}
