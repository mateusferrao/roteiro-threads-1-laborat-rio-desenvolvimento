import socket

# Parte B - UDP (servidor) em Python. SOCK_DGRAM = UDP. Fica recebendo
# datagramas e respondendo pra quem enviou. Sem conexao, sem garantia.
# Porta = 5001 + OFFSET(39) = 5040

HOST = "0.0.0.0"
PORTA = 5040

with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as servidor:
    servidor.bind((HOST, PORTA))
    print(f"[UDP] Servidor aguardando datagramas na porta {PORTA}...")

    while True:
        # recvfrom devolve os dados E o endereco de quem mandou
        dados, endereco_cliente = servidor.recvfrom(1024)
        mensagem = dados.decode("utf-8")
        print(f"[UDP] Recebido de {endereco_cliente}: {mensagem}")

        resposta = f'Monitor responde: recebi sua mensagem -> "{mensagem}"'
        servidor.sendto(resposta.encode("utf-8"), endereco_cliente)
