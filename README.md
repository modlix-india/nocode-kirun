# KIRun - Kinetic Instruction Runtime

<p align="center">
  <strong>A polyglot visual code execution engine for no-code/low-code platforms</strong>
</p>

<p align="center">
  <a href="#features">Features</a> •
  <a href="#installation">Installation</a> •
  <a href="#quick-start">Quick Start</a> •
  <a href="#architecture">Architecture</a> •
  <a href="#kirun-java">Java</a> •
  <a href="#kirun-js">JavaScript</a> •
  <a href="#api-reference">API Reference</a> •
  <a href="#license">License</a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21+-orange?style=flat-square&logo=openjdk" alt="Java 21+">
  <img src="https://img.shields.io/badge/TypeScript-5.x-blue?style=flat-square&logo=typescript" alt="TypeScript 5.x">
  <img src="https://img.shields.io/badge/License-MIT-green?style=flat-square" alt="MIT License">
  <img src="https://img.shields.io/badge/Maven_Central-3.14.0-purple?style=flat-square" alt="Maven Central">
  <img src="https://img.shields.io/badge/npm-2.16.0-red?style=flat-square&logo=npm" alt="npm">
</p>

---

## Overview

**KIRun** (Kinetic Instruction Runtime) is a cross-platform interpreter that executes visual/JSON-based function definitions. It enables the same business logic to run seamlessly on both server (Java) and client (JavaScript/TypeScript) environments, making it the execution backbone of no-code/low-code platforms.

KIRun transforms JSON-defined workflows into executable code through a graph-based execution engine, supporting complex control flows, expressions, loops, and event-driven programming.

---

## Features

- **Polyglot Execution** - Write once, run on Java and JavaScript runtimes
- **Graph-Based Execution** - Automatic dependency resolution and parallel execution
- **Rich Expression Engine** - Full arithmetic, logical, bitwise, and ternary operators
- **Comprehensive Function Library** - 100+ built-in functions for arrays, strings, math, dates, and objects
- **JSON Schema Validation** - Type-safe data validation with JSON Schema support
- **Event-Driven Architecture** - Support for branching workflows with custom events
- **Reactive Support** - Full Project Reactor integration (Java) and async/await (JavaScript)
- **Extensible** - Custom function and schema repositories
- **Debug Mode** - Built-in execution tracing and debugging

---

## Installation

### Java (Maven)

```xml
<dependency>
    <groupId>com.fincity.nocode</groupId>
    <artifactId>kirun-java</artifactId>
    <version>3.14.0</version>
</dependency>
```

### Java (Gradle)

```groovy
implementation 'com.fincity.nocode:kirun-java:3.14.0'
```

### JavaScript/TypeScript (npm)

```bash
npm install @fincity/kirun-js
```

### JavaScript/TypeScript (yarn)

```bash
yarn add @fincity/kirun-js
```

---

## Quick Start

### JavaScript Example

```typescript
import {
    FunctionDefinition,
    KIRuntime,
    FunctionExecutionParameters,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
} from '@fincity/kirun-js';

// Define a simple function that adds two numbers
const functionDef = new FunctionDefinition()
    .setName('addNumbers')
    .setNamespace('MyApp')
    .setParameters(
        new Map([
            ['a', Parameter.of('a', Schema.ofNumber('a'))],
            ['b', Parameter.of('b', Schema.ofNumber('b'))],
        ])
    )
    .setSteps(
        new Map([
            [
                'add',
                new Statement()
                    .setStatementName('add')
                    .setNamespace('System.Math')
                    .setName('Add')
                    .setParameterMap(
                        new Map([
                            ['value', new Map([['one', ParameterReference.of('Arguments.a')]])],
                        ])
                    ),
            ],
        ])
    );

// Execute the function
const runtime = new KIRuntime(functionDef);
const fep = new FunctionExecutionParameters(
    new KIRunFunctionRepository(),
    new KIRunSchemaRepository(),
    'execution-1'
).setArguments(new Map([['a', 5], ['b', 3]]));

const result = await runtime.execute(fep);
console.log(result); // Output: 8
```

