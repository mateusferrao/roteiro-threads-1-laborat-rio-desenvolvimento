import grpc
import central_pb2
import central_pb2_grpc

# Parte C/D - cliente gRPC em Python. Chamo o método do stub como se fosse uma
# função local; o gRPC serializa, manda pela rede e desserializa a resposta.
# OFFSET = mesmo valor do servidor (ver README)
OFFSET = 39
PORTA = 50061 + OFFSET  # 50100


def main():
    canal = grpc.insecure_channel(f"localhost:{PORTA}")
    stub = central_pb2_grpc.CentralAtendimentoStub(canal)

    nome = input("Digite seu nome: ")

    # Chamada unária: parece uma chamada de função local, mas atravessa a rede.
    resposta = stub.ConsultarHorario(central_pb2.PerguntaHorario(nome_aluno=nome))
    print(f"[gRPC] {resposta.mensagem}")

    # Chamada com streaming: itero sobre os Avisos que o servidor vai enviando.
    print("[gRPC] Inscrevendo-se para acompanhar avisos...")
    for aviso in stub.AcompanharAvisos(central_pb2.InscricaoAvisos(nome_aluno=nome)):
        print(f"[gRPC] Recebido: {aviso.texto}")


if __name__ == "__main__":
    main()
