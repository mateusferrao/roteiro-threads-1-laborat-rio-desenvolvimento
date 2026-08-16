import socket

# Parte A - TCP (cliente) em Python. Conecta, manda o que eu digito e mostra a
# resposta do servidor. "sair" encerra.
# Porta = 5000 + OFFSET(39) = 5039

HOST = "localhost"
PORTA = 5039

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as cliente:
    cliente.connect((HOST, PORTA))
    print("[TCP] Conectado ao servidor. Digite 'sair' para encerrar.")
    print("      (dica: mande 'hora' pra ver o horário do servidor)")
    # makefile deixa ler linha a linha, igual o readLine() do Java
    arquivo = cliente.makefile("r")

    while True:
        mensagem = input("> ")
        cliente.sendall((mensagem + "\n").encode("utf-8"))
        print(arquivo.readline().strip())
        if mensagem.lower() == "sair":
            break
