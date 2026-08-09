package br.pucminas.labdamd.threads.comum;

// Cronometro simples pra medir quanto tempo cada parte leva.
// Uso o nanoTime() porque ele serve pra medir intervalo (nao muda se o
// relogio do sistema for ajustado).
public final class Cronometro {

    private final long inicioNanos;

    private Cronometro(long inicioNanos) {
        this.inicioNanos = inicioNanos;
    }

    // comeca a contar agora
    public static Cronometro iniciar() {
        return new Cronometro(System.nanoTime());
    }

    // tempo passado desde o iniciar(), em milissegundos
    public long decorridoMillis() {
        return (System.nanoTime() - inicioNanos) / 1_000_000L;
    }

    // formata tipo "1234 ms (~1,2 s)"
    public String decorridoFormatado() {
        long ms = decorridoMillis();
        return String.format("%d ms (~%.1f s)", ms, ms / 1000.0);
    }
}
