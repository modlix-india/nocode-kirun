<p align="center">
  <a href="https://kirun.modlix.com">
    <picture>
      <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/modlix-india/nocode-kirun/master/.github/assets/logo-dark.png">
      <img src="https://raw.githubusercontent.com/modlix-india/nocode-kirun/master/.github/assets/logo-light.png" alt="kirun" width="280">
    </picture>
  </a>
</p>

<h3 align="center">kirun-py: the Python runtime for KIRun</h3>

<p align="center">
  <a href="https://kirun.modlix.com/playground"><strong>Playground</strong></a> ·
  <a href="https://kirun.modlix.com/docs">Docs</a> ·
  <a href="https://kirun.modlix.com/functions">Functions</a> ·
  <a href="https://github.com/modlix-india/nocode-kirun">GitHub</a>
</p>

<p align="center">
  <a href="https://pypi.org/project/kirun-py/"><img src="https://img.shields.io/pypi/v/kirun-py?style=flat-square&color=7AA2F7&labelColor=0A0B11" alt="PyPI"></a>
  <a href="https://github.com/modlix-india/nocode-kirun/blob/master/LICENSE"><img src="https://img.shields.io/badge/license-MIT-9ECE6A?style=flat-square&labelColor=0A0B11" alt="MIT License"></a>
</p>

---

Python runtime for **KIRun** — the Kinetic Instructions execution engine used by the [Modlix](https://modlix.com) no-code/low-code platform.

## Installation

```bash
pip install kirun-py
```

## Overview

KIRun executes `FunctionDefinition` graphs (DAGs) with:

- **Expression evaluation** — arithmetic, logical, string, ternary operators
- **Schema validation** — JSON Schema-based type checking
- **166 built-in functions** — Math, String, Array, Object, Date, JSON, Loop, Context ([catalog](https://kirun.modlix.com/functions))
- **Async execution** — full `async`/`await` support
- **Debug support** — execution tracing and performance profiling
- **DSL** — `kirun_py.dsl.dsl_compiler.DSLCompiler` compiles the KIRun text format to JSON and back

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

| Namespace | Count | Some of them |
|-----------|------:|--------------|
| `System` | 6 | `If`, `GenerateEvent`, `Print`, `Wait`, `Make`, `ValidateSchema` |
| `System.Math` | 32 | `Add`, `Absolute`, `Floor`, `Ceiling`, `Round`, `Power`, `Minimum`, `Maximum`, `Random`, ... |
| `System.String` | 33 | `Concatenate`, `Split`, `Trim`, `Replace`, `IndexOf`, `SubString`, `UpperCase`, `LowerCase`, ... |
| `System.Array` | 31 | `AddFirst`, `InsertLast`, `Delete`, `Sort`, `Reverse`, `IndexOf`, `SubArray`, `Join`, ... |
| `System.Object` | 6 | `ObjectKeys`, `ObjectValues`, `ObjectEntries`, `ObjectPutValue`, `ObjectDeleteKey`, `ObjectConvert` |
| `System.Date` | 49 | `GetCurrentTimestamp`, `AddTime`, `SubtractTime`, `Difference`, `StartOf`, `EndOf`, `FromNow`, ... |
| `System.Loop` | 4 | `RangeLoop`, `CountLoop`, `ForEachLoop`, `Break` |
| `System.Context` | 3 | `Create`, `Get`, `Set` |
| `System.JSON` | 2 | `JSONParse`, `JSONStringify` |

Every function, with its signature and an example you can run, is in the
[function catalog](https://kirun.modlix.com/functions).

## Requirements

- Python >= 3.9
- `python-dateutil >= 2.8`

## License

MIT
