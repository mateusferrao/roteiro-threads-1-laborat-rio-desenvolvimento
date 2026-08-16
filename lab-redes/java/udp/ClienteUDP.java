import java.net.*;
import java.util.Scanner;

// Parte B - UDP (cliente). Manda datagramas pro servidor e espera a resposta.
// Repare que nao existe "connect": eu simplesmente envio pro endereco/porta.
//
// Porta = 5001 + OFFSET(39) = 5040
public class ClienteUDP {
    public static void main(String[] args) throws Exception {
        String host = "localhost";
        int porta = 5040;

        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress endereco = InetAddress.getByName(host);
            Scanner teclado = new Scanner(System.in);
            byte[] buffer = new byte[1024];

            System.out.println("[UDP] Pronto para enviar. Digite 'sair' para encerrar.");
            while (true) {
                System.out.print("> ");
                String mensagem = teclado.nextLine();
                byte[] dados = mensagem.getBytes();

                DatagramPacket pacote = new DatagramPacket(dados, dados.length, endereco, porta);
                socket.send(pacote);
                if (mensagem.equalsIgnoreCase("sair")) break;

                // fica esperando a resposta. Se o servidor estiver desligado,
                // essa linha trava (nao chega resposta nenhuma) - ver pergunta 1.
                DatagramPacket resposta = new DatagramPacket(buffer, buffer.length);
                socket.receive(resposta);
                System.out.println(new String(resposta.getData(), 0, resposta.getLength()));
            }
        }
    }
}
