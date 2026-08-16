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

**1. Com o servidor desligado, o que aconteceu ao enviar? Compare com o TCP e explique pela ideia
de "sem conexão".**

O envio **não deu erro nenhum** — o cliente mandou o datagrama numa boa — mas depois ele
**travou** esperando a resposta que nunca chegou (deixei rodando 6s e tive que matar no timeout;
está em `evidencias/udp/`). Bem diferente do TCP da Parte A, onde nem dava pra mandar: o próprio
`connect` já falhava na hora com "connection refused".

A razão é que o UDP é **sem conexão**. No `send`/`sendto` o sistema só joga o datagrama pro
endereço e porta que eu pedi, sem verificar se tem alguém escutando do outro lado — não existe
handshake. Por isso o envio "dá certo" mesmo sem servidor: o pacote sai, só que cai no vazio.
Como o meu cliente manda e em seguida fica no `receive` esperando a resposta, ele fica preso ali
pra sempre. (No Linux ele trava; dependendo do sistema, no Windows pode aparecer um erro de
conexão resetada no `receive` em vez de travar — vale testar na sua máquina e relatar o que der.)
No TCP isso não acontece porque a conexão precisa existir *antes*, então a falha aparece logo no
começo.

**2. Dois exemplos reais que usam UDP e por que a confiabilidade do TCP não é essencial (ou
atrapalharia).**

- **Chamada de vídeo/voz ao vivo (streaming em tempo real).** Se um pacote de áudio se perde, não
  adianta o TCP retransmitir ele — quando a cópia chegasse já estaria atrasada e fora do momento
  da fala. É melhor descartar e seguir pro próximo trecho. A retransmissão do TCP, aqui, só
  causaria travadas e atraso acumulado; o que importa é velocidade e continuidade.
- **DNS (resolver um nome em IP).** É uma troca pergunta-resposta pequena e única. Abrir uma
  conexão TCP (com handshake) só pra fazer uma perguntinha seria overhead à toa. Com UDP a
  consulta vai num pacote só; se a resposta não vier, o cliente simplesmente pergunta de novo.
  (Jogos online multiplayer são outro caso clássico: a posição atual do jogador é o que importa;
  um dado antigo retransmitido não serve pra nada.)

**3. O servidor UDP não guarda "quem está conectado". Dá pra implementar isso? O que mudaria na
arquitetura?**

Dá sim, mas teria que ser feito na mão, na camada da aplicação. Como cada `recvfrom` já me
entrega o endereço (IP + porta) de quem mandou, eu poderia manter uma lista/conjunto desses
endereços pra saber "quem já apareceu". O problema é saber quem **saiu**: no UDP não existe evento
de "desconectou", então eu precisaria de algo como um timeout ou uma mensagem de "keep-alive"
(heartbeat) periódica — se um cliente ficar X segundos sem dar sinal, eu o removo da lista.

Ou seja, a arquitetura mudaria bastante: a aplicação passaria a **manter o estado da sessão por
conta própria** (uma coisa que o TCP já dá de graça), com controle de expiração de clientes e,
provavelmente, tratamento de duplicatas e ordem. Dá pra simular uma "conexão" por cima do UDP,
mas todo esse trabalho de controle vira responsabilidade minha.

---

## Parte C — Multicast

_(a preencher)_

---

## Parte D — WebSocket

_(a preencher)_
