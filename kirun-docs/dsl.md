# DSL (Domain-Specific Language)

KIRun includes a human-readable text format (DSL) that compiles to and from the JSON function definition format. The DSL makes function definitions easier to read and write by hand.

> **Status:** The DSL is available in kirun-js. It is a newer feature and supports the full function definition format.

## Overview

Instead of writing JSON:

```json
{
    "name": "AddNumbers",
    "namespace": "MyApp",
    "parameters": {
        "a": { "parameterName": "a", "schema": { "type": ["INTEGER"] } },
        "b": { "parameterName": "b", "schema": { "type": ["INTEGER"] } }
    },
    "events": { "output": { "name": "output", "parameters": { "result": { "type": ["INTEGER"] } } } },
    "steps": { ... }
}
```

You can write DSL:

```
FUNCTION AddNumbers
    NAMESPACE MyApp
    PARAMETERS
        a AS {"type":["INTEGER"]}
        b AS {"type":["INTEGER"]}
    EVENTS
        output
            result AS {"type":["INTEGER"]}
    LOGIC
        add: System.Math.Add(undefined = Arguments.a, undefined = Arguments.b)
            output
                event: System.GenerateEvent(eventName = "output", results = {
    "name": "result",
    "value": {
        "isExpression": true,
        "value": "Steps.add.output.value"
    }
})
```

## DSL Compiler API

### Compile (Text to JSON)

```typescript
import { DSLCompiler } from '@fincity/kirun-js';

const json = DSLCompiler.compile(dslText);
// json is a FunctionDefinition-compatible object
```

### Decompile (JSON to Text)

```typescript
const text = await DSLCompiler.decompile(functionDefinitionJson);
```

### Validate

```typescript
const result = DSLCompiler.validate(dslText);
if (!result.valid) {
    console.log(result.errors);
    // [{ message: "...", line: 5, column: 10 }]
}
```

### Format

```typescript
const formatted = await DSLCompiler.format(dslText);
```

### FunctionDefinition Integration

```typescript
import { FunctionDefinition } from '@fincity/kirun-js';

// Parse from DSL
const fd = await FunctionDefinition.fromText(dslText);

// Convert to DSL
const text = await fd.toText();
```

## Syntax Reference

### Function Declaration

The function name and namespace are declared on separate lines:

```
FUNCTION <name>
    NAMESPACE <namespace>
```

Example:

```
FUNCTION ReadPage
    NAMESPACE monkbars
```

### Parameters

Parameters are declared with the `AS` keyword followed by a JSON schema literal:

```
PARAMETERS
    <name> AS <jsonSchema>
```

Examples:

```
PARAMETERS
    a AS {"type":["INTEGER"]}
    b AS {"type":["INTEGER"]}
    name AS {"type":["STRING"]}
    number AS {"type":["INTEGER"],"version":1}
    size AS {"type":["INTEGER"],"version":1}
```

The schema is an inline JSON object matching the KIRun schema format. The `type` field is always an array of type strings.

### Events

Events define output channels with named parameters and their schemas:

```
EVENTS
    output
        <paramName> AS <jsonSchema>
    error
        message AS {"type":["STRING"]}
```

Example:

```
EVENTS
    output
        result AS {"type":["INTEGER"]}
```

Use `{}` for an untyped/any schema:

```
EVENTS
    output
        result AS {}
```

### Logic (Steps)

The `LOGIC` section defines the execution steps. Each step is a function call using colon syntax:

```
LOGIC
    <stepName>: <namespace>.<functionName>(<parameters>)
```

### Step Parameters

Parameters are passed with `=` for assignment:

```
    add: System.Math.Add(undefined = Arguments.a, undefined = Arguments.b)
```

- Parameters use `paramName = value` syntax
- Multiple parameters are comma-separated
- Use `undefined` as the parameter name for unnamed/default parameters
- Values can be expressions (`Arguments.x`, `Steps.step.output.value`), literals (`"string"`, `42`), or JSON objects
- Backticks `` ` ` `` can be used for empty or template values: `` page = ` ` ``

### Event Blocks and Nesting

Steps that produce events use indented blocks. Dependent steps are nested inside the event block they depend on:

```
    create: System.Context.Create(name = "filter", schema = {"type": "OBJECT"})
        output
            set: System.Context.Set(name = "Context.filter", value = {"field": "callType"})
                output
                    readPage: CoreServices.Storage.ReadPage(size = Arguments.size)
```

This nesting pattern replaces explicit `dependentStatements` - a step nested under an `output` block automatically depends on the parent step's output event.

Block names: `output`, `error`, `iteration`, `true`, `false`

### Dependencies with AFTER

Use `AFTER` for explicit dependencies (inline with the step):

```
    set: System.Context.Set(name = "Context.filter", value = {...}) AFTER Steps.create.output
```

### Complex Values (JSON Objects)

Pass JSON objects directly as parameter values:

```
    create: System.Context.Create(name = "filter", schema = {
    "type": "OBJECT"
})
```

### Expression References in Results

