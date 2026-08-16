import asyncio
import websockets

# Parte D - WebSocket (servidor) em Python, com a lib 'websockets'.
# Mural da turma: guarda os conectados e faz broadcast de cada mensagem.
# porta = 8888 + OFFSET(39) = 8927 (ver README)

PORTA = 8927
clientes_conectados = set()


async def tratar_conexao(websocket):
    clientes_conectados.add(websocket)
    print(f"[WebSocket] Novo aluno conectado. Total: {len(clientes_conectados)}")
    await websocket.send("Bem-vindo(a) ao mural de avisos da turma!")
    try:
        async for mensagem in websocket:
            print(f"[WebSocket] Recebido: {mensagem}")
            aviso_formatado = f"Aviso da turma: {mensagem}"
            # manda pra todos os conectados de uma vez
            websockets.broadcast(clientes_conectados, aviso_formatado)
    finally:
        clientes_conectados.remove(websocket)
        print(f"[WebSocket] Aluno desconectado. Total: {len(clientes_conectados)}")


async def main():
    print(f"[WebSocket] Servidor do mural iniciado na porta {PORTA}.")
    async with websockets.serve(tratar_conexao, "0.0.0.0", PORTA):
        await asyncio.Future()  # mantém o servidor rodando pra sempre


asyncio.run(main())
