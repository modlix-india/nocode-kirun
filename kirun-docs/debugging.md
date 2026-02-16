# Debug Mode

KIRun includes a built-in debugging system that traces execution at the step level. It captures arguments, results, timing, and errors for each step, making it easy to inspect what happened during a function execution.

## Enabling Debug Mode

### JavaScript/TypeScript

```typescript
import { KIRuntime, DebugCollector } from '@fincity/kirun-js';

// Enable via constructor
const runtime = new KIRuntime(functionDefinition, true); // debugMode = true

// Or enable the global collector directly
DebugCollector.getInstance().enable();
```

### Java

```java
ReactiveKIRuntime runtime = new ReactiveKIRuntime(functionDefinition, true);
```

## DebugCollector

The `DebugCollector` is a singleton that collects execution data globally. It stores data for up to 10 executions (configurable).

### Core API

```typescript
const collector = DebugCollector.getInstance();

// Enable/disable
collector.enable();
collector.disable();
collector.isEnabled();

// Query executions
collector.getExecution(executionId);    // Get specific execution
collector.getLastExecution();            // Get most recent
collector.getAllExecutionIds();           // List all tracked IDs
collector.getFlatLogs(executionId);      // Flat list of all steps
collector.getDefinition(executionId, functionName); // Get function def

// Cleanup
collector.clear();
```

### Execution Log Structure

```typescript
interface ExecutionLog {
    executionId: string;
    startTime: number;
    endTime?: number;
    errored: boolean;
    logs: LogEntry[];
    definitions: Map<string, FunctionDefinition>;
}
```

### Log Entry Structure

Each step execution produces a `LogEntry`:

```typescript
interface LogEntry {
    stepId: string;
    timestamp: number;
    duration?: number;
    functionName: string;
    statementName: string;
    kirunFunctionName?: string;  // For nested KIRuntime calls
    arguments?: any;
    result?: any;
    eventName?: string;
    error?: string;
    children: LogEntry[];       // Nested function calls
}
```

### Nested Execution

When a step calls another `KIRuntime` function, the child steps appear as `children` of the parent's `LogEntry`. This creates a tree structure that mirrors the call hierarchy.

## Event Listeners

Subscribe to real-time debug events:

```typescript
const collector = DebugCollector.getInstance();

const removeListener = collector.addEventListener((event) => {
    switch (event.type) {
        case 'executionStart':
            console.log('Started:', event.executionId, event.data.functionName);
            break;
        case 'executionEnd':
            console.log('Ended:', event.executionId,
                `${event.data.duration}ms`,
                event.data.errored ? 'ERRORED' : 'OK');
            break;
        case 'stepStart':
            console.log('Step started:', event.data.statementName);
            break;
        case 'stepEnd':
            console.log('Step ended:', event.data.log.statementName,
                event.data.log.duration + 'ms');
            break;
        case 'executionErrored':
            console.log('Error in:', event.executionId);
            break;
    }
});

// Later: remove listener
removeListener();
```

### Event Types

| Type | Data | Trigger |
|------|------|---------|
| `executionStart` | `{ functionName }` | When a function begins execution |
| `executionEnd` | `{ duration, errored }` | When a function completes |
| `stepStart` | `{ stepId, statementName, functionName }` | When a step begins |
| `stepEnd` | `{ log: LogEntry }` | When a step completes |
| `executionErrored` | - | When an error occurs |

## Inspecting Results

### Get All Steps Flat

```typescript
const logs = collector.getFlatLogs(executionId);
for (const log of logs) {
    console.log(
        `[${log.statementName}] ${log.functionName}`,
        `args=${JSON.stringify(log.arguments)}`,
        `result=${JSON.stringify(log.result)}`,
        `event=${log.eventName}`,
        log.error ? `ERROR: ${log.error}` : '',
    );
}
```

### Get Execution Timing

```typescript
const exec = collector.getExecution(executionId);
if (exec) {
    console.log(`Total time: ${(exec.endTime ?? 0) - exec.startTime}ms`);
    console.log(`Steps: ${collector.getFlatLogs(executionId).length}`);
    console.log(`Errored: ${exec.errored}`);
}
```

### Access Function Definitions

When debug mode is on, the collector also stores `FunctionDefinition` objects for nested calls. This is useful for visualizing the execution graph.

```typescript
const def = collector.getDefinition(executionId, 'MyApp.processOrder');
if (def) {
    const steps = def.getSteps();
    // Inspect the definition's steps
}
```

## Example: Debugging a Fibonacci Function

```typescript
import {
    FunctionDefinition,
    KIRuntime,
    FunctionExecutionParameters,
    KIRunFunctionRepository,
    KIRunSchemaRepository,
    DebugCollector,
} from '@fincity/kirun-js';

// Enable debug
const collector = DebugCollector.getInstance();
collector.enable();

// Listen to events
collector.addEventListener((event) => {
    if (event.type === 'stepEnd') {
        const log = event.data.log;
        console.log(
            `  ${log.statementName}: ${log.functionName}` +
            ` → ${log.eventName} (${log.duration}ms)`
        );
    }
});

// Execute
const fd = FunctionDefinition.from(fibonacciJson);
const runtime = new KIRuntime(fd, true);
const fep = new FunctionExecutionParameters(
    new KIRunFunctionRepository(),
    new KIRunSchemaRepository(),
).setArguments(new Map([['n', 10]]));

const result = await runtime.execute(fep);

// Inspect after execution
const exec = collector.getLastExecution()!;
console.log(`Execution ${exec.executionId}:`);
console.log(`  Duration: ${(exec.endTime ?? 0) - exec.startTime}ms`);
console.log(`  Total steps: ${collector.getFlatLogs(exec.executionId).length}`);
console.log(`  Errored: ${exec.errored}`);

// Cleanup
collector.clear();
collector.disable();
```

## Error Tracking

When a step fails, the error is captured in the `LogEntry`:

```typescript
const logs = collector.getFlatLogs(executionId);
const errors = logs.filter(log => log.error);

for (const errorLog of errors) {
    console.error(
        `Step "${errorLog.statementName}" (${errorLog.functionName}) failed:`,
        errorLog.error,
    );
}
```

The execution is also marked as `errored`:

```typescript
const exec = collector.getExecution(executionId);
if (exec?.errored) {
    console.error('Execution had errors');
}
```

## Error Message Formatting

KIRun provides structured error messages that include:
- The function name and namespace
- The statement name that failed
- The parameter that caused the issue (for validation errors)
- The expected schema
- The actual error message

This makes it straightforward to trace the source of errors in complex function graphs.
