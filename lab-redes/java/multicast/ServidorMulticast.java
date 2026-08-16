import java.net.*;
import java.io.IOException;

// Parte C - Multicast (servidor/emissor). Aqui o "professor" manda um aviso e
// TODO mundo que estiver inscrito no grupo recebe de uma vez, sem o emissor
// precisar conhecer cada destinatario. O grupo e um IP especial (classe D,
// faixa 224.0.0.0 a 239.255.255.255).
public class ServidorMulticast {
    // OFFSET = 2 ultimos digitos do RA (ver README). Cliente tem que usar o mesmo.
    static final int OFFSET = 39;

    public static void main(String[] args) throws IOException, InterruptedException {
        String grupoMulticast = "230.0.0.1";
        int porta = 4446 + OFFSET; // 4485

        InetAddress grupo = InetAddress.getByName(grupoMulticast);
        // Pra ENVIAR multicast basta um DatagramSocket comum, mandando pro IP do grupo.
        try (DatagramSocket socket = new DatagramSocket()) {
            int contador = 1;
            System.out.println("[Multicast] Enviando avisos para o grupo " + grupoMulticast + ":" + porta);
            while (contador <= 5) {
                String mensagem = "Aviso #" + contador + ": a aula começa em " + (5 - contador) + " minuto(s)!";
                byte[] dados = mensagem.getBytes();
                DatagramPacket pacote = new DatagramPacket(dados, dados.length, grupo, porta);
                socket.send(pacote);
                System.out.println("[Multicast] Enviado: " + mensagem);
                contador++;
                Thread.sleep(2000);
            }
        }
    }
}
