# KIRun (Kinetic Instruction Runtime) — Language Overview

KIRun is a **declarative, event-driven language** designed to express business logic and data-flow pipelines in a form that is equally friendly to (1) visual drag-and-drop authoring tools and (2) text-based version control.

A KIRun source file is always parsed in **one of two modes**:

| Mode                | Purpose                                                       | File extension | Entry-point symbol  |
| ------------------- | ------------------------------------------------------------- | -------------- | ------------------- |
| **Expression mode** | A single self-contained expression that evaluates to a value. | `.kir`         | _root expression_   |
| **Function mode**   | A complete function consisting of metadata and logic graph.   | `.kir`         | `FUNCTION`, `LOGIC` |

---

## 1. Design Goals

1. **Visual ⇆ Text round-tripping**
2. **Predictable side-effects**
3. **Polyglot embeddability**
4. **Schema-driven safety**
5. **No hidden control flow**

---

## 2. Lexical Structure (Common to Both Modes)

| Element         | Examples                            | Notes                                  |
| --------------- | ----------------------------------- | -------------------------------------- | --- | ------------------------- |
| Identifiers     | `total`, `Page.form.name`           | Case-sensitive; supports dotted paths. |
| String literals | `"Hello"` `"Mr. "+ Page.name`       | JSON escape rules.                     |
| Numbers         | `42`, `‑7`, `3.14`                  | Integers and decimals.                 |
| Comments        | `/* block */`, `/* inline EOL`      | Inline form ends at newline.           |
| Operators       | `+ - \* / // % ^ == != < <= > >= && |                                        | !`  | `//` is integer division. |
| Slices          | `array[1..4]`, `list[..-1]`         | Half-open intervals allowed.           |
| Template Holes  | `{{ expr }}`                        | Evaluated and embedded as literal.     |

---

## 3. Expression Grammar (Abridged)

```ebnf
expr        → logic_or
logic_or    → logic_and ( "||" logic_and )*
logic_and   → equality ( "&&" equality )*
equality    → comparison ( ("==" | "!=") comparison )*
comparison  → term ( ("<"|">"|"<="|">=") term )*
term        → factor ( ("+"|"-") factor )*
factor      → unary ( ("*"|"/"|"//"|"%") unary )*
unary       → ("+"|"-"|"!")? primary
primary     → literal | path | "(" expr ")" | slice
path        → IDENT ( "." IDENT | "[" slice_body "]" )*
slice_body  → expr? ".." expr?
```

---

## 4. Function Files

A function file has two top-level blocks:

### 4.1 Signature Block

```kir
FUNCTION  Fibonacci
NAMESPACE Demo
PARAMETERS
    n            { "type":"integer", "minimum":0 }
EVENTS
    output       { "type":"object", "properties":{ "result": { "type":"array","items":{"type":"integer"} } } }
```

### 4.2 Logic Block (Step Graph)

```kir
LOGIC
    init: System.Array.Create(value = 0)          // result = [0]
    second: System.Array.InsertLast(
        array = Steps.init.output.result,
        value = 1
    )
    loop: System.Loop.RangeLoop(
        start = 2,
        end   = Arguments.n
    )
        iteration
            compute: Math.Add(
                a = Steps.second.output.result[Steps.loop.iteration.index - 1],
                b = Steps.second.output.result[Steps.loop.iteration.index - 2]
            )
            push: System.Array.InsertLast(
                array = Steps.second.output.result,
                value = Steps.compute.output
            )
    finish: System.GenerateEvent(
        result = Steps.second.output.result
    )
```

---

## 5. Runtime Model

| Concept               | Description                                              |
| --------------------- | -------------------------------------------------------- |
| **Store**             | Mutable JSON data: `Context`, `Arguments`, etc.          |
| **Step**              | Invokes a namespaced function.                           |
| **Event**             | Control-flow unit; emitted by steps and triggers others. |
| **Execution Context** | Contains current store values and event payload.         |
| **System Namespace**  | Built-in standard library functions.                     |

---

## 6. Type System

- Uses **JSON Schema v2020-12** for parameters and event outputs.
- **No implicit coercion** (e.g., `int + string` = error).
- **Compile-time validation** when operand types are known.
- **Run-time validation** when writing or outputting store values.

---

## 7. Standard Library (v0.9 Snapshot)

| Namespace              | Functions                                 | Notes                                 |
| ---------------------- | ----------------------------------------- | ------------------------------------- |
| `System.Context`       | `Create`, `Put`, `Get`, `Delete`          | CRUD on local context store.          |
| `System.Array`         | `Create`, `InsertLast`, `Slice`, `Length` | Pure functions; result in new arrays. |
| `System.String`        | `Concat`, `Split`, `Replace`, `Match`     | String manipulation.                  |
| `System.Loop`          | `RangeLoop`, `ForEach`                    | Emit `iteration`, `complete`.         |
| `System.If`            | Emits `true`, `false`, `output`           | Used for branching.                   |
| `System.Math`          | `Add`, `Sub`, `Mul`, `Div`, `Mod`, `Pow`  | Arithmetic operations.                |
| `System.GenerateEvent` | Emits custom outputs                      | Used to finalize return payload.      |

---

## 8. Tooling & Ecosystem

| Tool               | Purpose                                         |
| ------------------ | ----------------------------------------------- |
| **ANTLR Grammar**  | `KirExpression.g4`, `KirFunction.g4`            |
| **TS AST Library** | Type-safe manipulation and visitors             |
| **Interpreter**    | Executes function steps based on emitted events |
| **CLI**            | Parse, validate, run tests, pretty-print        |
| **VS Code Ext**    | Syntax highlighting, inline run/debug tools     |
| **Web Studio**     | Drag & drop builder, exports JSON IR            |

---

## 9. Error Handling & Debugging

- **Compile-time diagnostics**: file, line, column, symbolic error codes.
- **Runtime tracing**: logs event transitions and state changes.
- **Breakpoints**: pause at steps for inspection.
- **Replay**: deterministic re-execution from traces.

---

## 10. Idiomatic Patterns

| Task                       | Recommended Pattern                                   |
| -------------------------- | ----------------------------------------------------- |
| **Conditional branch**     | `System.If` wired to `true` / `false` handlers.       |
| **Accumulative loop**      | `System.Loop.RangeLoop` + update array per iteration. |
| **Centralized error trap** | Route `error` events to a `handler:` step.            |

---

## 11. Hello, World Examples

### Expression Mode

```kir
"Hello, " + Page.user.firstName + " 👋"
```

### Function Mode

```kir
FUNCTION  Hello
EVENTS
    output   { "type":"string" }

LOGIC
    greet: System.GenerateEvent(
        output = "Hello, world!"
    )
```

Returns:

```json
{ "output": "Hello, world!" }
```

---

## 12. Road-map Highlights

- ✅ User-defined events
- ✅ Package/import system
- ⏳ First-class functions (lambda/map/filter)
- ⏳ Static code generation (e.g. JS backend)
- ⏳ Built-in test harness and coverage

---

## Final Thoughts

KIRun's event-based, schema-validated, indentation-aware model is purpose-built for embedding logic into low-code environments, while still offering enough formal rigor and extensibility for backend and CLI workflows. Its combination of drag-and-drop friendliness and clean textual syntax makes it suitable for both developers and power users.
