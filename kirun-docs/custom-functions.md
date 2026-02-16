# Custom Functions

KIRun is extensible - you can define your own functions and register them via custom repositories. There are two approaches:

1. **Code-based** - Implement the `Function` interface in JavaScript/TypeScript or Java
2. **Definition-based** - Define functions as JSON (same format as built-in function definitions)

## Code-based Custom Functions (JavaScript/TypeScript)

### Step 1: Extend AbstractFunction

```typescript
import {
    AbstractFunction,
    FunctionSignature,
    FunctionExecutionParameters,
    FunctionOutput,
    EventResult,
    Parameter,
    Event,
    Schema,
    MapUtil,
} from '@fincity/kirun-js';

class GreetFunction extends AbstractFunction {
    private readonly signature: FunctionSignature;

    constructor() {
        super();
        this.signature = new FunctionSignature('Greet')
            .setNamespace('MyApp')
            .setParameters(
                new Map([
                    Parameter.ofEntry('name', Schema.ofString('name')),
                    Parameter.ofEntry('greeting', Schema.ofString('greeting')
                        .setDefaultValue('Hello')),
                ]),
            )
            .setEvents(
                new Map([
                    Event.outputEventMapEntry(
                        MapUtil.of('message', Schema.ofString('message')),
                    ),
                ]),
            );
    }

    getSignature(): FunctionSignature {
        return this.signature;
    }

    protected async internalExecute(
        context: FunctionExecutionParameters,
    ): Promise<FunctionOutput> {
        const args = context.getArguments()!;
        const name = args.get('name') as string;
        const greeting = args.get('greeting') as string;

        const message = `${greeting}, ${name}!`;

        return new FunctionOutput([
            EventResult.outputOf(MapUtil.of('message', message)),
        ]);
    }
}
```

### Step 2: Create a Custom Repository

```typescript
import { Repository, Function } from '@fincity/kirun-js';

class MyFunctionRepository implements Repository<Function> {
    private functions: Map<string, Function> = new Map();

    constructor() {
        const greet = new GreetFunction();
        const key = `${greet.getSignature().getNamespace()}.${greet.getSignature().getName()}`;
        this.functions.set(key, greet);
    }

    async find(namespace: string, name: string): Promise<Function | undefined> {
        return this.functions.get(`${namespace}.${name}`);
    }

    async filter(name: string): Promise<string[]> {
        return Array.from(this.functions.keys())
            .filter(k => k.includes(name));
    }
}
```

### Step 3: Use with HybridRepository

```typescript
import {
    HybridRepository,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
    KIRuntime,
    FunctionExecutionParameters,
} from '@fincity/kirun-js';

// Combine built-in + custom functions
const functionRepo = new HybridRepository<Function>(
    new KIRunFunctionRepository(),
    new MyFunctionRepository(),
);

const schemaRepo = new KIRunSchemaRepository();

// Now your function definitions can reference "MyApp.Greet"
const runtime = new KIRuntime(functionDefinition);
const fep = new FunctionExecutionParameters(functionRepo, schemaRepo)
    .setArguments(args);

const result = await runtime.execute(fep);
```

## Code-based Custom Functions (Java)

### Using AbstractReactiveFunction

```java
import com.fincity.nocode.kirun.engine.function.reactive.AbstractReactiveFunction;
import com.fincity.nocode.kirun.engine.model.*;
import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.runtime.reactive.ReactiveFunctionExecutionParameters;
import reactor.core.publisher.Mono;

public class GreetFunction extends AbstractReactiveFunction {

    private static final FunctionSignature SIGNATURE = new FunctionSignature("Greet")
        .setNamespace("MyApp")
        .setParameters(Map.of(
            "name", Parameter.of("name", Schema.ofString("name")),
            "greeting", Parameter.of("greeting", Schema.ofString("greeting")
                .setDefaultValue(new JsonPrimitive("Hello")))
        ))
        .setEvents(Map.of(
            Event.OUTPUT, new Event(Event.OUTPUT, Map.of(
                "message", Schema.ofString("message")
            ))
        ));

    @Override
    public FunctionSignature getSignature() {
        return SIGNATURE;
    }

    @Override
    protected Mono<FunctionOutput> internalExecute(
            ReactiveFunctionExecutionParameters context) {
        String name = context.getArguments().get("name").getAsString();
        String greeting = context.getArguments().get("greeting").getAsString();

        String message = greeting + ", " + name + "!";

        return Mono.just(new FunctionOutput(List.of(
            EventResult.outputOf(Map.of("message", new JsonPrimitive(message)))
        )));
    }
}
```

## Definition-based Custom Functions

You can also define reusable functions as JSON definitions and register them as `KIRuntime` instances in your repository.

### Step 1: Define the Function JSON

