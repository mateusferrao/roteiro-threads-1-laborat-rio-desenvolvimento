# Respostas — Lab de Redes (Roteiro 2)

Central de Avisos da Turma. Aqui vão as 12 perguntas (3 por parte), respondidas com base no que
eu vi rodando o código. Rodei os 8 programas (4 protocolos × Java/Python); a saída de cada um
está na pasta `evidencias/`.

OFFSET usado: **39** (portas: TCP 5039, UDP 5040, Multicast 4485, WebSocket 8926/8927).

---

## Parte A — TCP

**1. O que acontece se você iniciar o cliente antes do servidor? Por quê (considerando o TCP)?**

Dá erro de conexão recusada na hora do `connect`. Testei e o Java estourou
`java.net.ConnectException: Connection refused` e o Python `ConnectionRefusedError: [Errno 111]
Connection refused` (as duas saídas estão em `evidencias/tcp/`).

Isso acontece porque o TCP é orientado a conexão: antes de trocar qualquer dado ele precisa
fazer o handshake (o famoso SYN / SYN-ACK / ACK) com um socket que esteja **escutando** naquela
porta. Como o servidor não está no ar, ninguém aceita o SYN — o sistema operacional do outro
lado responde com um RST recusando, e o `connect` falha imediatamente. Ou seja, no TCP a conexão
tem que existir *antes* de mandar a mensagem; sem servidor, não tem o que conectar.

**2. O TCP garante que as mensagens cheguem na ordem enviada. Qual mecanismo faz isso?**

Os **números de sequência** junto com os **ACKs (confirmações)**. Cada byte enviado leva um
número de sequência; o lado que recebe usa esses números pra remontar os dados na ordem certa,
mesmo que os pacotes cheguem fora de ordem pela rede, e confirma (ACK) o que recebeu. Se algum
pedaço não chega, o ACK não vem e o TCP **retransmite**. É essa combinação
sequência + confirmação + retransmissão que garante ordem e entrega — e é justamente o que o UDP
(Parte B) não tem.

**3. E se dois clientes tentassem conectar ao mesmo tempo? O código atual suporta? Justifique
olhando o servidor.**

Do jeito que está, **não** suporta dois clientes de verdade. Olhando o `ServidorTCP.java`, o
`servidor.accept()` é chamado **uma vez só**, dentro de um `try-with-resources`. Ele atende
aquele único cliente no `while`, e quando esse cliente manda `sair` (ou desconecta) o bloco
fecha e o servidor **encerra**. O servidor Python é igual: um `accept()` e um laço.

Um segundo cliente até consegue completar o handshake TCP (fica na fila do `listen`), mas ele
nunca é "aceito" enquanto o primeiro está sendo atendido — e como o servidor termina depois do
primeiro, o segundo fica sem atendimento. Pra suportar vários, eu teria que colocar o `accept()`
dentro de um laço e tratar **cada cliente numa thread própria** (ou num pool de threads, tipo o
que vi no roteiro 1 de threads). Aí sim daria pra atender conexões simultâneas.

---

## Parte B — UDP

_(a preencher)_

---

## Parte C — Multicast

_(a preencher)_

---

## Parte D — WebSocket

_(a preencher)_