### Java Example

```java
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveKIRuntime;
import com.fincity.nocode.kirun.engine.model.FunctionDefinition;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveFunctionRepository;
import com.fincity.nocode.kirun.engine.repository.reactive.KIRunReactiveSchemaRepository;

// Create and execute a function definition
FunctionDefinition fd = new FunctionDefinition()
    .setName("calculateTotal")
    .setNamespace("MyApp");
    // ... configure steps and parameters

ReactiveKIRuntime runtime = new ReactiveKIRuntime(fd);
FunctionExecutionParameters fep = new FunctionExecutionParameters(
    new KIRunReactiveFunctionRepository(),
    new KIRunReactiveSchemaRepository()
);

runtime.execute(fep)
    .subscribe(result -> System.out.println(result));
```

---

## Architecture

### Core Components

```
┌─────────────────────────────────────────────────────────────────┐
│                        KIRuntime                                 │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
│  │  Function   │  │   Schema    │  │    Expression           │  │
│  │  Repository │  │  Repository │  │    Evaluator            │  │
│  └─────────────┘  └─────────────┘  └─────────────────────────┘  │
├─────────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────┐    │
│  │              Execution Graph Engine                      │    │
│  │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐    │    │
│  │  │ Vertex  │──│ Vertex  │──│ Vertex  │──│ Vertex  │    │    │
│  │  └─────────┘  └─────────┘  └─────────┘  └─────────┘    │    │
│  └─────────────────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────────────────┤
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                  Built-in Functions                        │  │
│  │  Math • String • Array • Object • Date • Loop • Control   │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### Execution Flow

1. **Parse** - FunctionDefinition is loaded from JSON
2. **Plan** - ExecutionGraph is built with dependency analysis
3. **Validate** - Parameters and schemas are validated
4. **Execute** - Statements run in dependency order with parallel execution where possible
5. **Output** - Events are raised and results returned

---

# KIRun Java

## Overview

`kirun-java` is the Java implementation of the KIRun interpreter, built with Java 21 and Project Reactor for fully reactive, non-blocking execution.

## Requirements

- Java 21 or higher
- Maven 3.8+

## Build

```bash
cd kirun-java
./mvnw clean install
```

## Run Tests

```bash
./mvnw test
```

## Dependencies

| Dependency | Purpose |
|------------|---------|
| Project Reactor | Reactive streams support |
| Gson | JSON parsing and serialization |
| Lombok | Boilerplate reduction |
| Reflections | Runtime class scanning |

## Package Structure

```
com.fincity.nocode.kirun.engine
├── constant/          # Constants and configuration
├── exception/         # Custom exceptions
├── function/          # Function interfaces and implementations
│   ├── reactive/      # Reactive function wrappers
│   └── system/        # Built-in system functions
│       ├── array/     # Array operations (32 functions)
│       ├── context/   # Context management
│       ├── date/      # Date/time operations (18 functions)
│       ├── json/      # JSON operations
│       ├── loop/      # Loop constructs
│       ├── math/      # Mathematical operations
│       ├── object/    # Object manipulation (7 functions)
│       └── string/    # String operations (17 functions)
├── json/
│   └── schema/        # JSON Schema implementation
├── model/             # Data models (FunctionDefinition, Statement, etc.)
├── namespaces/        # Namespace management
├── reactive/          # Reactive utilities
├── repository/        # Function and Schema repositories
│   └── reactive/      # Reactive repository implementations
├── runtime/           # Execution engine
│   ├── expression/    # Expression parser and evaluator
│   ├── graph/         # Execution graph implementation
│   ├── reactive/      # Reactive runtime
│   └── tokenextractors/ # Token value extraction
└── util/              # Utility classes
```

## Reactive Runtime

The Java implementation uses Project Reactor for fully non-blocking execution:

```java
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveKIRuntime;
import reactor.core.publisher.Mono;

