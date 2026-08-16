import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Parte A - TCP (servidor). O TCP abre uma conexao (handshake) antes de trocar
// dados e garante entrega e ordem. Aqui o servidor é o "monitor da turma": o
// cliente conecta, manda mensagens e recebe resposta, ate mandar "sair".
//
// Porta = 5000 + OFFSET(39) = 5039  (OFFSET = 2 ultimos digitos do RA, ver README)
public class ServidorTCP {
    public static void main(String[] args) throws IOException {
        int porta = 5039;
        try (ServerSocket servidor = new ServerSocket(porta)) {
            System.out.println("[TCP] Servidor aguardando conexões na porta " + porta + "...");
            // accept() bloqueia ate um cliente conectar. So aceita UM cliente
            // (ver pergunta 3 da Parte A no RESPOSTAS.md).
            try (Socket cliente = servidor.accept();
                 BufferedReader entrada = new BufferedReader(
                         new InputStreamReader(cliente.getInputStream()));
                 PrintWriter saida = new PrintWriter(cliente.getOutputStream(), true)) {

                System.out.println("[TCP] Cliente conectado: " + cliente.getRemoteSocketAddress());
                String mensagem;
                while ((mensagem = entrada.readLine()) != null) {
                    System.out.println("[TCP] Recebido: " + mensagem);

                    if (mensagem.equalsIgnoreCase("sair")) {
                        saida.println("Encerrando conexão. Até mais!");
                        break;
                    }
                    // tarefa 3: se pedir "hora", responde o horario do servidor
                    if (mensagem.equalsIgnoreCase("hora")) {
                        String agora = LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                        saida.println("Monitor responde: agora são " + agora);
                        continue;
                    }
                    // caso normal: eco da mensagem
                    saida.println("Monitor responde: recebi sua mensagem -> \"" + mensagem + "\"");
                }
            }
        }
        System.out.println("[TCP] Servidor encerrado.");
    }
}
