import java.io.*;
import java.net.*;

// Parte A - TCP (cliente). Conecta no servidor, le o que eu digito no teclado,
// manda pro servidor e mostra a resposta. Digita "sair" pra encerrar.
//
// Porta = 5000 + OFFSET(39) = 5039
public class ClienteTCP {
    public static void main(String[] args) throws IOException {
        String host = "localhost";
        int porta = 5039;

        try (Socket socket = new Socket(host, porta);
             PrintWriter saida = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader entrada = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()));
             BufferedReader teclado = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("[TCP] Conectado ao servidor. Digite 'sair' para encerrar.");
            System.out.println("      (dica: mande 'hora' pra ver o horário do servidor)");
            String linha;
            while (true) {
                System.out.print("> ");
                linha = teclado.readLine();
                if (linha == null) break; // fim da entrada (ex: pipe fechou)
                saida.println(linha);
                System.out.println(entrada.readLine());
                if (linha.equalsIgnoreCase("sair")) break;
            }
        }
    }
}
