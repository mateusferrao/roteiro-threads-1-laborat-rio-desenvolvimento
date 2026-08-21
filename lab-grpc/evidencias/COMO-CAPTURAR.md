# Como capturar as evidências (prints) para a entrega

O enunciado pede **4 prints de tela** (`.png`/`.jpg`) de execução real, com a saída do
comando `Get-Date` visível em algum terminal (prova de que é seu e recente). A Parte A é
conceitual — não tem print; a evidência dela é a resposta no `RESPOSTAS.md`.

> Os arquivos `.txt` nesta pasta são a **saída real** dos programas quando rodei aqui. Servem
> de conferência, mas **não substituem o print** — os `.png` você gera na sua máquina.

## Os 4 prints

```
evidencias/unario/unario-java.png        evidencias/unario/unario-python.png
evidencias/streaming/streaming-java.png  evidencias/streaming/streaming-python.png
```

## Passo a passo (Windows / PowerShell)

Para cada print, abra **2 terminais** (servidor e cliente), rode `Get-Date` uma vez em um deles,
e capture as duas janelas lado a lado com `Win + Shift + S` (salve com o nome exato acima).

### Unário (`ConsultarHorario`)

**Java:**
```powershell
# Terminal 1
cd java/grpc-central
Get-Date
mvn compile exec:java "-Dexec.mainClass=br.pucminas.labdamd.central.ServidorCentral"
# Terminal 2
cd java/grpc-central
mvn compile exec:java "-Dexec.mainClass=br.pucminas.labdamd.central.ClienteCentral"
```
Digite seu nome no cliente; o print deve mostrar a resposta com o horário chegando. Salve como
`evidencias/unario/unario-java.png`.

**Python:** igual, em `python/grpc_central`, com `python servidor_central.py` e
`python cliente_central.py`. Salve como `evidencias/unario/unario-python.png`.

> Como o cliente faz a chamada unária **e** o streaming em sequência, dá para tirar os dois
> prints (unário e streaming) numa mesma execução: capture primeiro a resposta do horário
> (unário) e depois os avisos chegando (streaming).

### Streaming (`AcompanharAvisos`)

Mesma execução acima: depois da resposta do horário, o cliente se inscreve e recebe **5 avisos,
um a cada 2 segundos**. Espere aparecerem pelo menos 2 ou 3 avisos e tire o print:
- Java → `evidencias/streaming/streaming-java.png`
- Python → `evidencias/streaming/streaming-python.png`

## Se o Maven não baixar o `protoc` (proxy/firewall)

É a mesma causa do problema da Parte D do roteiro de redes. Documente a tentativa no
`RESPOSTAS.md` e fale com o professor sobre um espelho local ou testar em outra rede.
