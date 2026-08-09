package br.pucminas.labdamd.threads.comum;

/**
 * Cronômetro simples baseado em {@link System#nanoTime()} para medir o tempo
 * de parede (<em>wall-clock time</em>) das simulações do roteiro.
 *
 * <p>Usamos {@code nanoTime()} — e não {@code currentTimeMillis()} — porque ele
 * é monotônico e próprio para medir intervalos: não é afetado por ajustes do
 * relógio do sistema (NTP, horário de verão etc.).</p>
 *
 * <p>A classe é imutável: {@link #iniciar()} captura o instante inicial e cada
 * chamada de {@link #decorridoMillis()} calcula o intervalo até agora.</p>
 */
public final class Cronometro {

    private final long inicioNanos;

    private Cronometro(long inicioNanos) {
        this.inicioNanos = inicioNanos;
    }

    /** Cria e dispara um novo cronômetro no instante atual. */
    public static Cronometro iniciar() {
        return new Cronometro(System.nanoTime());
    }

    /** Tempo decorrido desde {@link #iniciar()}, em milissegundos. */
    public long decorridoMillis() {
        return (System.nanoTime() - inicioNanos) / 1_000_000L;
    }

    /** Tempo decorrido formatado como {@code "1234 ms (~1,2 s)"}. */
    public String decorridoFormatado() {
        long ms = decorridoMillis();
        return String.format("%d ms (~%.1f s)", ms, ms / 1000.0);
    }
}
