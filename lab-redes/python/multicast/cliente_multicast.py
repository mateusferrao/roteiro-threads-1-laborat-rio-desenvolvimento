import socket
import struct

# Parte C - Multicast (cliente/receptor) em Python. Entra no grupo e escuta.
# OFFSET = mesmo valor do servidor (ver README).
OFFSET = 39

GRUPO_MULTICAST = "230.0.0.1"
PORTA = 4446 + OFFSET  # 4485

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
# SO_REUSEADDR: deixa varios clientes na mesma maquina ouvirem a mesma porta
sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
sock.bind(("", PORTA))

# entra no grupo multicast (IP_ADD_MEMBERSHIP) em todas as interfaces (INADDR_ANY)
grupo = socket.inet_aton(GRUPO_MULTICAST)
solicitacao_membro = struct.pack("4sL", grupo, socket.INADDR_ANY)
sock.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, solicitacao_membro)

print(f"[Multicast] Inscrito no grupo {GRUPO_MULTICAST}:{PORTA}. Aguardando avisos...")
while True:
    dados, endereco = sock.recvfrom(1024)
    print(f"[Multicast] Recebido: {dados.decode('utf-8')}")
