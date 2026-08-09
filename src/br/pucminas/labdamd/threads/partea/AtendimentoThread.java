package br.pucminas.labdamd.threads.partea;

import br.pucminas.labdamd.threads.comum.Guiche;

/**
 * Parte A — forma clássica #1: a própria classe <strong>herda de
 * {@link Thread}</strong> e sobrescreve {@link #run()}.
 *
 * <p><strong>Limitação central (herança única):</strong> em Java uma classe só
 * pode estender <em>uma</em> superclasse. Como {@code AtendimentoThread} já
 * gastou seu "slot" de herança em {@code Thread}, ela nunca poderá herdar de
 * outra classe (por exemplo, um hipotético {@code Funcionario}). É exatamente
 * essa amarração que a Parte B resolve usando {@link Runnable}.</p>
 */
public class AtendimentoThread extends Thread {

    private final int idCliente;

    public AtendimentoThread(int idCliente) {
        this.idCliente = idCliente;
        // setName() ajuda a identificar cada thread nos logs — essencial para
        // depurar concorrência, onde a ordem de execução não é determinística.
        setName("Atendente-" + idCliente);
    }

    /**
     * Código que será executado <em>na nova thread</em> quando (e somente
     * quando) {@link #start()} for chamado.
     */
    @Override
    public void run() {
        System.out.printf("%s atendendo cliente %d%n", getName(), idCliente);
        Guiche.atender(idCliente); // bloqueia ~1s simulando o atendimento
        System.out.printf("%s finalizou o cliente %d%n", getName(), idCliente);
    }
}
