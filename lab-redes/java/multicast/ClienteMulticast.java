import java.net.*;
import java.io.IOException;

// Parte C - Multicast (cliente/receptor). Ele "entra" (joinGroup) no grupo e
// passa a receber tudo que for mandado pra aquele grupo:porta. Abra 2 ou 3
// clientes ao mesmo tempo pra ver todos recebendo o mesmo aviso.
public class ClienteMulticast {
    // OFFSET = mesmo valor do servidor (ver README)
    static final int OFFSET = 39;

    public static void main(String[] args) throws IOException {
        String grupoMulticast = "230.0.0.1";
        int porta = 4446 + OFFSET; // 4485

        try (MulticastSocket socket = new MulticastSocket(porta)) {
            InetAddress grupo = InetAddress.getByName(grupoMulticast);
            InetSocketAddress endpointGrupo = new InetSocketAddress(grupo, porta);

            // Em Wi-Fi corporativa/VPN o multicast costuma ser bloqueado. Pra testar
            // servidor e cliente na MESMA maquina, use a interface de loopback:
            // NetworkInterface interfaceRede = NetworkInterface.getByInetAddress(InetAddress.getLoopbackAddress());
            NetworkInterface interfaceRede = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());

            socket.joinGroup(endpointGrupo, interfaceRede);
            System.out.println("[Multicast] Inscrito no grupo " + grupoMulticast + ":" + porta + ". Aguardando avisos...");

            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
                socket.receive(pacote);
                String mensagem = new String(pacote.getData(), 0, pacote.getLength());
                System.out.println("[Multicast] Recebido: " + mensagem);
            }
        }
    }
}
