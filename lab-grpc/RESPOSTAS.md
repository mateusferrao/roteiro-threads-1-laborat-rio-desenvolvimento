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

**3. (Respondida após concluir C e D) Comparando o cliente TCP do lab anterior com o cliente gRPC:
qual faz você "pensar em rede" e qual deixa você "pensar no problema"? A que transparência se
relaciona?**

Agora que implementei o unário e o streaming, dá pra comparar com clareza:

- O **cliente TCP** me faz **pensar em rede**: eu tinha que criar o `Socket`, pegar os streams de
  entrada/saída, mandar com `println`, ler com `readLine`, e ainda combinar o formato do texto
  (onde a mensagem começa/termina, o que significa cada resposta). O meu código estava cheio de
  detalhe de comunicação.
- O **cliente gRPC** me deixa **pensar no problema**: eu escrevo `stub.consultarHorario(pergunta)`
  e recebo uma `RespostaHorario`, ou faço um `for` sobre `stub.acompanharAvisos(...)` e vou
  recebendo os avisos. Parece chamada de função e iteração normais — o socket, o HTTP/2, a
  serialização e o parsing sumiram do meu código.

Isso se relaciona principalmente com a **transparência de acesso** (acessar o recurso remoto com a
mesma cara de um acesso local) e, junto, com a **transparência de localização** (o endereço e toda
a "canalização" ficam escondidos no canal/stub, não espalhados pelo meu código). Foi exatamente o
ganho que este roteiro quis mostrar: no lab anterior a transparência era pouca e eu pagava por ela
"na mão"; com o gRPC, boa parte dela vem "de graça" a partir do contrato `.proto`.

---

## Parte B — Protocol Buffers

**1. Qual a vantagem de ter um contrato explícito e gerado automaticamente (`central.proto`) em vez
de combinar o formato das mensagens só "de boca"?**

No lab anterior, o formato das mensagens era um **combinado informal**: eu tinha que lembrar que a
resposta do TCP vinha como `"Monitor responde: ..."` e tratar isso na mão, e se o cliente e o
servidor discordassem (um espera um `\n`, o outro não manda) só descobria na hora que quebrava. Com
o `.proto`, o formato é **uma fonte única da verdade**: os dois lados geram o código a partir do
mesmo arquivo, então não dá pra um esperar `nome_aluno` e o outro mandar `aluno`. Além disso, a
serialização (transformar objeto em bytes e vice-versa) é **gerada** — some a chance de bug de
`getBytes`/`decode`, e a compatibilidade fica documentada pelos números dos campos (`= 1`, `= 2`).
Resumindo: em vez de um acordo frágil na cabeça de quem programou, tenho um contrato versionado que
a ferramenta garante que os dois lados respeitam.

**2. O mesmo `central.proto` gerou código para Java e para Python. O que isso sugere sobre equipes
que usam linguagens diferentes?**

Sugere que a linguagem deixa de ser barreira pra comunicação. Como o contrato e o formato de fio
(wire format) são os mesmos, um servidor escrito em Java e um cliente em Python (ou o contrário)
conversam sem problema — foi o que testei, os dois lados falam o mesmo protocolo. Numa empresa real,
isso quer dizer que times diferentes podem escolher a linguagem que preferem (Go, Java, Python,
C++...) e ainda assim integrar seus serviços, desde que compartilhem o `.proto`. O contrato vira a
"língua franca" entre os sistemas.

**3. Onde ficam definidas `ConsultarHorario` e `AcompanharAvisos` no código gerado? Cite uma classe
ou método que você reconheceu.**

Dá pra achar as duas operações mesmo sem entender o resto do código gerado:

- No **Python** (`central_pb2_grpc.py`): tem a classe `CentralAtendimentoStub` (o que o cliente
  usa) e a `CentralAtendimentoServicer` (o que o servidor implementa), e dentro dela os métodos
  `def ConsultarHorario(self, request, context)` e `def AcompanharAvisos(self, request, context)`.
  Tem também a função `add_CentralAtendimentoServicer_to_server`, que é a que eu chamo no servidor.
- No **Java** (`CentralAtendimentoGrpc.java`): reconheci a classe `CentralAtendimentoImplBase` (que
  o meu servidor estende) e a `CentralAtendimentoBlockingStub`, onde aparece o método
  `RespostaHorario consultarHorario(PerguntaHorario request)` e o
  `Iterator<Aviso> acompanharAvisos(...)` — repare que o `acompanharAvisos` já devolve um
  `Iterator`, justamente porque é o streaming (vários avisos).

---

## Parte C — RPC unário

> Rodei em Java e Python; a resposta com o horário chegou nos dois (`evidencias/unario/`).

**1. A linha `stub.consultarHorario(pergunta)` parece uma chamada de método comum. Cite pelo menos
três coisas que acontecem "por baixo dos panos" entre essa chamada e o `return` no servidor.**

Parece uma função local, mas no meio do caminho acontece bastante coisa. Pelo menos:

1. **Serialização:** o objeto `PerguntaHorario` é transformado em bytes no formato Protocol Buffers
   (marshalling).
2. **Transporte pela rede:** o gRPC abre/usa uma conexão **HTTP/2** com o servidor (no meu caso
   `localhost:50090`) e envia esses bytes por ela.
