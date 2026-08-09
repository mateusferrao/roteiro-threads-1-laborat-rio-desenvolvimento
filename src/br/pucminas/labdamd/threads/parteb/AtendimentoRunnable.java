package br.pucminas.labdamd.threads.parteb;

import br.pucminas.labdamd.threads.comum.Guiche;

// Parte B - agora a tarefa implementa Runnable em vez de herdar de Thread.
// Vantagem: como so implementa uma interface, a classe fica livre pra herdar de
// outra classe. E a Thread so executa a tarefa, ela nao "e" a tarefa.
// Desde o Java 5 essa e a forma recomendada.
public class AtendimentoRunnable implements Runnable {

    private final int idCliente;

    public AtendimentoRunnable(int idCliente) {
        this.idCliente = idCliente;
    }

    @Override
    public void run() {
        // o nome vem da Thread que ta executando (definido la na hora de criar a
        // Thread), reforcando que tarefa e executor sao coisas separadas
        String executor = Thread.currentThread().getName();
        System.out.printf("%s atendendo cliente %d%n", executor, idCliente);
        Guiche.atender(idCliente);
        System.out.printf("%s finalizou o cliente %d%n", executor, idCliente);
    }
}
