#!/usr/bin/env bash
#
# Compila e executa as partes do roteiro "Threads em Java".
# Não requer Maven/Gradle — apenas o JDK 21+ (javac/java) no PATH.
#
# Uso:
#   ./run.sh compile          # compila tudo em ./out
#   ./run.sh a  [args...]     # executa a Parte A (extends Thread)
#   ./run.sh b  [args...]     # executa a Parte B (implements Runnable)
#   ./run.sh c  [args...]     # executa a Parte C (muitas threads de SO)
#   ./run.sh d  [args...]     # executa a Parte D (ExecutorService)  ex.: ./run.sh d --cached
#   ./run.sh e  [args...]     # executa a Parte E (Virtual Threads)
#   ./run.sh all              # executa A, B, D em sequência (C e E são pesadas)
#   ./run.sh clean            # remove ./out
#
set -euo pipefail

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SRC="$RAIZ/src"
OUT="$RAIZ/out"
PKG="br.pucminas.labdamd.threads"

exigir_jdk21() {
  if ! command -v javac >/dev/null 2>&1; then
    echo "ERRO: 'javac' não encontrado. Instale o JDK 21+." >&2
    exit 1
  fi
  local versao
  # Isola a linha "javac XX.Y.Z" (ignorando banners como JAVA_TOOL_OPTIONS) e
  # extrai o número maior da versão.
  versao="$(javac -version 2>&1 | grep -oE 'javac[[:space:]]+[0-9]+' | grep -oE '[0-9]+' | head -1)"
  if [ -z "$versao" ]; then
    echo "AVISO: não foi possível detectar a versão do javac; prosseguindo." >&2
    return 0
  fi
  if [ "$versao" -lt 21 ]; then
    echo "ERRO: JDK 21+ é necessário (Parte E usa Virtual Threads). Encontrado: $versao." >&2
    exit 1
  fi
}

compilar() {
  exigir_jdk21
  echo ">> Compilando para $OUT ..."
  rm -rf "$OUT"
  mkdir -p "$OUT"
  # Lista todos os .java e compila de uma vez.
  find "$SRC" -name '*.java' > "$OUT/fontes.txt"
  javac -d "$OUT" @"$OUT/fontes.txt"
  echo ">> Compilação concluída."
}

garantir_compilado() {
  if [ ! -d "$OUT" ]; then
    compilar
  fi
}

executar() {
  local main_classe="$1"; shift
  garantir_compilado
  # Força UTF-8 na saída para preservar acentos mesmo quando a saída é
  # redirecionada/encanada (Java 18+ define stdout.encoding pelo console).
  java -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -cp "$OUT" "$main_classe" "$@"
}

comando="${1:-}"
[ $# -gt 0 ] && shift || true

case "$comando" in
  compile) compilar ;;
  a) executar "$PKG.partea.MainParteA" "$@" ;;
  b) executar "$PKG.parteb.MainParteB" "$@" ;;
  c) executar "$PKG.partec.MainParteC" "$@" ;;
  d) executar "$PKG.parted.MainParteD" "$@" ;;
  e) executar "$PKG.partee.MainParteE" "$@" ;;
  all)
    executar "$PKG.partea.MainParteA"
    echo; executar "$PKG.parteb.MainParteB"
    echo; executar "$PKG.parted.MainParteD"
    ;;
  clean) rm -rf "$OUT"; echo ">> ./out removido." ;;
  *)
    echo "Uso: ./run.sh {compile|a|b|c|d|e|all|clean} [args...]" >&2
    exit 1
    ;;
esac
