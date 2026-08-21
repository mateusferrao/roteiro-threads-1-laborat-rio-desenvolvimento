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

        // RPC com streaming de servidor: uma inscrição, VÁRIOS avisos ao longo do
        // tempo. Chamo onNext() várias vezes na mesma conexão e só no fim onCompleted().
        @Override
        public void acompanharAvisos(InscricaoAvisos pedido, StreamObserver<Aviso> observador) {
            System.out.println("[gRPC] AcompanharAvisos: " + pedido.getNomeAluno() + " se inscreveu.");
            try {
                for (int i = 1; i <= 5; i++) {
                    Aviso aviso = Aviso.newBuilder()
                            .setNumero(i)
                            .setTexto("Aviso #" + i + ": a aula começa em " + (5 - i) + " minuto(s)!")
                            .build();
                    observador.onNext(aviso);
                    Thread.sleep(2000);
                }
                observador.onCompleted();
            } catch (InterruptedException e) {
                observador.onError(e);
            }
        }
    }
}
