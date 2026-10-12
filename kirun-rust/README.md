# kirun-rust

Rust runtime for **KIRun** (Kinetic Instructions), the function-graph engine behind
Modlix. It is a port of [kirun-js](https://github.com/modlix-india/nocode-kirun/tree/master/kirun-js) and runs the same JSON function
definitions with the same results.

## Status

Complete port of kirun-js: schema validation and conversion, the expression engine,
every built-in function (System, Context, Loop, JSON, Math, String, Array, Object,
Date), `KIRuntime` with the debug collector, and the DSL compiler and decompiler.

* **Tests:** 769 tests, ported from every file in `kirun-js/__tests__` plus the DSL
  cases that exist only in kirun-py. All pass.
* **DSL conformance with kirun-js:** run over the 14,303 stored functions in
  `../test-data/functions`, kirun-rust's compiled JSON is identical for all 14,299
  that kirun-js can process. Decompiled text is identical for 14,298; the one
  difference is listed below.

## Performance

The same workloads and JSON definitions on all three runtimes (Apple Silicon, macOS; median ms,
lower is better). Details and how to rerun: [bench/README.md](https://github.com/modlix-india/nocode-kirun/blob/master/kirun-rust/bench/README.md).

| Workload (median ms) | js | java | rust | rust+mimalloc | rust+mimalloc vs js |
| --- | --- | --- | --- | --- | --- |
| Fibonacci (n=1000) | 4988.9 | 516.0 | 140.9 | 107.9 | 46.2x faster |
| Loop sum (100k) | 1457.0 | 545.5 | 376.3 | 232.5 | 6.3x faster |
| Expression x 200k | 402.7 | 245.9 | 523.7 | 346.8 | 1.2x faster |
| Schema (5,000 records) | 30.4 | 59.4 | 23.4 | 16.5 | 1.8x faster |
| DSL round trip (14k functions) | 1003.4 | n/a | 1208.4 | 791.8 | 1.3x faster |
| Peak memory (fibonacci, loopsum, expression, schema) | 245 MB | 1986 MB | 120 MB | 73 MB | |

The engine allocates many small short-lived values, which V8 and the JVM handle with cheap bump
allocation. With the system allocator Rust is fastest on Fibonacci, loop sum and schema, and behind
on expressions and the DSL; an application that sets `mimalloc` as its global allocator
(`#[global_allocator]`) gets the `rust+mimalloc` column, ahead of kirun-js everywhere and of
kirun-java everywhere except expressions.

## Install

```toml
[dependencies]
kirun = "1"
```

or `cargo add kirun`. The library is used as `kirun` (`use kirun::runtime::KIRuntime;`).

## Layout

The source tree mirrors `kirun-js/src/engine` file for file. See
[DESIGN.md](https://github.com/modlix-india/nocode-kirun/blob/master/kirun-rust/DESIGN.md) for the naming, value and concurrency rules.

```
src/json/schema      Schema model, validators, convertors
src/runtime          KIRuntime, expression engine, token extractors, debug
src/function/system  Built-in functions (Array, String, Math, Date, Object, Loop, Context, JSON)
src/model            FunctionDefinition, Statement, Parameter, Event, ...
src/repository       KIRunFunctionRepository, KIRunSchemaRepository
src/dsl              DSL text <-> FunctionDefinition JSON
tests/<area>         One integration test binary per area, ported from kirun-js/__tests__
examples             dsl_conformance: compares DSL output with kirun-js
```

## Build and test

```bash
cargo build
cargo test                          # everything
cargo test --test array_functions   # one area
```

DSL conformance against kirun-js (needs `../kirun-js/dist` built and the stored
functions in `../test-data/functions`):

```bash
node scripts/js_dsl_dump.js /tmp/js_dsl.jsonl
cargo run --release --example dsl_conformance -- /tmp/js_dsl.jsonl
```

## Quick start

```rust
use std::sync::Arc;
use kirun::function::Function;
use kirun::model::FunctionDefinition;
use kirun::repository::{KIRunFunctionRepository, KIRunSchemaRepository};
use kirun::runtime::{FunctionExecutionParameters, KIRuntime};

async fn run(json: serde_json::Value) -> kirun::Result<()> {
    let fd = FunctionDefinition::from_value(&json)?;
    let runtime = KIRuntime::new(fd, false)?;
    let mut fep = FunctionExecutionParameters::new(
        Arc::new(KIRunFunctionRepository::new()),
        Arc::new(KIRunSchemaRepository::new()),
    )
    .set_arguments(kirun::Map::from([("a".to_string(), serde_json::json!(2))]));
    let mut output = runtime.execute(&mut fep).await?;
    let event = output.next(); // EventResult { name: "output", result: {...} }
    Ok(())
}
```

## Where kirun-rust differs from kirun-js

All of these are deliberate. Each is also commented in the code.

**Values**
* JS `undefined` and `null` are both `Value::Null`. As a result, `String(null)` in an
  expression gives `"undefined"`. The DSL still keeps the difference: a ref with no
  `value` key prints and compiles as `undefined`.
* JSON has no `Infinity` or `NaN`. JS keeps `Infinity` in memory and only writes
  `null` when serialising; Rust stores `null` immediately. This affects arithmetic
  past `f64::MAX`, such as the Fibonacci runtime test after the 1,477th term.

**Built-in functions**
* `JSONParse` error messages are modelled on V8's wording, not copied from V8.
* Date functions port luxon 3.7 on top of chrono and chrono-tz:
  * Month names, weekday names, localized date formats and relative time cover
    English and French; other locales fall back to English.
  * Zone names (`GetTimeZoneOffsetLong`/`Short`) use a small built-in table.
  * `GetNames(TIMEZONES)` lists the chrono-tz zone names instead of Node's
    `Intl` zone list.

**DSL**
* A single-entry type array (`{"type": ["INTEGER"]}`) prints as `INTEGER`, following
  the kirun-py fix; kirun-js prints it as JSON. The one decompile difference in the
  conformance run is this case.
* Integer literals stay integers and literals written with a fraction stay floats,
  so a round trip keeps int vs float (as kirun-py does).
* `dependentStatements` stored as a list of keys is accepted (kirun-py fix).

**Kept from JS on purpose**
* `exclusiveMaximum` passes a value equal to the limit.

`anyOf` / `allOf` / `oneOf` are checked as kirun-java checks them (kirun-js was fixed to
match): anyOf returns the value as validated by the first matching schema; allOf and
oneOf return the original value.
