import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

// Parte D - WebSocket (servidor). É o "mural de avisos" da turma: vários alunos
// ficam conectados e, quando um manda uma mensagem, ela é reenviada (broadcast)
// pra todos. O WebSocket começa com um handshake HTTP (Upgrade) e depois mantém
// a conexão TCP aberta pra troca nos dois sentidos, em tempo real.
public class MuralServidor extends WebSocketServer {

    public MuralServidor(int porta) {
        super(new InetSocketAddress(porta));
    }

    @Override
    public void onOpen(WebSocket conexao, ClientHandshake handshake) {
        System.out.println("[WebSocket] Novo aluno conectado: " + conexao.getRemoteSocketAddress());
        conexao.send("Bem-vindo(a) ao mural de avisos da turma!");
    }

    @Override
    public void onMessage(WebSocket conexao, String mensagem) {
        System.out.println("[WebSocket] Recebido: " + mensagem);
        String avisoFormatado = "Aviso da turma: " + mensagem;
        // reenvia pra TODOS os conectados (inclusive quem mandou)
        for (WebSocket cliente : getConnections()) {
            cliente.send(avisoFormatado);
        }
    }

    @Override
    public void onClose(WebSocket conexao, int codigo, String motivo, boolean remoto) {
        System.out.println("[WebSocket] Aluno desconectado: " + conexao.getRemoteSocketAddress());
    }

    @Override
    public void onError(WebSocket conexao, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("[WebSocket] Servidor do mural iniciado.");
    }

    public static void main(String[] args) {
        // porta = 8887 + OFFSET(39) = 8926 (ver README)
        MuralServidor servidor = new MuralServidor(8926);
        servidor.start();
    }
}
