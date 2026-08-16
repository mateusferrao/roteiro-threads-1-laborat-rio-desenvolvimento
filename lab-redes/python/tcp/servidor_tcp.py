import socket
from datetime import datetime

# Parte A - TCP (servidor) em Python. Mesma ideia da versao Java: aceita um
# cliente, recebe mensagens e responde, ate receber "sair".
# Porta = 5000 + OFFSET(39) = 5039

HOST = "0.0.0.0"
PORTA = 5039

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as servidor:
    # SO_REUSEADDR: deixa reusar a porta logo apos fechar (sem esperar o TIME_WAIT)
    servidor.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    servidor.bind((HOST, PORTA))
    servidor.listen(1)
    print(f"[TCP] Servidor aguardando conexões na porta {PORTA}...")

    conexao, endereco = servidor.accept()
    with conexao:
        print(f"[TCP] Cliente conectado: {endereco}")
        while True:
            dados = conexao.recv(1024).decode("utf-8").strip()
            if not dados:
                break
            print(f"[TCP] Recebido: {dados}")

            if dados.lower() == "sair":
                conexao.sendall("Encerrando conexão. Até mais!\n".encode("utf-8"))
                break
            # tarefa 3: se pedir "hora", responde o horario do servidor
            if dados.lower() == "hora":
                agora = datetime.now().strftime("%d/%m/%Y %H:%M:%S")
                conexao.sendall(f"Monitor responde: agora são {agora}\n".encode("utf-8"))
                continue

            resposta = f'Monitor responde: recebi sua mensagem -> "{dados}"\n'
            conexao.sendall(resposta.encode("utf-8"))

print("[TCP] Servidor encerrado.")