ReactiveKIRuntime runtime = new ReactiveKIRuntime(functionDefinition, debugMode);

Mono<FunctionOutput> result = runtime.execute(parameters);

// Subscribe to execute
result.subscribe(
    output -> processOutput(output),
    error -> handleError(error),
    () -> onComplete()
);
```

## Built-in Functions (Java)

### System Functions

| Function | Namespace | Description |
|----------|-----------|-------------|
| `If` | System | Conditional branching |
| `GenerateEvent` | System | Raise custom events |
| `Print` | System | Debug output |
| `Wait` | System | Async delay |
| `ValidateSchema` | System | JSON Schema validation |

### Math Functions (`System.Math`)

| Function | Description |
|----------|-------------|
| `Add` | Addition with variable arguments |
| `Minimum` | Find minimum value |
| `Maximum` | Find maximum value |
| `Random` | Generate random numbers |
| `Hypotenuse` | Calculate hypotenuse |

### Array Functions (`System.Array`)

| Function | Description |
|----------|-------------|
| `AddFirst` / `InsertLast` | Add elements |
| `Delete` / `DeleteFirst` / `DeleteLast` | Remove elements |
| `Sort` / `Reverse` / `Shuffle` | Reorder elements |
| `IndexOf` / `LastIndexOf` | Search elements |
| `SubArray` / `Copy` | Extract elements |
| `Concatenate` / `Join` | Combine arrays |
| `Min` / `Max` | Find extremes |
| `RemoveDuplicates` | Deduplicate |
| `BinarySearch` | Binary search |
| `Fill` / `Rotate` | Transform arrays |
| `Frequency` / `Disjoint` / `MisMatch` | Compare arrays |

### String Functions (`System.String`)

| Function | Description |
|----------|-------------|
| `Concatenate` | Join strings |
| `Split` | Split string |
| `Reverse` | Reverse string |
| `Matches` | Regex matching |
| `PrePad` / `PostPad` | Padding |
| `TrimTo` | Trim to length |
| `ToString` | Type conversion |
| `Frequency` | Character count |
| `InsertAtGivenPosition` | Insert substring |
| `DeleteForGivenLength` | Delete substring |
| `ReplaceAtGivenPosition` | Replace substring |
| `RegionMatches` | Region comparison |

### Object Functions (`System.Object`)

| Function | Description |
|----------|-------------|
| `ObjectKeys` | Get object keys |
| `ObjectValues` | Get object values |
| `ObjectEntries` | Get key-value pairs |
| `ObjectPutValue` | Set property |
| `ObjectDeleteKey` | Remove property |
| `ObjectConvert` | Type conversion |

### Date Functions (`System.Date`)

| Function | Description |
|----------|-------------|
| `GetCurrentTimestamp` | Current time |
| `FromNow` | Relative time |
| `AddSubtractTime` | Time arithmetic |
| `Difference` | Time difference |
| `EpochToTimestamp` | Epoch conversion |
| `TimestampToEpoch` | Timestamp to epoch |
| `FromDateString` | Parse date string |
| `ToDateString` | Format date |
| `IsBetween` | Range check |
| `IsValidISODate` | Validation |
| `StartEndOf` | Period boundaries |
| `LastFirstOf` | Period navigation |
| `TimeAs` | Unit extraction |
| `GetNames` | Name retrieval |
| `SetTimeZone` | Timezone handling |

### Loop Constructs (`System.Loop`)

| Function | Description |
|----------|-------------|
| `CountLoop` | Count-based iteration |
| `ForEachLoop` | Collection iteration |
| `RangeLoop` | Range-based iteration |
| `Break` | Loop termination |

## Expression Operators (Java)

### Arithmetic
`+` `-` `*` `/` `%` (modulo)

### Comparison
`==` `!=` `<` `>` `<=` `>=`

### Logical
`&&` (AND) `||` (OR) `!` (NOT)

### Bitwise
`&` `|` `^` `~` `<<` `>>` `>>>`

### Ternary
`condition ? trueValue : falseValue`

### Null Coalescing
`??` (null coalescing)

---

# KIRun JavaScript/TypeScript

## Overview

`@fincity/kirun-js` is the JavaScript/TypeScript implementation of KIRun, designed for browser and Node.js environments with full async/await support.

## Requirements

- Node.js 16+ or modern browser
- TypeScript 5.x (for TypeScript projects)

## Build

```bash
cd kirun-js
npm install
npm run build
```

## Run Tests

```bash
npm test           # Watch mode
npm run coverage   # Coverage report
```

## Format Code

```bash
npm run pretty
```

## Dependencies

| Dependency | Purpose |
|------------|---------|
| Luxon | Date/time handling |
| Parcel | Build bundling |
| TypeScript | Type definitions |
| Jest | Testing |

## Module Exports

The package exports both CommonJS and ES modules:

```typescript
// ES Modules
import { KIRuntime, FunctionDefinition } from '@fincity/kirun-js';

