package br.pucminas.labdamd.threads.parteb;

import br.pucminas.labdamd.threads.comum.Guiche;

/**
 * Parte B — forma clássica #2: a tarefa implementa {@link Runnable}, separando
 * <strong>o que executar</strong> (a tarefa) de <strong>quem executa</strong>
 * (a {@link Thread}).
 *
 * <p><strong>Vantagens sobre a Parte A:</strong></p>
 * <ul>
 *   <li><em>Sem herança presa:</em> a classe implementa {@code Runnable} e
 *       ainda fica livre para herdar de qualquer outra classe.</li>
 *   <li><em>Reuso da tarefa:</em> o mesmo {@code Runnable} pode ser passado a
 *       várias {@code Thread}s diferentes.</li>
 *   <li><em>Papéis claros:</em> a {@code Thread} apenas <em>executa</em> a
 *       tarefa — ela não <em>é</em> a tarefa, como no modelo anterior.</li>
 * </ul>
 *
 * <p>Desde o Java 5, implementar {@code Runnable} é a forma recomendada em vez
 * de estender {@code Thread} diretamente.</p>
 */
public class AtendimentoRunnable implements Runnable {

    private final int idCliente;

    public AtendimentoRunnable(int idCliente) {
        this.idCliente = idCliente;
    }

    @Override
    public void run() {
        // Aqui o nome vem da Thread que executa a tarefa (definido na criação
        // da Thread), reforçando que tarefa e executor são coisas distintas.
        String executor = Thread.currentThread().getName();
        System.out.printf("%s atendendo cliente %d%n", executor, idCliente);
        Guiche.atender(idCliente);
        System.out.printf("%s finalizou o cliente %d%n", executor, idCliente);
    }
}
