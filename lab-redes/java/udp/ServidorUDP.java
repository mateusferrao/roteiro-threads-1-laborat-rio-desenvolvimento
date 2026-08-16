import java.net.*;

// Parte B - UDP (servidor). Diferente do TCP, o UDP nao abre conexao: ele so
// fica esperando datagramas chegarem e responde pra quem mandou. Nao ha
// garantia de entrega nem de ordem.
//
// Porta = 5001 + OFFSET(39) = 5040
public class ServidorUDP {
    public static void main(String[] args) throws Exception {
        int porta = 5040;
        byte[] buffer = new byte[1024];

        try (DatagramSocket socket = new DatagramSocket(porta)) {
            System.out.println("[UDP] Servidor aguardando datagramas na porta " + porta + "...");
            while (true) {
                // recebe um datagrama; o pacote traz o endereco de quem enviou
                DatagramPacket pacoteRecebido = new DatagramPacket(buffer, buffer.length);
                socket.receive(pacoteRecebido);

                String mensagem = new String(pacoteRecebido.getData(), 0, pacoteRecebido.getLength());
                System.out.println("[UDP] Recebido de " + pacoteRecebido.getAddress() + ": " + mensagem);

                String resposta = "Monitor responde: recebi sua mensagem -> \"" + mensagem + "\"";
                byte[] dadosResposta = resposta.getBytes();
                // responde de volta pro endereco/porta de origem
                DatagramPacket pacoteResposta = new DatagramPacket(
                        dadosResposta, dadosResposta.length,
                        pacoteRecebido.getAddress(), pacoteRecebido.getPort());
                socket.send(pacoteResposta);
            }
        }
    }
}
