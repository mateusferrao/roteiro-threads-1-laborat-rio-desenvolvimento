# Respostas — Lab de gRPC e Transparências (Roteiro 3)

Central de Atendimento da Turma via gRPC. Aqui vão a reflexão da Parte A (comparando com o lab de
redes) e as 12 perguntas (3 por parte), respondidas com base no que eu vi implementando e rodando.
Rodei os 4 exemplos (unário e streaming × Java/Python); a saída está em `evidencias/`.

OFFSET usado: **39** (portas gRPC: Java 50090, Python 50100).

---

## Parte A — Transparências em Sistemas Distribuídos

### Reflexão sobre o lab anterior (seção 4.1)

Olhando o código das 4 soluções do lab de redes (`../lab-redes/`), respondendo os 3 pontos para
cada uma:

**TCP**
1. *Endereço no código do cliente?* Sim — o `ClienteTCP.java` tem `host = "localhost"` e
   `porta = 5039` escritos direto. Isso **prejudica** a transparência de localização: o cliente
   precisa saber exatamente onde o servidor está.
2. *Monta string na mão?* Sim — o cliente manda linhas de texto e o servidor faz `readLine()` e
   compara strings (`"hora"`, `"sair"`). É **ausência** de transparência de acesso: eu manipulo
   texto/bytes, não chamo uma operação.
3. *Servidor muda de máquina amanhã?* O cliente para de funcionar até eu editar o `host` no
   código-fonte e recompilar. Não sobrevive sozinho.

**UDP**
1. Mesma coisa: `HOST = "localhost"` e `PORTA = 5040` fixos no cliente → prejudica localização.
2. Também monta/interpreta string na mão (`encode`/`decode`, `getBytes`) → ausência de acesso.
3. Igual ao TCP: mudou a máquina do servidor, tenho que editar o cliente.

**Multicast**
1. O cliente não aponta pra uma máquina, e sim pra um **grupo** (`230.0.0.1:4485`), que também
   está fixo no código. Mas aqui tem uma diferença boa: o grupo é um endereço lógico, não a
   localização física de um servidor.
2. Ainda é string na mão (o cliente faz `decode` do texto do aviso) → ausência de acesso.
3. **Esta é a que mais sobrevive!** Como o cliente escuta um *grupo*, não uma máquina específica,
   se o emissor (o "servidor") trocar de computador, os clientes continuam recebendo sem mudar
   nada — eles nunca souberam de qual máquina o aviso veio. Ou seja, o multicast tem uma
   transparência de localização (do lado do receptor) que as outras não têm.

**WebSocket**
1. O cliente tem a URI `ws://localhost:8926` (Java) / `8927` (Python) fixa → prejudica
   localização.
2. As mensagens ainda são texto montado na mão; o WebSocket dá o *enquadramento* (sei onde uma
   mensagem termina), mas o **conteúdo** continua sendo string que eu monto e interpreto → é um
   **meio-termo** de acesso (melhor que TCP cru, mas ainda não é "chamar um método").
3. Servidor mudou de máquina → tenho que editar a URI no cliente. Não sobrevive sozinho.

**Resumo:** as 4 soluções escrevem o endereço direto no cliente (pouca transparência de
localização), sendo o multicast a exceção parcial (escuta um grupo, não uma máquina). E as 4
exigem montar/interpretar texto na mão (pouca ou nenhuma transparência de acesso). É exatamente
esse trabalho manual que o gRPC vai esconder nas Partes C e D.

### Perguntas (seção 4.3)

**1. Dos 8 tipos de transparência, qual é a mais visível para o programador que usa um serviço
remoto (e não constrói a infraestrutura)? Justifique.**

Pra mim é a **transparência de acesso**. Ela é a que o programador literalmente escreve e enxerga
no código: chamar uma operação remota **com a mesma cara** de uma chamada local. No gRPC,
`stub.consultarHorario(pergunta)` parece um método comum — não tem `send`, `receive`, `getBytes`
nem parsing à vista. É a diferença que mais salta aos olhos de quem só *usa* o serviço. (A de
localização também é visível, mas ela aparece mais na configuração/endereço; a de acesso está na
forma de cada chamada que eu escrevo.)

**2. Transparência total é sempre desejável? Dê um exemplo em que esconder que a operação é remota
atrapalharia (pense em desempenho ou falha).**

Não, nem sempre. Se a chamada remota fica **idêntica** a uma local, é fácil o programador tratá-la
como se fosse barata. Exemplo de **desempenho**: imagina alguém chamando `stub.consultarHorario()`
dentro de um `for` de 10.000 iterações achando que é um "getter" local — cada volta vira uma ida e
volta pela rede, e o que pareceria instantâneo local vira lentidão enorme. Exemplo de **falha**:
uma chamada local praticamente não "falha por rede"; uma remota pode dar timeout, cair no meio,
perder conexão. Se a natureza remota estiver 100% escondida, o programador não escreve tratamento
de timeout/retry, e o programa quebra de um jeito que uma função local nunca quebraria. Por isso os
frameworks bons **deixam vazar de propósito** o que precisa: o gRPC ainda me obriga a lidar com
`StatusRuntimeException`, deadlines etc. Um pouco de "não-transparência" de desempenho e de falha é
saudável.

**3. (Responder após concluir C e D) Comparando o cliente TCP do lab anterior com o cliente gRPC:
qual faz você "pensar em rede" e qual deixa você "pensar no problema"? A que transparência se
relaciona?**

_(respondida na Parte D, após implementar o RPC unário e o streaming)_

---

## Parte B — Protocol Buffers

_(a preencher)_

---

## Parte C — RPC unário

_(a preencher)_

---

## Parte D — RPC com streaming

_(a preencher)_
