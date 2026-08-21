# Central de Atendimento da Turma via gRPC (Roteiro 3)

Roteiro 3 de laboratório - PUC Minas, LabDAMD, U1 (Introdução a Aplicações Distribuídas).

É o mesmo cenário da "central da turma" do roteiro anterior (o de redes), só que agora a
comunicação não é feita "na mão" com sockets: a gente define um **contrato** num arquivo
`.proto` e o **gRPC** gera o código de cliente/servidor automaticamente. A ideia é sentir na
prática o quanto de **transparência** um framework de RPC dá "de graça".

As respostas (reflexão da Parte A + as 12 perguntas) estão no [`RESPOSTAS.md`](RESPOSTAS.md).

| Parte | Conteúdo |
|---|---|
| A | Transparências em sistemas distribuídos (conceitual + comparação com o lab de redes) |
| B | Protocol Buffers: o contrato `central.proto` e a geração dos stubs |
| C | RPC unário — `ConsultarHorario` (uma pergunta, uma resposta) |
| D | RPC com streaming de servidor — `AcompanharAvisos` (uma inscrição, vários avisos) |

> Pré-requisito: o roteiro anterior (redes) está em [`../lab-redes/`](../lab-redes/) — a Parte A
> compara os dois.

## Sobre uso de IA (transparência)

Implementado com apoio do Claude (Anthropic), usado para escrever e revisar código e texto.
Entendo o que cada parte faz e consigo explicar/defender qualquer trecho.

## Portas (OFFSET = 39)

| Servidor | Base | Porta usada |
|---|---|---|
| gRPC — Java | 50051 | **50090** |
| gRPC — Python | 50061 | **50100** |

As bases já são diferentes de propósito, então dá pra rodar os dois servidores ao mesmo tempo.

## O que precisa instalar

- **Java JDK 17+** e **Maven 3.8+** (o Maven baixa o `protoc` e o plugin do gRPC sozinho na
  primeira compilação — pode demorar).
- **Python 3.10+** e as libs: `pip install grpcio grpcio-tools`.
- **Git**.

## Gerar os stubs a partir do `.proto`

**Java** (gera em `target/generated-sources/`, não precisa versionar nem editar):
```powershell
cd java/grpc-central
mvn compile
```

**Python** (gera `central_pb2.py` e `central_pb2_grpc.py` na pasta):
```powershell
cd python/grpc_central
pip install grpcio grpcio-tools
python -m grpc_tools.protoc -I ../../proto --python_out=. --grpc_python_out=. ../../proto/central.proto
```
Rode o comando do Python de novo sempre que mudar o `.proto`.

## Como rodar (sempre 2 terminais: servidor e cliente)

**Java** (porta 50090):
```powershell
cd java/grpc-central
mvn compile exec:java "-Dexec.mainClass=br.pucminas.labdamd.central.ServidorCentral"   # terminal 1
mvn compile exec:java "-Dexec.mainClass=br.pucminas.labdamd.central.ClienteCentral"    # terminal 2
```

**Python** (porta 50100):
```powershell
cd python/grpc_central
python servidor_central.py    # terminal 1
python cliente_central.py     # terminal 2
```

O cliente pergunta seu nome, faz a chamada unária (`ConsultarHorario`) e depois se inscreve no
streaming (`AcompanharAvisos`), recebendo 5 avisos, um a cada 2 segundos.

## Evidências

A pasta `evidencias/` tem a **saída real** dos programas que rodei (`.txt`). Para a entrega, o
enunciado pede **prints (.png)** de execução real com o `Get-Date` visível — veja
[`evidencias/COMO-CAPTURAR.md`](evidencias/COMO-CAPTURAR.md). A Parte A é conceitual (sem print);
a evidência dela é a resposta no `RESPOSTAS.md`.
