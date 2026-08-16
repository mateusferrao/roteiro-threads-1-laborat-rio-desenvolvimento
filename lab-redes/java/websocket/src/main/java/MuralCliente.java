import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Scanner;
import java.util.concurrent.CompletionStage;

// Parte D - WebSocket (cliente) em Java. Aqui NÃO precisa de lib externa: uso a
// API java.net.http.WebSocket, nativa do JDK 11+. O cliente conecta no mural,
// fica ouvindo os avisos (onText) e manda o que eu digitar. "sair" encerra.
public class MuralCliente {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        WebSocket.Listener listener = new WebSocket.Listener() {
            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                System.out.println("\n" + data);
                System.out.print("> ");
                webSocket.request(1); // pede a próxima mensagem
                return null;
            }
        };

        // porta = 8887 + OFFSET(39) = 8926
        WebSocket socket = client.newWebSocketBuilder()
                .buildAsync(URI.create("ws://localhost:8926"), listener)
                .join();

        Scanner teclado = new Scanner(System.in);
        System.out.println("[WebSocket] Conectado ao mural. Digite 'sair' para encerrar.");
        while (true) {
            System.out.print("> ");
            String mensagem = teclado.nextLine();
            if (mensagem.equalsIgnoreCase("sair")) {
                socket.sendClose(WebSocket.NORMAL_CLOSURE, "Até mais!").join();
                break;
            }
            socket.sendText(mensagem, true).join();
        }
    }
}
