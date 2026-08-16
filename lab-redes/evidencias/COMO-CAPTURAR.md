# Como capturar as evidências (prints) para a entrega

O enunciado (seção 3.2) exige **8 prints de tela** (`.png`/`.jpg`), um por protocolo/linguagem,
mostrando a **execução real** — os terminais do servidor e do(s) cliente(s) lado a lado, com as
mensagens sendo trocadas, e a saída do comando `Get-Date` visível em algum terminal (pra provar
que o print é seu e foi feito na hora).

> Os arquivos `.txt` que estão nas pastas desta seção são a **saída real** dos programas quando
> rodei aqui — servem de conferência do resultado esperado, mas **não substituem o print**. Você
> precisa gerar os `.png` na sua máquina.

## Passo a passo (Windows / PowerShell)

Para cada uma das 8 combinações (TCP, UDP, Multicast, WebSocket × Java, Python):

1. Abra 2 (ou 3, no multicast/websocket) janelas de terminal.
2. Em uma delas, rode `Get-Date` uma vez (deixa a data/hora aparecendo no print).
3. Suba o servidor num terminal e o(s) cliente(s) no(s) outro(s), conforme o README.
4. Troque as mensagens que a tarefa de cada parte pede:
   - **TCP:** pelo menos 3 mensagens, incluindo o teste do `hora`.
   - **UDP:** troca normal **e** o teste com o servidor desligado (Ctrl+C e mandar mensagem).
   - **Multicast:** servidor + 2 clientes recebendo o mesmo aviso ao mesmo tempo.
   - **WebSocket:** servidor + 2 clientes, uma mensagem de um cliente aparecendo nos outros.
5. Posicione as janelas lado a lado, tire o print (tecla `PrtScn`, ou `Win+Shift+S` pra recortar).
6. Salve com o nome certo:

```
evidencias/tcp/tcp-java.png            evidencias/tcp/tcp-python.png
evidencias/udp/udp-java.png            evidencias/udp/udp-python.png
evidencias/multicast/multicast-java.png    evidencias/multicast/multicast-python.png
evidencias/websocket/websocket-java.png    evidencias/websocket/websocket-python.png
```

7. O print entra no mesmo commit da parte (o `git add` de cada parte já inclui a pasta
   `evidencias/`).

## Se o multicast não funcionar na sua rede

É comum (VPN/Wi-Fi da facul/WSL bloqueiam multicast). Siga a ordem da seção 6.5 do enunciado.
Se mesmo assim não rolar, **não é falha sua**: documente na resposta da Pergunta 3 da Parte C o
que você tentou, e coloque um print da tentativa (mesmo sem receber a mensagem) — o enunciado
aceita isso como evidência válida.
