package br.pucminas.labdamd.threads.partea;

import br.pucminas.labdamd.threads.comum.Guiche;

// Parte A - forma antiga: a classe herda de Thread e sobrescreve o run().
// Problema dessa forma: como ela ja herda de Thread, nao pode herdar de mais
// nenhuma classe (ex: um Funcionario). E isso que a Parte B resolve.
public class AtendimentoThread extends Thread {

    private final int idCliente;

    public AtendimentoThread(int idCliente) {
        this.idCliente = idCliente;
        // dou nome pra thread pra conseguir identificar quem e quem no log
        setName("Atendente-" + idCliente);
    }

    // esse codigo roda na thread nova quando eu chamo start()
    @Override
    public void run() {
        System.out.printf("%s atendendo cliente %d%n", getName(), idCliente);
        Guiche.atender(idCliente); // fica ~1s "atendendo"
        System.out.printf("%s finalizou o cliente %d%n", getName(), idCliente);
    }
}
