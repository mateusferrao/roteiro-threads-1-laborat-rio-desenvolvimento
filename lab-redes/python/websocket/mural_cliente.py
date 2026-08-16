import asyncio
import websockets

# Parte D - WebSocket (cliente) em Python. Conecta no mural, escuta os avisos
# numa tarefa em paralelo e manda o que eu digitar. "sair" encerra.
# porta = 8888 + OFFSET(39) = 8927

PORTA = 8927


async def escutar(websocket):
    async for mensagem in websocket:
        print(f"\n{mensagem}")
        print("> ", end="", flush=True)


async def main():
    uri = f"ws://localhost:{PORTA}"
    async with websockets.connect(uri) as websocket:
        print("[WebSocket] Conectado ao mural. Digite 'sair' para encerrar.")
        tarefa_escuta = asyncio.create_task(escutar(websocket))

        loop = asyncio.get_event_loop()
        while True:
            # input() é bloqueante; jogo num executor pra não travar o asyncio
            mensagem = await loop.run_in_executor(None, input, "> ")
            if mensagem.lower() == "sair":
                break
            await websocket.send(mensagem)

        tarefa_escuta.cancel()


asyncio.run(main())
