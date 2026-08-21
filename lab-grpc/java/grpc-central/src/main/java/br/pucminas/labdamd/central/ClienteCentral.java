package br.pucminas.labdamd.central;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Scanner;

// Parte C/D - cliente gRPC. O que dá pra reparar: eu chamo métodos do stub como
// se fossem funções locais. Não tem send/receive nem montagem de string - o
// gRPC serializa, manda por HTTP/2 e desserializa a resposta por baixo.
public class ClienteCentral {
    // OFFSET = mesmo valor do servidor (ver README)
    static final int OFFSET = 39;

    public static void main(String[] args) {
        int porta = 50051 + OFFSET; // 50090
        // O "canal" é a conexão com o servidor. usePlaintext() = sem TLS (lab local).
        ManagedChannel canal = ManagedChannelBuilder.forAddress("localhost", porta)
                .usePlaintext()
                .build();
        try {
            CentralAtendimentoGrpc.CentralAtendimentoBlockingStub stub =
                    CentralAtendimentoGrpc.newBlockingStub(canal);

            Scanner teclado = new Scanner(System.in);
            System.out.print("Digite seu nome: ");
            String nome = teclado.nextLine();

            // Chamada unária: parece uma chamada de método local, mas atravessa a rede.
            PerguntaHorario pergunta = PerguntaHorario.newBuilder().setNomeAluno(nome).build();
            RespostaHorario resposta = stub.consultarHorario(pergunta);
            System.out.println("[gRPC] " + resposta.getMensagem());
        } finally {
            canal.shutdown();
        }
    }
}
