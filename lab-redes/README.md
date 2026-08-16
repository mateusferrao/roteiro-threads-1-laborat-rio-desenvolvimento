# Central de Avisos da Turma — Lab de Redes (Roteiro 2)

Roteiro 2 de laboratório (Revisão de Redes) - PUC Minas, LabDAMD.

A ideia é a mesma "central de avisos da turma" feita de 4 jeitos diferentes, cada um em
**Java e Python**, pra sentir na prática por que cada protocolo existe:

| Parte | Protocolo | O que representa |
|---|---|---|
| A | TCP | conversa privada e confiável com o monitor |
| B | UDP | o mesmo pedido, mas "solto", sem garantia de entrega |
| C | Multicast | o professor avisa todo mundo do grupo de uma vez |
| D | WebSocket | mural de avisos em tempo real, vários conectados juntos |

As respostas das 12 perguntas (3 por parte) estão no [`RESPOSTAS.md`](RESPOSTAS.md).

## Sobre uso de IA (transparência)

Este roteiro foi implementado com apoio do Claude (Anthropic), usado pra escrever e revisar o
código e o texto. Entendo o que cada parte faz e consigo explicar/defender qualquer trecho.

## Portas (OFFSET = 39)

O enunciado pede somar os 2 últimos dígitos do RA à porta-base de cada parte pra não colidir
com colegas na rede. Meu OFFSET é **39**, então:

| Parte | Base | Porta usada |
|---|---|---|
| A — TCP | 5000 | **5039** |
| B — UDP | 5001 | **5040** |
| C — Multicast | 4446 | **4485** |
| D — WebSocket (Java) | 8887 | **8926** |
| D — WebSocket (Python) | 8888 | **8927** |

O grupo multicast continua `230.0.0.1` (o que isola o tráfego é a porta).

## O que precisa instalar

- **Java JDK 17+** (`java -version`) — testei com o 21
- **Maven 3.8+** (`mvn -version`) — só na Parte D (WebSocket Java)
- **Python 3.10+** (`python --version`) e a lib `websockets` (`pip install websockets`) — só na Parte D
- **Git**

> O enunciado assume Windows/PowerShell. Os comandos abaixo estão nas duas versões quando muda
> algo (o único que muda de verdade é o separador de classpath do Java: `;` no Windows, `:` no
> Linux/Mac).

## Como rodar cada parte

Sempre são **dois terminais** (servidor num, cliente no outro). Se acento aparecer trocado no
Windows, rode `chcp 65001` antes, ou use o Windows Terminal.

### A — TCP
```powershell
cd java/tcp
javac ServidorTCP.java ClienteTCP.java
java ServidorTCP      # terminal 1
java ClienteTCP       # terminal 2
```
```powershell
cd python/tcp
python servidor_tcp.py   # terminal 1
python cliente_tcp.py    # terminal 2
```
Manda `hora` pra ver o horário do servidor e `sair` pra encerrar.

### B — UDP
```powershell
cd java/udp
javac ServidorUDP.java ClienteUDP.java
java ServidorUDP      # terminal 1
java ClienteUDP       # terminal 2
```
```powershell
cd python/udp
python servidor_udp.py   # terminal 1
python cliente_udp.py    # terminal 2
```

### C — Multicast (abra o cliente primeiro, de preferência 2 ou 3)
```powershell
cd java/multicast
javac ServidorMulticast.java ClienteMulticast.java
java ClienteMulticast    # terminal(is) 1..n, primeiro
java ServidorMulticast   # depois
```
```powershell
cd python/multicast
python cliente_multicast.py    # primeiro
python servidor_multicast.py   # depois
```
Se o cliente não receber nada, veja a seção 6.5 do enunciado (VPN/Wi-Fi/WSL costumam bloquear
multicast). Pra testar tudo na mesma máquina o Python já funciona; no Java, se precisar, troque
a interface de rede pelo loopback (tem comentário no código).

### D — WebSocket
Java (o servidor usa a lib Java-WebSocket via Maven; o cliente é a API nativa do JDK):
```powershell
cd java/websocket
mvn compile exec:java "-Dexec.mainClass=MuralServidor"   # terminal 1
mvn compile exec:java "-Dexec.mainClass=MuralCliente"    # terminal 2 e 3
```
Python (precisa de `pip install websockets`):
```powershell
cd python/websocket
python mural_servidor.py    # terminal 1
python mural_cliente.py     # terminal 2 e 3
```

## Evidências

A pasta `evidencias/` tem, pra cada parte, a **saída real** dos programas que eu rodei
(arquivos `.txt`). Pra entrega oficial, o enunciado pede **print de tela (.png)** da execução
na sua máquina, com os terminais lado a lado e o `Get-Date` visível — veja
[`evidencias/COMO-CAPTURAR.md`](evidencias/COMO-CAPTURAR.md).
