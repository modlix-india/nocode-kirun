# KIRun Documentation

Comprehensive documentation for the KIRun (Kinetic Instruction Runtime) engine.

## Table of Contents

### Getting Started

- [Architecture](./architecture.md) - Core concepts, data model, execution flow, and repository pattern
- [Visual Editor (kirun-ui)](./visual-editor.md) - Integrating the visual node editor and DSL text editor
- [Expressions](./expressions.md) - Expression syntax, operators, precedence, and nested expressions

### Reference

- [Built-in Functions](./functions.md) - Complete reference for all 120+ built-in functions across 9 namespaces
- [Schema & Types](./schemas.md) - Type system, JSON Schema validation, and schema composition
- [JSON Definition Format](./json-format.md) - Full JSON format specification for function definitions

### Advanced

- [Custom Functions](./custom-functions.md) - Building custom functions in JavaScript/TypeScript and Java
- [DSL](./dsl.md) - Human-readable text format that compiles to/from JSON definitions
- [Debug Mode](./debugging.md) - Execution tracing, event listeners, and error tracking

## Quick Reference

### Installation

```bash
# JavaScript/TypeScript
npm install @fincity/kirun-js

# Java (Maven)
<dependency>
    <groupId>com.fincity.nocode</groupId>
    <artifactId>kirun-java</artifactId>
    <version>4.3.0</version>
</dependency>
```

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

| Namespace | Functions |
|-----------|-----------|
| `System` | If, GenerateEvent, Make, Print, Wait, ValidateSchema |
| `System.Math` | Add, Minimum, Maximum, Random, RandomAny, Hypotenuse |
| `System.String` | Concatenate, Split, Reverse, Matches, PrePad, PostPad, TrimTo, ToString, ... |
| `System.Array` | AddFirst, InsertLast, Delete, Sort, Reverse, IndexOf, SubArray, Join, ... (33 functions) |
| `System.Object` | ObjectKeys, ObjectValues, ObjectEntries, ObjectPutValue, ObjectDeleteKey, ObjectConvert |
| `System.Date` | GetCurrent, FromNow, AddSubtractTime, Difference, EpochToTimestamp, ... (16 functions) |
| `System.Loop` | CountLoop, ForEachLoop, RangeLoop, Break |
| `System.Context` | Create, Get, Set |
| `System.Json` | JSONParse, JSONStringify |
