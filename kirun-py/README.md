# kirun-py

Python runtime for **KIRun** — the Kinetic Instructions execution engine used by the [Modlix](https://modlix.com) no-code/low-code platform.

## Installation

```bash
pip install kirun-py
```

## Overview

KIRun executes `FunctionDefinition` graphs (DAGs) with:

- **Expression evaluation** — arithmetic, logical, string, ternary operators
- **Schema validation** — JSON Schema-based type checking
- **100+ built-in functions** — Math, String, Array, Object, Date, JSON, Loop, Context
- **Async execution** — full `async`/`await` support
- **Debug support** — execution tracing and performance profiling

## Quick Start

```python
import asyncio
import json
from kirun_py.model.function_definition import FunctionDefinition
from kirun_py.runtime.ki_runtime import KIRuntime
from kirun_py.runtime.function_execution_parameters import FunctionExecutionParameters
from kirun_py.repository.ki_run_function_repository import KIRunFunctionRepository
from kirun_py.repository.ki_run_schema_repository import KIRunSchemaRepository

fd = FunctionDefinition.from_value(json.loads("""
{
    "name": "AddTwo",
    "namespace": "MyApp",
    "parameters": {
        "a": { "parameterName": "a", "schema": { "name": "a", "type": "INTEGER" } },
        "b": { "parameterName": "b", "schema": { "name": "b", "type": "INTEGER" } }
    },
    "events": {
        "output": {
            "name": "output",
            "parameters": { "result": { "name": "result", "type": "INTEGER" } }
        }
    },
    "steps": {
        "gen": {
            "statementName": "gen",
            "namespace": "System",
            "name": "GenerateEvent",
            "parameterMap": {
                "eventName": { "one": { "key": "one", "type": "VALUE", "value": "output" } },
                "results": {
                    "one": {
                        "key": "one", "type": "VALUE",
                        "value": {
                            "name": "result",
                            "value": { "isExpression": true, "value": "Arguments.a + Arguments.b" }
                        }
                    }
                }
            }
        }
    }
}
"""))

async def main():
    runtime = KIRuntime(fd)
    repo = KIRunFunctionRepository()
    schema_repo = KIRunSchemaRepository()
    output = await runtime.execute(
        FunctionExecutionParameters(repo, schema_repo)
            .set_arguments({"a": 3, "b": 4})
    )
    result = output.next()
    print(result.get_result().get("result"))  # 7

asyncio.run(main())
```

## Built-in Function Namespaces

| Namespace | Functions |
|-----------|-----------|
| `System` | `If`, `GenerateEvent`, `Print`, `Wait`, `Make`, `ValidateSchema` |
| `System.Math` | `Add`, `Subtract`, `Multiply`, `Divide`, `Power`, `Abs`, `Floor`, `Ceil`, `Round`, `Random`, `Min`, `Max`, ... |
| `System.String` | `Concat`, `Split`, `Trim`, `Replace`, `IndexOf`, `SubString`, `ToUpperCase`, `ToLowerCase`, ... |
| `System.Array` | `AddElement`, `RemoveElement`, `Sort`, `Filter`, `Map`, `Reduce`, `IndexOf`, `Reverse`, ... |
| `System.Object` | `Get`, `Set`, `Keys`, `Values`, `Entries`, `Merge` |
| `System.Date` | `Now`, `Format`, `Parse`, `AddDuration`, `Difference`, `StartOf`, `EndOf`, ... |
| `System.Loop` | `RangeLoop`, `CountLoop`, `ForEachLoop`, `Break` |
| `System.Context` | `Create`, `Get`, `Set` |
| `System.JSON` | `Parse`, `Stringify` |

## Requirements

- Python >= 3.9
- `python-dateutil >= 2.8`

## License

MIT
