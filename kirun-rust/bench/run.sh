#!/usr/bin/env bash
# Runs the KIRun benchmarks on kirun-rust (system allocator and mimalloc), kirun-js and
# kirun-java, then prints a comparison table. See README.md.
#
# Needs: ../../kirun-js/dist (npm run build), JDK 21 for ../../kirun-java, and for the DSL
# workload the stored functions in ../../test-data/functions (local only).
# Peak memory comes from macOS `/usr/bin/time -l`.
set -euo pipefail
BENCH="$(cd "$(dirname "$0")" && pwd)"
CRATE="$(dirname "$BENCH")"
ROOT="$(dirname "$CRATE")"
OUT="$BENCH/results"
mkdir -p "$OUT"
: > "$OUT/results.jsonl"

node "$BENCH/gen_data.js" > /dev/null

RUNTIME_WORKLOADS=(fibonacci loopsum expression schema)

# Runtime workloads in one process (its peak memory is reported), then the DSL alone: loading
# the 14k-function corpus would otherwise dominate the memory figure, and Java has no DSL.
run() { # name, command...
  local name="$1"; shift
  echo "running $name..." >&2
  /usr/bin/time -l "$@" "${RUNTIME_WORKLOADS[@]}" 2> "$OUT/$name.time" >> "$OUT/results.jsonl"
  local rss
  rss=$(awk '/maximum resident set size/ {print $1}' "$OUT/$name.time")
  echo "{\"lang\":\"$name\",\"workload\":\"_peak_rss\",\"bytes\":$rss}" >> "$OUT/results.jsonl"
  if [ "$name" != java ]; then
    "$@" dsl >> "$OUT/results.jsonl"
  fi
}

cd "$CRATE"
cargo build --release -q --example bench
cp target/release/examples/bench "$OUT/bench-rust"
cargo build --release -q --example bench --features mimalloc
cp target/release/examples/bench "$OUT/bench-rust-mimalloc"
run rust "$OUT/bench-rust"
run rust+mimalloc "$OUT/bench-rust-mimalloc"

run js node "$BENCH/bench.js"

cd "$ROOT/kirun-java"
./mvnw -q compile
./mvnw -q dependency:build-classpath -Dmdep.outputFile="$OUT/java-cp.txt"
CP="target/classes:$(cat "$OUT/java-cp.txt")"
mkdir -p "$OUT/java-classes"
javac -d "$OUT/java-classes" -cp "$CP" "$BENCH/java/Bench.java"
run java java -cp "$OUT/java-classes:$CP" Bench "$BENCH"

node "$BENCH/report.js" "$OUT/results.jsonl"
