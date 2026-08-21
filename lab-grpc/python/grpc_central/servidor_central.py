from concurrent import futures
from datetime import datetime
import time

import grpc
import central_pb2
import central_pb2_grpc

# Parte C/D - servidor gRPC em Python. Igual ao Java: eu só implemento os
# métodos do serviço; o gRPC cuida da rede e da serialização.
# OFFSET = 2 últimos dígitos do RA (ver README).
OFFSET = 39
PORTA = 50061 + OFFSET  # 50100


class CentralAtendimentoServicer(central_pb2_grpc.CentralAtendimentoServicer):

    # RPC unário: recebe uma PerguntaHorario, devolve uma RespostaHorario.
    def ConsultarHorario(self, request, context):
        horario = datetime.now().strftime("%H:%M:%S")
        print(f"[gRPC] ConsultarHorario chamado por: {request.nome_aluno}")
        return central_pb2.RespostaHorario(
            horario_atual=horario,
            mensagem=f"Olá, {request.nome_aluno}! Agora são {horario}.",
        )


def main():
    servidor = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    central_pb2_grpc.add_CentralAtendimentoServicer_to_server(CentralAtendimentoServicer(), servidor)
    servidor.add_insecure_port(f"[::]:{PORTA}")
    servidor.start()
    print(f"[gRPC] Servidor da Central ouvindo na porta {PORTA}")
    servidor.wait_for_termination()


if __name__ == "__main__":
    main()
