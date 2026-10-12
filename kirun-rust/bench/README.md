# KIRun benchmarks

The same workloads on kirun-js, kirun-java and kirun-rust, from the same JSON files.

| Workload | Definition | Exercises |
|---|---|---|
| `fibonacci` | `defs/fibonacci.json` (the kirun-js Fibonacci test), n = 1000 | runtime, loops, If, Set, array insert |
| `loopsum` | `defs/loopsum.json`, 100,000 iterations | loop overhead, context writes, expressions |
| `expression` | one expression from `config.json`, 200,000 evaluations | expression engine |
| `schema` | `defs/record_schema.json` over 5,000 records | schema validation |
| `dsl` | decompile + compile of every function in `../../test-data/functions` | DSL (no Java DSL) |

Each workload is warmed up, then timed; the report shows the median. Counts are in `config.json`.

```bash
bench/run.sh          # all runtimes, prints a Markdown table; raw numbers in bench/results/
```

Single runtimes:

```bash
cargo run --release --example bench [workload...]                      # Rust, system allocator
cargo run --release --example bench --features mimalloc [workload...]  # Rust, mimalloc
node bench/bench.js [workload...]                                       # kirun-js (needs dist)
```

`KIRUN_BENCH_RUNS=<n>` overrides the run count (useful to keep a profiler attached).

Rust is measured with both the system allocator and mimalloc: the engine allocates many small
short-lived values, which the JVM and V8 handle with cheap bump allocation. A library cannot pick
the allocator; an application embedding kirun can (`#[global_allocator]`).
