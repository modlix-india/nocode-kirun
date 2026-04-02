# Expression Engine

KIRun includes a full expression engine that evaluates expressions within parameter bindings. Expressions allow dynamic value resolution, arithmetic, comparisons, and data access across the execution context.

## Expression Syntax

Expressions are strings that reference values and apply operators. They can appear in:

- `ParameterReference` with `type: "EXPRESSION"` and `expression: "..."`
- Nested within `{{ }}` delimiters for dynamic interpolation

### Value Access

Access values from the execution context using dot notation:

```
Arguments.paramName                    → Function input parameter
Steps.stepName.output.propertyName     → Step output value
Steps.stepName.iteration.index         → Loop iteration index
Context.variableName                   → Context variable
```

### Property Access

```
Arguments.user.name                    → Nested object property
Arguments.items[0]                     → Array index access
Arguments.items[0].name                → Combined access
Steps.fetch.output.data[2].id          → Deep path
Arguments.config["mail.props.port"]    → Bracket notation for dotted keys
```

### Bracket Notation

Use brackets for:
- **Numeric indices**: `Arguments.items[0]`
- **Quoted keys with dots**: `Arguments.config["mail.host"]`
- **Dynamic indices**: `Arguments.items[Arguments.index]`

## Operators

### Arithmetic

| Operator | Description | Example |
|----------|-------------|---------|
| `+` | Addition (or string concatenation) | `Arguments.a + Arguments.b` |
| `-` | Subtraction | `Arguments.total - Arguments.discount` |
| `*` | Multiplication | `Arguments.price * Arguments.qty` |
| `/` | Division | `Arguments.total / Arguments.count` |
| `//` | Integer division | `Arguments.a // Arguments.b` |
| `%` | Modulus | `Arguments.value % 2` |

### Comparison

| Operator | Description | Example |
|----------|-------------|---------|
| `=` | Equal | `Arguments.status = 'active'` |
| `!=` | Not equal | `Arguments.role != 'admin'` |
| `<` | Less than | `Arguments.age < 18` |
| `>` | Greater than | `Arguments.count > 0` |
| `<=` | Less than or equal | `Arguments.score <= 100` |
| `>=` | Greater than or equal | `Arguments.min >= 0` |

> **Note:** KIRun uses single `=` for equality (not `==`).

### Logical

| Operator | Description | Example |
|----------|-------------|---------|
| `and` | Logical AND | `Arguments.active and Arguments.verified` |
| `or` | Logical OR | `Arguments.admin or Arguments.manager` |
| `not` | Logical NOT (unary) | `not Arguments.disabled` |

> Logical operators use keywords (`and`, `or`, `not`), not symbols.

### Bitwise

| Operator | Description |
|----------|-------------|
| `&` | Bitwise AND |
| `\|` | Bitwise OR |
| `^` | Bitwise XOR |
| `~` | Bitwise complement (unary) |
| `<<` | Left shift |
| `>>` | Right shift |
| `>>>` | Unsigned right shift |

### Ternary

```
Arguments.age >= 18 ? 'adult' : 'minor'
```

Syntax: `condition ? trueValue : falseValue`

### Nullish Coalescing

```
Arguments.nickname ?? Arguments.name ?? 'Anonymous'
```

Returns the left operand if it is not `null`/`undefined`, otherwise the right operand.

### Array Range

```
Arguments.items[2..5]     → Elements at indices 2, 3, 4
Arguments.items[..3]      → Elements at indices 0, 1, 2
Arguments.items[3..]      → Elements from index 3 to end
```

### Unary

| Operator | Description | Example |
|----------|-------------|---------|
| `+` | Unary plus | `+Arguments.value` |
| `-` | Unary minus (negation) | `-Arguments.offset` |
| `not` | Logical negation | `not Arguments.flag` |
| `~` | Bitwise complement | `~Arguments.mask` |

## Operator Precedence

From highest to lowest:

| Priority | Operators |
|----------|-----------|
| 1 | Unary (`+`, `-`, `not`, `~`), `.` (object), `[` (array) |
| 2 | `..` (range), `*`, `/`, `//`, `%` |
| 3 | `+`, `-` |
| 4 | `<<`, `>>`, `>>>` |
| 5 | `<`, `<=`, `>`, `>=` |
| 6 | `=`, `!=` |
| 7 | `&` (bitwise AND) |
| 8 | `^` (bitwise XOR) |
| 9 | `\|` (bitwise OR) |
| 10 | `not`, `and` |
| 11 | `or`, `??` |
| 12 | `?` `:` (ternary) |

Use parentheses to override precedence: `(Arguments.a + Arguments.b) * Arguments.c`

## Nested Expressions

Use `{{ }}` to embed expressions within strings or other expressions:

```
Steps.items[{{Arguments.index}}].name
```

This first evaluates `Arguments.index`, then uses the result as the array index.

Nested expressions can be deeply nested:

```
Steps.data[{{Steps.lookup[{{Arguments.key}}].output.index}}].value
```

Evaluation proceeds from the innermost `{{ }}` outward.

## Literals

Expressions support these literal types:

| Type | Examples |
|------|----------|
| Numbers | `42`, `3.14`, `-7` |
| Strings | `'hello'`, `"world"` |
| Booleans | `true`, `false` |
| Null | `null` |
| Undefined | `undefined` |

## Token Value Extractors

The expression engine resolves values through extractors registered by prefix:

| Prefix | Extractor | Source |
|--------|-----------|--------|
| `Arguments.` | `ArgumentsTokenValueExtractor` | Function input arguments |
| `Steps.` | `OutputMapTokenValueExtractor` | Step output results |
| `Context.` | `ContextTokenValueExtractor` | Runtime context variables |

You can register custom extractors for additional prefixes (e.g., `Store.`, `Page.`, `Parent.`).

### Custom Token Value Extractors

```typescript
class StoreTokenValueExtractor extends TokenValueExtractor {
    private store: Map<string, any>;

    constructor(store: Map<string, any>) {
        super();
        this.store = store;
    }

    getPrefix(): string {
        return 'Store.';
    }

    getValueInternal(token: string): any {
        // token is the full path: "Store.user.name"
        // Strip prefix and navigate the store
        const path = token.substring(this.getPrefix().length);
        return this.navigatePath(this.store, path);
    }

    protected getStore(): any {
        return this.store;
    }
}
```

## Expression Evaluation

### Programmatic Usage

```typescript
import { ExpressionEvaluator, TokenValueExtractor } from '@fincity/kirun-js';

// Build values map with extractors
const valuesMap = new Map<string, TokenValueExtractor>();
valuesMap.set('Arguments.', new ArgumentsTokenValueExtractor(argsMap));
valuesMap.set('Steps.', new OutputMapTokenValueExtractor(stepsMap));

// Evaluate
const evaluator = new ExpressionEvaluator('Arguments.x + Arguments.y * 2');
const result = evaluator.evaluate(valuesMap);
```

### Performance

The expression engine includes several optimizations:

- **Expression caching** - Parsed expressions are cached and reused
- **Pattern detection** - Fast paths for common patterns (literals, simple paths, comparisons, ternaries)
- **Cached arrays** - Internal linked lists are converted to arrays for fast evaluation

## Examples

### Simple Arithmetic

```
Arguments.price * Arguments.quantity
```

### Conditional Value

```
Arguments.role = 'admin' ? 'Full Access' : 'Limited Access'
```

### Null-safe Access

```
Arguments.user.nickname ?? Arguments.user.name ?? 'Unknown'
```

### Step Output Reference

```
Steps.fetchUser.output.data.email
```

### Dynamic Array Index

```
Steps.items[{{Steps.counter.iteration.index}}].name
```

### String Concatenation

```
Arguments.firstName + ' ' + Arguments.lastName
```

### Complex Expression

```
(Steps.total.output.value - Steps.discount.output.value) * (1 + Arguments.taxRate / 100)
```