3. **Do outro lado:** o servidor **desserializa** os bytes de volta pra um `PerguntaHorario`,
   descobre qual método chamar (roteamento pelo nome do serviço/método) e invoca o meu
   `consultarHorario(...)`.
4. E na volta é o caminho inverso: a `RespostaHorario` é serializada, volta pela mesma conexão e é
   desserializada no cliente, virando o objeto que eu recebo. Tudo isso escondido atrás de uma
   linha só.

**2. Onde estava, no `ClienteTCP` do lab anterior, o equivalente a "montar a mensagem" e
"interpretar a resposta"? Quem faz isso agora no gRPC?**

No TCP eu fazia isso **na mão**: "montar a mensagem" era o `saida.println(linha)` mandando a string
que eu digitei, e "interpretar a resposta" era o `entrada.readLine()` lendo a linha de texto e eu
tendo que entender aquele formato (`"Monitor responde: ..."`). Ou seja, o protocolo de aplicação era
por minha conta. No gRPC, quem faz isso é o **código gerado a partir do `.proto`** (os stubs): eu só
crio o objeto `PerguntaHorario` e leio os campos da `RespostaHorario` (`resposta.getMensagem()`); a
montagem/serialização e a interpretação/desserialização são geradas e ficam invisíveis pra mim.

**3. O que acontece se você chamar com o servidor desligado? Teste e descreva.**

Testei nas duas linguagens (está em `evidencias/unario/`): o gRPC lança o status **`UNAVAILABLE`**.
No Java vem como `io.grpc.StatusRuntimeException: UNAVAILABLE`, causada por um
`Connection refused` na porta 50090; no Python vem como `StatusCode.UNAVAILABLE` com
"failed to connect ... Connection refused". Ou seja, o erro **não fica escondido**: em vez de um
erro de socket cru como no TCP, o gRPC me entrega um **código de status padronizado** dizendo que o
serviço está indisponível — mas eu ainda preciso tratá-lo. É exatamente o ponto da Pergunta 2 da
Parte A: a falha continua sendo minha responsabilidade, o framework só a apresenta de forma mais
organizada.

---

## Parte D — RPC com streaming

> Rodei nas duas linguagens: depois da resposta do horário, o cliente recebe os 5 avisos, um a
> cada 2 segundos, pela mesma conexão (`evidencias/streaming/`).

**1. No lab anterior o Multicast alcançava vários clientes com um endereço de grupo; aqui o
streaming é o servidor conversando com um cliente por vez, numa conexão só. Se você quisesse que
vários clientes gRPC recebessem os mesmos avisos ao mesmo tempo, o que mudaria no servidor?**

Do jeito que está, cada cliente que chama `AcompanharAvisos` recebe seu **próprio** streaming
independente: o servidor entra no laço e gera os 5 avisos só pra aquele `StreamObserver` (Java) /
aquele gerador (Python). Se dois clientes se inscrevem, cada um tem sua sequência separada.

Pra virar um "aviso pra todos ao mesmo tempo", eu teria que fazer no servidor o mesmo que fiz no
**mural WebSocket** do lab anterior: manter uma **lista dos clientes inscritos** (guardar cada
`StreamObserver`/contexto quando o cliente chama `AcompanharAvisos`) e, quando um aviso for gerado,
**percorrer essa lista mandando o mesmo aviso pra cada um** (um `onNext` por observador). Ou seja,
a produção do aviso deixaria de ser local a cada chamada e passaria a ser central, com um
*fan-out* pra todos os observadores registrados — além de tratar quem desconecta (tirar da lista).
O gRPC sozinho não faz esse "um-pra-muitos" como o multicast fazia pela rede; eu que teria que
implementar o broadcast na aplicação.

**2. Compare o streaming em Java (`StreamObserver` chamando `onNext()`) com o de Python (função
geradora com `yield`). Qual achou mais natural? Justifique.**

Achei o **Python mais natural de ler**. No Python eu só escrevo um `for` com `yield` a cada aviso,
como se a função "devolvesse vários valores" — o gRPC transforma cada `yield` num envio, e o "mandar
pela rede" fica implícito. No Java é mais explícito e verboso: eu chamo `observador.onNext(aviso)`
pra cada aviso, `observador.onCompleted()` no fim e trato erro com `observador.onError(...)`.

Dito isso, a explicitação do Java tem um lado bom: fica claro **exatamente** quando cada mensagem é
empurrada e quando o stream termina — no Python isso está escondido no mecanismo do gerador. Então:
`yield` é mais gostoso de escrever, mas o `StreamObserver` deixa o ciclo de vida do stream (enviar /
terminar / dar erro) mais visível.

**3. O que acontece se o cliente fechar a conexão no meio dos 5 avisos? Teste ou pesquise e
descreva.**

Testei: subi o servidor e o cliente em Python e **matei o cliente** depois de ele receber 2 avisos.
O que observei: o **servidor não quebrou** nem imprimiu erro — ele simplesmente parou de ter pra
quem enviar. O gRPC percebe que o cliente sumiu e **cancela** aquela chamada de streaming (o stream
fica inativo/cancelado). Ou seja, o trabalho daquele streaming é interrompido, mas o servidor
continua no ar atendendo outros. Na prática, se eu quisesse economizar, dá pra o servidor detectar
esse cancelamento (por exemplo checando se o contexto/cliente ainda está ativo antes de gerar o
próximo aviso) e parar o laço mais cedo, em vez de continuar tentando enviar pra alguém que já foi.