```json
{
    "name": "CalculateDiscount",
    "namespace": "MyApp",
    "parameters": {
        "price": {
            "parameterName": "price",
            "schema": { "type": ["DOUBLE"] }
        },
        "discountPercent": {
            "parameterName": "discountPercent",
            "schema": { "type": ["DOUBLE"], "defaultValue": 10 }
        }
    },
    "events": {
        "output": {
            "name": "output",
            "parameters": {
                "discountedPrice": { "type": ["DOUBLE"] }
            }
        }
    },
    "steps": {
        "calculate": {
            "statementName": "calculate",
            "namespace": "System",
            "name": "Make",
            "parameterMap": {
                "resultShape": {
                    "one": {
                        "key": "one",
                        "type": "VALUE",
                        "value": "{{Arguments.price * (1 - Arguments.discountPercent / 100)}}"
                    }
                }
            }
        },
        "output": {
            "statementName": "output",
            "namespace": "System",
            "name": "GenerateEvent",
            "parameterMap": {
                "eventName": {
                    "one": { "key": "one", "type": "VALUE", "value": "output" }
                },
                "results": {
                    "discountedPrice": {
                        "key": "discountedPrice",
                        "type": "EXPRESSION",
                        "expression": "Steps.calculate.output.value"
                    }
                }
            },
            "dependentStatements": { "Steps.calculate.output": true }
        }
    }
}
```

### Step 2: Register as KIRuntime in Repository

```typescript
import { Repository, Function, FunctionDefinition, KIRuntime } from '@fincity/kirun-js';

class DefinitionBasedRepository implements Repository<Function> {
    private definitions: Map<string, any> = new Map();

    registerDefinition(json: any): void {
        const key = `${json.namespace}.${json.name}`;
        this.definitions.set(key, json);
    }

    async find(namespace: string, name: string): Promise<Function | undefined> {
        const json = this.definitions.get(`${namespace}.${name}`);
        if (!json) return undefined;

        const fd = FunctionDefinition.from(json);
        return new KIRuntime(fd);
    }

    async filter(name: string): Promise<string[]> {
        return Array.from(this.definitions.keys())
            .filter(k => k.includes(name));
    }
}
```

## Function Signature Details

### Description and Documentation

Functions can include metadata for tooling and documentation:

```typescript
const signature = new FunctionSignature('MyFunction')
    .setNamespace('MyApp')
    .setDescription('Short one-line description')
    .setDocumentation('Detailed markdown documentation for the function')
    .setMetadata({ category: 'utility', version: '1.0' })
    .setParameters(...)
    .setEvents(...);
```

### Variable Arguments

Parameters with `variableArgument: true` accept multiple values:

```typescript
Parameter.of('values', Schema.ofNumber('values'), true)
```

When called, the values are collected into an array:

```json
{
    "parameterMap": {
        "values": {
            "one": { "key": "one", "type": "VALUE", "value": 10, "order": 1 },
            "two": { "key": "two", "type": "VALUE", "value": 20, "order": 2 },
            "three": { "key": "three", "type": "VALUE", "value": 30, "order": 3 }
        }
    }
}
```

The function receives `values` as `[10, 20, 30]`.

### Multiple Events

Functions can emit multiple event types for branching:

```typescript
const signature = new FunctionSignature('Validate')
    .setNamespace('MyApp')
    .setEvents(new Map([
        Event.eventMapEntry('valid', MapUtil.of('data', Schema.ofAny('data'))),
        Event.eventMapEntry('invalid', MapUtil.of('errors', Schema.ofArray('errors', Schema.ofString('error')))),
    ]));
```

Downstream steps can depend on specific events:

```json
{
    "dependentStatements": { "Steps.validate.valid": true },
    "executeIftrue": { "Steps.validate.valid": true }
}
```

## Accessing the Execution Context

Within `internalExecute`, you can access:

```typescript
protected async internalExecute(
    context: FunctionExecutionParameters,
): Promise<FunctionOutput> {
    // Function arguments (validated)
    const args = context.getArguments()!;

    // Expression evaluation
    const valuesMap = context.getValuesMap();
    const evaluator = new ExpressionEvaluator('Arguments.field');
    const value = evaluator.evaluate(valuesMap);

    // Function repository (for calling other functions)
    const funcRepo = context.getFunctionRepository();

    // Schema repository
    const schemaRepo = context.getSchemaRepository();

    // Execution context (shared state)
    const execContext = context.getExecutionContext();

    // ...
}
```

## Custom Schema Repository

Similarly, you can create custom schema repositories:

```typescript
class MySchemaRepository implements Repository<Schema> {
    async find(namespace: string, name: string): Promise<Schema | undefined> {
        if (namespace === 'MyApp' && name === 'UserSchema') {
            return Schema.ofObject('UserSchema')
                .setNamespace('MyApp')
                .setProperties(new Map([
                    ['id', Schema.ofInteger('id')],
                    ['name', Schema.ofString('name')],
                    ['email', Schema.ofString('email')],
                ]));
        }
        return undefined;
    }

    async filter(name: string): Promise<string[]> {
        return ['MyApp.UserSchema'];
    }
}
```

## Token Value Extractors

For functions that need to evaluate expressions against custom data sources:

```typescript
import { TokenValueExtractor } from '@fincity/kirun-js';

class CustomExtractor extends TokenValueExtractor {
    private data: any;

    constructor(data: any) {
        super();
        this.data = data;
    }

    getPrefix(): string {
        return 'Custom.';
    }

    protected getValueInternal(token: string): any {
        const path = token.substring(this.getPrefix().length);
        return this.navigateObject(this.data, path.split('.'));
    }

    protected getStore(): any {
        return this.data;
    }

    private navigateObject(obj: any, parts: string[]): any {
        let current = obj;
        for (const part of parts) {
            if (current == null) return undefined;
            current = current[part];
        }
        return current;
    }
}

// Register with execution parameters
fep.addTokenValueExtractor(new CustomExtractor(myData));
```