// CommonJS
const { KIRuntime, FunctionDefinition } = require('@fincity/kirun-js');
```

## Package Structure

```
@fincity/kirun-js
├── engine/
│   ├── constant/           # Constants
│   ├── exception/          # Custom exceptions
│   ├── function/           # Function interfaces
│   │   └── system/         # Built-in functions
│   │       ├── array/      # Array operations
│   │       ├── context/    # Context management
│   │       ├── date/       # Date operations
│   │       ├── json/       # JSON operations
│   │       ├── loop/       # Loop constructs
│   │       ├── math/       # Math operations
│   │       ├── object/     # Object operations
│   │       └── string/     # String operations
│   ├── json/
│   │   └── schema/         # JSON Schema
│   │       ├── array/      # Array schema types
│   │       ├── string/     # String formats
│   │       ├── type/       # Schema types
│   │       └── validator/  # Schema validators
│   ├── model/              # Data models
│   ├── namespaces/         # Namespace constants
│   ├── repository/         # Repositories
│   ├── runtime/            # Execution engine
│   │   ├── expression/     # Expression evaluator
│   │   │   └── operators/  # Operator implementations
│   │   ├── graph/          # Execution graph
│   │   └── tokenextractor/ # Token extraction
│   └── util/               # Utilities
└── index.ts                # Public API exports
```

## TypeScript Types

Full TypeScript definitions are included:

```typescript
import {
    // Core Runtime
    KIRuntime,
    FunctionExecutionParameters,
    FunctionOutput,
    EventResult,

    // Models
    FunctionDefinition,
    FunctionSignature,
    Statement,
    Parameter,
    ParameterReference,
    Event,

    // Schema
    Schema,
    SchemaType,
    SchemaValidator,

    // Repositories
    Repository,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
    HybridRepository,

    // Expression
    ExpressionEvaluator,
    Expression,

    // Utilities
    LinkedList,
    Tuple2,
    Tuple4,
    StringFormatter,
    StringUtil,
} from '@fincity/kirun-js';
```

## Async Execution

KIRun-JS uses native async/await for all operations:

```typescript
const runtime = new KIRuntime(functionDef, debugMode);

// Async execution
const output = await runtime.execute(parameters);

// Process results
for (const eventResult of output.allResults()) {
    console.log(eventResult.getName(), eventResult.getResult());
}
```

## Custom Function Repository

```typescript
import { Repository, Function, HybridRepository, KIRunFunctionRepository } from '@fincity/kirun-js';

class CustomFunctionRepository implements Repository<Function> {
    async find(namespace: string, name: string): Promise<Function | undefined> {
        // Custom function lookup logic
        if (namespace === 'MyApp' && name === 'CustomFunction') {
            return new MyCustomFunction();
        }
        return undefined;
    }

    async filter(name: string): Promise<string[]> {
        return ['MyApp.CustomFunction'];
    }
}

