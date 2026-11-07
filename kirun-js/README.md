<p align="center">
  <a href="https://kirun.modlix.com">
    <picture>
      <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/modlix-india/nocode-kirun/master/.github/assets/logo-dark.png">
      <img src="https://raw.githubusercontent.com/modlix-india/nocode-kirun/master/.github/assets/logo-light.png" alt="kirun" width="280">
    </picture>
  </a>
</p>

<h3 align="center">kirun-js: the JavaScript / TypeScript runtime for KIRun</h3>

<p align="center">
  <a href="https://kirun.modlix.com/playground"><strong>Playground</strong></a> ·
  <a href="https://kirun.modlix.com/docs">Docs</a> ·
  <a href="https://kirun.modlix.com/functions">Functions</a> ·
  <a href="https://github.com/modlix-india/nocode-kirun">GitHub</a>
</p>

<p align="center">
  <a href="https://www.npmjs.com/package/@fincity/kirun-js"><img src="https://img.shields.io/npm/v/@fincity/kirun-js?style=flat-square&color=7AA2F7&labelColor=0A0B11" alt="npm"></a>
  <a href="https://github.com/modlix-india/nocode-kirun/blob/master/LICENSE"><img src="https://img.shields.io/badge/license-MIT-9ECE6A?style=flat-square&labelColor=0A0B11" alt="MIT License"></a>
</p>

---

KIRun runs functions that are stored as data: a JSON definition with parameters, events and a graph
of steps. kirun-js runs those definitions in the browser and on Node.js, compiles the KIRun DSL to
JSON and back, and records a per-step trace in debug mode. The same definitions run on
[Java](https://github.com/modlix-india/nocode-kirun/tree/master/kirun-java),
[Rust](https://github.com/modlix-india/nocode-kirun/tree/master/kirun-rust) and
[Python](https://github.com/modlix-india/nocode-kirun/tree/master/kirun-py).

The [KIRun playground](https://kirun.modlix.com/playground) runs on this package, in your tab.

## Install

```bash
npm install @fincity/kirun-js
```

## Run a function

```typescript
import {
    FunctionDefinition,
    FunctionExecutionParameters,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
    KIRuntime,
} from '@fincity/kirun-js';

const fd = await FunctionDefinition.fromText(`
FUNCTION Fizz
    PARAMETERS
        n AS INTEGER
    LOGIC
        check: System.If(condition = Arguments.n % 3 = 0)
            true
                yes: System.GenerateEvent(results = {name: "say", value: "Fizz"}) AFTER Steps.check.true
            false
                no: System.GenerateEvent(results = {name: "say", value: Arguments.n}) AFTER Steps.check.false
`);

const output = await new KIRuntime(fd).execute(
    new FunctionExecutionParameters(new KIRunFunctionRepository(), new KIRunSchemaRepository())
        .setArguments(new Map([['n', 9]])),
);

console.log(output.next()?.getResult()); // Map { 'say' => 'Fizz' }
```

A stored definition loads with `FunctionDefinition.from(json)`. The JSON format is described in
[JSON definition format](https://kirun.modlix.com/docs/json-format).

## What is in the package

| | |
| --- | --- |
| `KIRuntime` | Executes a `FunctionDefinition`. `new KIRuntime(fd, true)` turns on debug mode. |
| `FunctionExecutionParameters` | Arguments, repositories and context for one run. |
| `KIRunFunctionRepository`, `KIRunSchemaRepository` | The 166 built-in functions and their schemas. |
| `HybridRepository` | Puts your own functions and schemas beside the built-ins. |
| `AbstractFunction` | Base class for a function written in code. |
| `DSLCompiler` | `compile`, `decompile`, `validate` (with line and column) and `format` for the DSL. |
| `ExpressionEvaluator` | Evaluates one expression on its own. |
| `DebugCollector` | The per-step log of arguments, results, events and timings. |
| `Schema` | Schemas and validation. |

## Documentation

- [Architecture](https://kirun.modlix.com/docs/architecture): definitions, steps, the execution plan
- [Expressions](https://kirun.modlix.com/docs/expressions) and the [DSL](https://kirun.modlix.com/docs/dsl)
- [Built-in functions](https://kirun.modlix.com/docs/functions), and the [searchable catalog](https://kirun.modlix.com/functions)
- [Custom functions](https://kirun.modlix.com/docs/custom-functions) and [debugging](https://kirun.modlix.com/docs/debugging)
- The visual editor for these definitions is [@fincity/kirun-ui](https://www.npmjs.com/package/@fincity/kirun-ui)

## Build

```bash
npm install
npm run build
npm test
```

## License

MIT. Built by [Modlix](https://modlix.com).
