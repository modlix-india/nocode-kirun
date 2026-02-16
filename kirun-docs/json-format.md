# JSON Definition Format

This document specifies the complete JSON format for KIRun function definitions. This is the canonical format used for storage, transmission, and cross-platform execution.

## FunctionDefinition

The top-level structure:

```json
{
    "name": "functionName",
    "namespace": "Namespace",
    "version": 1,
    "description": "Optional short description",
    "documentation": "Optional detailed markdown documentation",
    "metadata": { "key": "value" },
    "parameters": { ... },
    "events": { ... },
    "steps": { ... },
    "stepGroups": { ... },
    "parts": [ ... ]
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | String | Yes | Function name |
| `namespace` | String | Yes | Dot-separated namespace (e.g., `MyApp.Utils`) |
| `version` | Integer | No | Definition version (default: 1) |
| `description` | String | No | Short one-line description |
| `documentation` | String | No | Detailed documentation (markdown supported) |
| `metadata` | Object | No | Arbitrary key-value metadata |
| `parameters` | Object | No | Input parameter definitions |
| `events` | Object | No | Output event definitions |
| `steps` | Object | Yes | Execution steps (the function graph) |
| `stepGroups` | Object | No | Visual grouping of steps |
| `parts` | Array | No | Sub-function definitions |

## Parameters

Parameters are keyed by parameter name:

```json
{
    "parameters": {
        "userId": {
            "parameterName": "userId",
            "schema": {
                "type": ["STRING"]
            },
            "variableArgument": false,
            "type": "EXPRESSION"
        },
        "values": {
            "parameterName": "values",
            "schema": {
                "type": ["INTEGER"]
            },
            "variableArgument": true,
            "type": "EXPRESSION"
        }
    }
}
```

### Parameter Fields

| Field | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `parameterName` | String | Yes | - | Parameter name (must match the key) |
| `schema` | Schema | Yes | - | JSON Schema defining the type |
| `variableArgument` | Boolean | No | `false` | Accept multiple values (varargs) |
| `type` | String | No | `"EXPRESSION"` | `"EXPRESSION"` or `"CONSTANT"` |

## Events

Events define the output channels:

```json
{
    "events": {
        "output": {
            "name": "output",
            "parameters": {
                "result": {
                    "type": ["STRING"]
                },
                "count": {
                    "type": ["INTEGER"]
                }
            }
        },
        "error": {
            "name": "error",
            "parameters": {
                "message": {
                    "type": ["STRING"]
                }
            }
        }
    }
}
```

### Event Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | String | Yes | Event name (must match the key) |
| `parameters` | Object | Yes | Map of parameter name to Schema |

### Standard Event Names

| Name | Usage |
|------|-------|
| `output` | Standard successful result |
| `error` | Error information |
| `iteration` | Loop iteration data |
| `true` | Conditional true branch |
| `false` | Conditional false branch |

## Steps (Statements)

Steps are the execution graph nodes, keyed by statement name:

```json
{
    "steps": {
        "fetchUser": {
            "statementName": "fetchUser",
            "namespace": "MyApp",
            "name": "GetUser",
            "parameterMap": { ... },
            "dependentStatements": { ... },
            "executeIftrue": { ... },
            "position": { "left": 100, "top": 200 },
            "comment": "Fetch user by ID",
            "description": "Retrieves user from database"
        }
    }
}
```

### Statement Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `statementName` | String | Yes | Unique identifier (must match the key) |
| `namespace` | String | Yes | Namespace of function to call |
| `name` | String | Yes | Name of function to call |
| `parameterMap` | Object | No | Parameter bindings |
| `dependentStatements` | Object | No | Step dependencies |
| `executeIftrue` | Object | No | Conditional execution flags |
| `position` | Object | No | Visual editor position `{left, top}` |
| `comment` | String | No | Developer comment |
| `description` | String | No | Step description |
| `override` | Boolean | No | Override flag for parts |

## ParameterMap

The parameter map binds values to function parameters. It's a nested structure:

```
parameterMap[parameterName][referenceKey] = ParameterReference
```

```json
{
    "parameterMap": {
        "value": {
            "ref1": {
                "key": "ref1",
                "type": "EXPRESSION",
                "expression": "Arguments.x",
                "order": 1
            },
            "ref2": {
                "key": "ref2",
                "type": "VALUE",
                "value": 42,
                "order": 2
            }
        },
        "separator": {
            "ref3": {
                "key": "ref3",
                "type": "VALUE",
                "value": ", "
            }
        }
    }
}
```

### ParameterReference Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `key` | String | Yes | Unique key for this reference |
| `type` | String | Yes | `"VALUE"` or `"EXPRESSION"` |
| `value` | Any | Conditional | Static value (when type is `VALUE`) |
| `expression` | String | Conditional | Expression string (when type is `EXPRESSION`) |
| `order` | Integer | No | Ordering for variable arguments |

### VALUE vs EXPRESSION

- **VALUE** - The value is used directly as-is. Use for static/literal values.
- **EXPRESSION** - The expression string is evaluated against the execution context. Use for dynamic values.

### Variable Arguments

For parameters with `variableArgument: true`, provide multiple references with `order` to control ordering:

```json
{
    "value": {
        "first": { "key": "first", "type": "EXPRESSION", "expression": "Arguments.a", "order": 1 },
        "second": { "key": "second", "type": "EXPRESSION", "expression": "Arguments.b", "order": 2 },
        "third": { "key": "third", "type": "VALUE", "value": 10, "order": 3 }
    }
}
```

The function receives these as an ordered array: `[<Arguments.a>, <Arguments.b>, 10]`.

### Nested Parameter Keys

For functions like `GenerateEvent`, the parameter map can use dotted keys:

```json
{
    "parameterMap": {
        "eventName": {
            "one": { "key": "one", "type": "VALUE", "value": "output" }
        },
        "results": {
            "result": {
                "key": "result",
                "type": "EXPRESSION",
                "expression": "Steps.compute.output.value"
            }
        }
    }
}
```

## Dependent Statements

Dependencies are expressed as a map of step event paths to booleans:

```json
{
    "dependentStatements": {
        "Steps.step1.output": true,
        "Steps.step2.output": false
    }
}
```

The key format is: `Steps.<statementName>.<eventName>`

The boolean value works with `executeIftrue`:
- If the key exists in `dependentStatements`, the step waits for that dependency
- If `executeIftrue` has the same key set to `true`, the step only executes when that specific event fires

### Conditional Branching Example

```json
{
    "steps": {
        "check": {
            "statementName": "check",
            "namespace": "System",
            "name": "If",
            "parameterMap": {
                "condition": {
                    "one": { "key": "one", "type": "EXPRESSION", "expression": "Arguments.age >= 18" }
                }
            }
        },
        "adult": {
            "statementName": "adult",
            "namespace": "System",
            "name": "GenerateEvent",
            "parameterMap": {
                "eventName": { "one": { "key": "one", "type": "VALUE", "value": "output" } },
                "results": {
                    "category": { "key": "category", "type": "VALUE", "value": "adult" }
                }
            },
            "dependentStatements": { "Steps.check.true": true },
            "executeIftrue": { "Steps.check.true": true }
        },
        "minor": {
            "statementName": "minor",
            "namespace": "System",
            "name": "GenerateEvent",
            "parameterMap": {
                "eventName": { "one": { "key": "one", "type": "VALUE", "value": "output" } },
                "results": {
                    "category": { "key": "category", "type": "VALUE", "value": "minor" }
                }
            },
            "dependentStatements": { "Steps.check.false": true },
            "executeIftrue": { "Steps.check.false": true }
        }
    }
}
```

## Schema Format

Schemas follow JSON Schema conventions with KIRun-specific type arrays:

```json
{
    "name": "fieldName",
    "namespace": "Namespace",
    "type": ["STRING"],
    "version": 1,

    "defaultValue": "hello",
    "enums": ["a", "b", "c"],

    "minimum": 0,
    "maximum": 100,
    "exclusiveMinimum": 0,
    "exclusiveMaximum": 100,
    "multipleOf": 5,

    "minLength": 1,
    "maxLength": 255,
    "pattern": "^[a-z]+$",

    "items": { "type": ["STRING"] },
    "minItems": 0,
    "maxItems": 50,
    "uniqueItems": false,

    "properties": {
        "name": { "type": ["STRING"] },
        "age": { "type": ["INTEGER"] }
    },
    "additionalProperties": {
        "schemaValue": { "type": ["ANY"] }
    },
    "required": ["name"],

    "anyOf": [ ... ],
    "allOf": [ ... ],
    "oneOf": [ ... ],
    "not": { ... },

    "ref": "Namespace.SchemaName"
}
```

> **Important:** The `type` field is always an array of type strings, e.g., `["STRING"]` or `["INTEGER", "NULL"]` for nullable types.

## Position

Visual editor position for steps and step groups:

```json
{
    "position": {
        "left": 250,
        "top": 150
    }
}
```

## Step Groups

Optional visual grouping:

```json
{
    "stepGroups": {
        "validation": {
            "statementGroupName": "validation",
            "statementNames": ["validate1", "validate2"],
            "position": { "left": 100, "top": 100 }
        }
    }
}
```

## Parts

Sub-function definitions that can be referenced from the parent:

```json
{
    "parts": [
        {
            "name": "helper",
            "namespace": "MyApp",
            "parameters": { ... },
            "events": { ... },
            "steps": { ... }
        }
    ]
}
```

## Complete Example

```json
{
    "name": "processOrder",
    "namespace": "MyApp",
    "version": 1,
    "description": "Process an order with discount calculation",
    "parameters": {
        "items": {
            "parameterName": "items",
            "schema": {
                "type": ["ARRAY"],
                "items": {
                    "type": ["OBJECT"],
                    "properties": {
                        "name": { "type": ["STRING"] },
                        "price": { "type": ["DOUBLE"] },
                        "quantity": { "type": ["INTEGER"] }
                    }
                }
            }
        },
        "discountPercent": {
            "parameterName": "discountPercent",
            "schema": {
                "type": ["DOUBLE"],
                "defaultValue": 0,
                "minimum": 0,
                "maximum": 100
            }
        }
    },
    "events": {
        "output": {
            "name": "output",
            "parameters": {
                "total": { "type": ["DOUBLE"] },
                "itemCount": { "type": ["INTEGER"] }
            }
        }
    },
    "steps": {
        "createTotal": {
            "statementName": "createTotal",
            "namespace": "System.Context",
            "name": "Create",
            "parameterMap": {
                "name": {
                    "one": { "key": "one", "type": "VALUE", "value": "total", "order": 1 }
                },
                "schema": {
                    "one": { "key": "one", "type": "VALUE", "value": { "type": "DOUBLE" }, "order": 1 }
                }
            }
        },
        "loop": {
            "statementName": "loop",
            "namespace": "System.Loop",
            "name": "ForEachLoop",
            "parameterMap": {
                "source": {
                    "one": { "key": "one", "type": "EXPRESSION", "expression": "Arguments.items", "order": 1 }
                }
            },
            "dependentStatements": {
                "Steps.createTotal.output": true,
                "Steps.addToTotal.output": true
            }
        },
        "calcItemTotal": {
            "statementName": "calcItemTotal",
            "namespace": "System",
            "name": "Make",
            "parameterMap": {
                "resultShape": {
                    "one": {
                        "key": "one",
                        "type": "VALUE",
                        "value": "{{Steps.loop.iteration.each.price * Steps.loop.iteration.each.quantity}}",
                        "order": 1
                    }
                }
            },
            "dependentStatements": { "Steps.loop.iteration": true },
            "executeIftrue": { "Steps.loop.iteration": true }
        },
        "addToTotal": {
            "statementName": "addToTotal",
            "namespace": "System.Context",
            "name": "Set",
            "parameterMap": {
                "name": {
                    "one": { "key": "one", "type": "VALUE", "value": "total", "order": 1 }
                },
                "value": {
                    "one": {
                        "key": "one",
                        "type": "EXPRESSION",
                        "expression": "Context.total + Steps.calcItemTotal.output.value",
                        "order": 1
                    }
                }
            },
            "dependentStatements": { "Steps.calcItemTotal.output": true }
        },
        "applyDiscount": {
            "statementName": "applyDiscount",
            "namespace": "System",
            "name": "Make",
            "parameterMap": {
                "resultShape": {
                    "one": {
                        "key": "one",
                        "type": "VALUE",
                        "value": "{{Context.total * (1 - Arguments.discountPercent / 100)}}",
                        "order": 1
                    }
                }
            },
            "dependentStatements": { "Steps.loop.output": true }
        },
        "genOutput": {
            "statementName": "genOutput",
            "namespace": "System",
            "name": "GenerateEvent",
            "parameterMap": {
                "eventName": {
                    "one": { "key": "one", "type": "VALUE", "value": "output", "order": 1 }
                },
                "results": {
                    "total": {
                        "key": "total",
                        "type": "EXPRESSION",
                        "expression": "Steps.applyDiscount.output.value",
                        "order": 1
                    },
                    "itemCount": {
                        "key": "itemCount",
                        "type": "EXPRESSION",
                        "expression": "Arguments.items.length",
                        "order": 1
                    }
                }
            },
            "dependentStatements": { "Steps.applyDiscount.output": true }
        }
    }
}
```