// Combine with built-in functions
const repo = new HybridRepository<Function>(
    new KIRunFunctionRepository(),
    new CustomFunctionRepository()
);
```

## Expression Evaluation

```typescript
import { ExpressionEvaluator, TokenValueExtractor } from '@fincity/kirun-js';

// Create value extractors
const valuesMap = new Map<string, TokenValueExtractor>();
valuesMap.set('Arguments', new ArgumentsTokenValueExtractor(args));
valuesMap.set('Steps', new OutputMapTokenValueExtractor(steps));

// Evaluate expression
const evaluator = new ExpressionEvaluator('Arguments.x + Arguments.y * 2');
const result = evaluator.evaluate(valuesMap);
```

## JSON Schema Validation

```typescript
import { Schema, SchemaValidator, SchemaType } from '@fincity/kirun-js';

// Define schema
const userSchema = Schema.ofObject('User')
    .setProperties(new Map([
        ['name', Schema.ofString('name')],
        ['age', Schema.ofInteger('age').setMinimum(0)],
        ['email', Schema.ofString('email').setFormat(StringFormat.EMAIL)],
    ]))
    .setRequired(['name', 'email']);

// Validate data
const validator = new SchemaValidator();
const result = await validator.validate(null, userSchema, schemaRepo, userData);
```

## Browser Usage

```html
<script type="module">
import { KIRuntime, FunctionDefinition } from 'https://unpkg.com/@fincity/kirun-js/dist/module.js';

// Your code here
</script>
```

---

## API Reference

### FunctionDefinition

The core model representing an executable function:

```typescript
interface FunctionDefinition {
    namespace: string;           // Function namespace
    name: string;               // Function name
    parameters: Map<string, Parameter>;  // Input parameters
    events: Map<string, Event>; // Output events
    steps: Map<string, Statement>;       // Execution steps
    version: number;            // Definition version
}
```

### Statement

A single executable step:

```typescript
interface Statement {
    statementName: string;      // Unique step identifier
    namespace: string;          // Function namespace to call
    name: string;              // Function name to call
    parameterMap: Map<string, Map<string, ParameterReference>>;
    dependentStatements: Map<string, boolean>;
    executeIftrue: Map<string, boolean>;  // Conditional execution
}
```

### ParameterReference

Reference to parameter values:

```typescript
interface ParameterReference {
    type: ParameterReferenceType;  // VALUE or EXPRESSION
    value?: any;                   // Static value
    expression?: string;           // Dynamic expression
    order?: number;                // For variable arguments
}
```

### Expression Syntax

```
// Variable access
Arguments.paramName
Steps.stepName.output.property
Context.contextKey

// Array indexing
Arguments.items[0]
Steps.result.output.data[index]

// Object property access
Arguments.user.name
Steps.fetch.output.response.body

// Operators
Arguments.a + Arguments.b
Steps.check.output.valid ? 'yes' : 'no'
Arguments.value ?? 'default'
```

---

## Contributing

Contributions are welcome! Please read our contributing guidelines and submit pull requests to the `master` branch.

### Development Setup

```bash
# Clone repository
git clone https://github.com/fincity-india/nocode-kirun.git
cd nocode-kirun

# Java development
cd kirun-java
./mvnw clean install

# JavaScript development
cd ../kirun-js
npm install
npm run build
npm test
```

### Running All Tests

```bash
# Java tests
cd kirun-java && ./mvnw test

# JavaScript tests
cd kirun-js && npm run coverage
```

---

## License

MIT License - see [LICENSE](LICENSE) for details.

---

## Links

- **Repository**: [github.com/fincity-india/nocode-kirun](https://github.com/fincity-india/nocode-kirun)
- **Java Package**: [Maven Central](https://central.sonatype.com/artifact/com.fincity.nocode/kirun-java)
- **npm Package**: [@fincity/kirun-js](https://www.npmjs.com/package/@fincity/kirun-js)
- **Issues**: [GitHub Issues](https://github.com/fincity-india/nocode-kirun/issues)

---

<p align="center">
  Built with care by <a href="https://modlix.com">Modlix</a>
</p>
