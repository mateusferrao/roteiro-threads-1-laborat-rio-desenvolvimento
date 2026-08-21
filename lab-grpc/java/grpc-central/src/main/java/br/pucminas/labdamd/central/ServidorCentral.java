package br.pucminas.labdamd.central;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

// Parte C/D - servidor gRPC da Central. Repare que não abro socket nem trato
// bytes: eu só implemento os métodos do serviço que foram definidos no
// central.proto. O gRPC cuida da rede (HTTP/2), da serialização, etc.
public class ServidorCentral {
    // OFFSET = 2 últimos dígitos do RA (ver README). Cliente usa o mesmo.
    static final int OFFSET = 39;

    public static void main(String[] args) throws IOException, InterruptedException {
        int porta = 50051 + OFFSET; // 50090
        Server servidor = ServerBuilder.forPort(porta)
                .addService(new CentralAtendimentoImpl())
                .build();
        servidor.start();
        System.out.println("[gRPC] Servidor da Central ouvindo na porta " + porta);
        servidor.awaitTermination();
    }

    // Implementação do serviço: estende a classe-base gerada a partir do .proto.
    static class CentralAtendimentoImpl extends CentralAtendimentoGrpc.CentralAtendimentoImplBase {

        // RPC unário: recebe uma PerguntaHorario, devolve uma RespostaHorario.
        @Override
        public void consultarHorario(PerguntaHorario pedido, StreamObserver<RespostaHorario> observador) {
            String horario = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            System.out.println("[gRPC] ConsultarHorario chamado por: " + pedido.getNomeAluno());
            RespostaHorario resposta = RespostaHorario.newBuilder()
                    .setHorarioAtual(horario)
                    .setMensagem("Olá, " + pedido.getNomeAluno() + "! Agora são " + horario + ".")
                    .build();
            // onNext envia a resposta; onCompleted encerra a chamada (é unária: uma só).
            observador.onNext(resposta);
            observador.onCompleted();
        }
    }
}
