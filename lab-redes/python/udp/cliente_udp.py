import socket

# Parte B - UDP (cliente) em Python. Manda datagrama e espera resposta.
# Porta = 5001 + OFFSET(39) = 5040

HOST = "localhost"
PORTA = 5040

with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as cliente:
    print("[UDP] Pronto para enviar. Digite 'sair' para encerrar.")
    while True:
        mensagem = input("> ")
        cliente.sendto(mensagem.encode("utf-8"), (HOST, PORTA))
        if mensagem.lower() == "sair":
            break
        # espera a resposta; com o servidor desligado, trava aqui (pergunta 1)
        dados, _ = cliente.recvfrom(1024)
        print(dados.decode("utf-8"))
