# KIRun Documentation

Documentation for KIRun (Kinetic Instruction Runtime): functions stored as data, run the same way on
JavaScript, Java, Rust and Python. It is also published at
[kirun.modlix.com/docs](https://kirun.modlix.com/docs), beside the
[playground](https://kirun.modlix.com/playground) where every example can be run in the browser.

New to KIRun? The [Learn](https://kirun.modlix.com/learn) tour is twelve small functions you can run in the
browser, and the [Examples](https://kirun.modlix.com/examples) are more of them.

## Contents

### Getting Started

- [Architecture](./architecture.md) - Core concepts, data model, execution flow, and repository pattern
- [Visual Editor (kirun-ui)](./visual-editor.md) - Integrating the visual node editor and DSL text editor
- [Expressions](./expressions.md) - Expression syntax, operators, precedence, and nested expressions

### Reference

- [Built-in Functions](./functions.md) - Reference for the 166 built-in functions across 9 namespaces
- [Schema & Types](./schemas.md) - Type system, JSON Schema validation, and schema composition
- [JSON Definition Format](./json-format.md) - Full JSON format specification for function definitions

### Advanced

- [Custom Functions](./custom-functions.md) - Building custom functions in JavaScript/TypeScript and Java
- [DSL](./dsl.md) - Human-readable text format that compiles to/from JSON definitions
- [Debug Mode](./debugging.md) - Execution tracing, event listeners, and error tracking

## Quick Reference

### Installation

```bash
npm install @fincity/kirun-js     # JavaScript / TypeScript
cargo add kirun                   # Rust
pip install kirun-py              # Python
npm install @fincity/kirun-ui     # the visual editor (React)
```

```xml
<!-- Java 21+ (Maven Central) -->
<dependency>
    <groupId>com.fincity.nocode</groupId>
    <artifactId>kirun-java</artifactId>
    <version><!-- latest --></version>
</dependency>
```

### Runtimes

| Runtime | Package | Read more |
|---------|---------|-----------|
| JavaScript / TypeScript | `@fincity/kirun-js` | [kirun-js](../kirun-js/README.md) |
| Java | `com.fincity.nocode:kirun-java` | [kirun-java](../kirun-java/README.md) |
| Rust | `kirun` | [kirun-rust](../kirun-rust/README.md) |
| Python | `kirun-py` | [kirun-py](../kirun-py/README.md) |
| Visual editor | `@fincity/kirun-ui` | [kirun-ui](../kirun-ui/README.md) |

### Minimal Example

```typescript
import {
    FunctionDefinition,
    KIRuntime,
    FunctionExecutionParameters,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
} from '@fincity/kirun-js';

const fd = FunctionDefinition.from(myFunctionJson);
const runtime = new KIRuntime(fd);
const fep = new FunctionExecutionParameters(
    new KIRunFunctionRepository(),
    new KIRunSchemaRepository(),
).setArguments(new Map([['key', 'value']]));

const result = await runtime.execute(fep);
```

### Key Imports

```typescript
// Core
import { KIRuntime, FunctionDefinition, FunctionExecutionParameters } from '@fincity/kirun-js';

// Repositories
import { KIRunFunctionRepository, KIRunSchemaRepository, HybridRepository } from '@fincity/kirun-js';

// Models
import { Statement, Parameter, ParameterReference, Event, EventResult, FunctionOutput } from '@fincity/kirun-js';

// Schema
import { Schema, SchemaType, SchemaValidator } from '@fincity/kirun-js';

// Expressions
import { ExpressionEvaluator, Expression, TokenValueExtractor } from '@fincity/kirun-js';

// Custom functions
import { AbstractFunction, FunctionSignature } from '@fincity/kirun-js';

// DSL
import { DSLCompiler } from '@fincity/kirun-js';

// Debug
import { DebugCollector } from '@fincity/kirun-js';
```

### Expression Cheat Sheet

```
Arguments.param              → Access function parameter
Steps.step.output.prop       → Access step output
Context.variable             → Access context variable
Steps.step.iteration.index   → Loop iteration index
Steps.step.iteration.each    → Loop current element

a + b, a - b, a * b, a / b  → Arithmetic
a = b, a != b, a < b, a > b → Comparison
a and b, a or b, not a       → Logical
a ?? b                        → Nullish coalescing
a ? b : c                    → Ternary
arr[0], arr[1..3]            → Array access/range
obj["key.with.dots"]         → Bracket notation
{{expr}}                     → Nested expression
```

### Function Namespaces

Counted in kirun-js; browse and run each one in the [function catalog](https://kirun.modlix.com/functions).

| Namespace | Count | Some of them |
|-----------|------:|--------------|
| `System` | 6 | If, GenerateEvent, Make, Print, Wait, ValidateSchema |
| `System.Math` | 32 | Add, Absolute, Round, Power, Minimum, Maximum, Random, Hypotenuse, ... |
| `System.String` | 33 | Concatenate, Split, Replace, SubString, Matches, PrePad, PostPad, ToString, ... |
| `System.Array` | 31 | AddFirst, InsertLast, Delete, Sort, Reverse, IndexOf, SubArray, Join, ... |
| `System.Object` | 6 | ObjectKeys, ObjectValues, ObjectEntries, ObjectPutValue, ObjectDeleteKey, ObjectConvert |
| `System.Date` | 49 | GetCurrentTimestamp, AddTime, SubtractTime, Difference, FromNow, StartOf, IsBefore, ... |
| `System.Loop` | 4 | CountLoop, ForEachLoop, RangeLoop, Break |
| `System.Context` | 3 | Create, Get, Set |
| `System.JSON` | 2 | JSONParse, JSONStringify |

kirun-java adds `System.WaitUntil` and `System.WaitForSignal`, which stop an execution so a host can
resume it later (see [Stopping and resuming](./architecture.md#stopping-and-resuming-an-execution)).
