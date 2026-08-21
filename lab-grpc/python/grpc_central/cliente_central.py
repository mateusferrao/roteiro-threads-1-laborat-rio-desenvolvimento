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


if __name__ == "__main__":
    main()