For `GenerateEvent` results that reference step outputs, use the `isExpression` pattern:

```
    generateEvent: System.GenerateEvent(results = {
    "name": "result",
    "value": {
        "isExpression": true,
        "value": "Steps.readPage.output.result"
    }
})
```

## Complete Examples

### Example 1: Add Two Numbers

```
FUNCTION AddNumbers
    NAMESPACE MyApp
    PARAMETERS
        a AS {"type":["INTEGER"]}
        b AS {"type":["INTEGER"]}
    EVENTS
        output
            result AS {"type":["INTEGER"]}
    LOGIC
        add: System.Math.Add(undefined = Arguments.a, undefined = Arguments.b)
            output
                event: System.GenerateEvent(eventName = "output", results = {
    "name": "result",
    "value": {
        "isExpression": true,
        "value": "Steps.add.output.value"
    }
})
```

### Example 2: Read Page with Context and Filtering

```
FUNCTION ReadPage
    NAMESPACE monkbars
    PARAMETERS
        number AS {"type":["INTEGER"],"version":1}
        size AS {"type":["INTEGER"],"version":1}
    EVENTS
        output
            result AS {}
    LOGIC
        create: System.Context.Create(name = "filter", schema = {
    "type": "OBJECT"
})
            output
                set: System.Context.Set(name = "Context.filter", value = {
    "field": "callType",
    "operator": "EQUALS",
    "value": "PHONE"
}) AFTER Steps.create.output
                    output
                        readPage: CoreServices.Storage.ReadPage(size = Arguments.size, appCode = "monkbars", page = ``, storageName = "table", filter = Context.filter) AFTER Steps.set.output
                            output
                                generateEvent: System.GenerateEvent(results = {
    "name": "result",
    "value": {
        "isExpression": true,
        "value": "Steps.readPage.output.result"
    }
})
```

This example demonstrates:
- **Context creation** - Creating a context variable with a schema
- **Context setting** - Setting a value on the context variable
- **Nested event blocks** - Each step's `output` block contains the next dependent step
- **AFTER declarations** - Explicit dependency ordering
- **Backtick values** - Empty value for `page` parameter using `` ` ` ``
- **Expression results** - Using `isExpression` to reference step outputs in GenerateEvent

## Key Syntax Rules

| Element | Syntax | Example |
|---------|--------|---------|
| Function name | `FUNCTION <name>` | `FUNCTION ReadPage` |
| Namespace | `NAMESPACE <ns>` (indented) | `NAMESPACE monkbars` |
| Parameter | `<name> AS <jsonSchema>` | `size AS {"type":["INTEGER"]}` |
| Event param | `<name> AS <jsonSchema>` | `result AS {}` |
| Step | `<name>: <ns>.<func>(<params>)` | `add: System.Math.Add(...)` |
| Param binding | `<param> = <value>` | `name = "filter"` |
| Event block | Indented block name | `output` (indented under step) |
| Dependency | `AFTER Steps.<step>.<event>` | `AFTER Steps.create.output` |
| JSON value | Inline JSON object | `schema = {"type": "OBJECT"}` |
| Empty value | Backticks | `` page = ` ` `` |
| Expression ref | `isExpression` JSON | `{"isExpression": true, "value": "..."}` |

## Monaco Editor Integration

KIRun includes a Monaco Editor integration for DSL syntax highlighting and auto-completion:

![DSL Editor](./images/dsl.png)

*The Monaco-based DSL editor with syntax highlighting, showing the ReadPage function from Example 2.*

```typescript
import { DSLFunctionProvider } from '@fincity/kirun-js';
```

The `DSLFunctionProvider` provides:

- Language definition for syntax highlighting
- Completion providers for function names, parameters, and types
- Hover information for built-in functions

## Compilation Pipeline

The DSL compiler follows a standard three-stage pipeline:

```
DSL Text
    │
    ▼
┌──────────┐
│  Lexer   │ ── Tokenizes text into DSL tokens
└────┬─────┘
     │
     ▼
┌──────────┐
│  Parser  │ ── Builds AST (Abstract Syntax Tree)
└────┬─────┘
     │
     ▼
┌──────────────┐
│  Transformer │ ── Converts AST to FunctionDefinition JSON
└──────────────┘
```

### AST Node Types

| Node                  | Purpose                       |
| --------------------- | ----------------------------- |
| `FunctionDefNode`   | Top-level function definition |
| `ParameterDeclNode` | Parameter declaration         |
| `EventDeclNode`     | Event declaration             |
| `StatementNode`     | Function call step            |
| `ArgumentNode`      | Step argument                 |
| `SchemaNode`        | Type definition               |
| `SchemaLiteralNode` | Inline JSON schema            |
| `ExpressionNode`    | Expression value              |
| `ComplexValueNode`  | Complex value (object/array)  |

### Decompilation

The reverse process (JSON to Text) is handled by `JSONToTextTransformer`:

```typescript
import { JSONToTextTransformer } from '@fincity/kirun-js';

const transformer = new JSONToTextTransformer();
const text = await transformer.transform(functionDefinitionJson);
```
