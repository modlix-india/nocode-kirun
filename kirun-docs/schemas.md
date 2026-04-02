# Schema & Type System

KIRun uses JSON Schema for type definitions and validation. Schemas define the types of function parameters, event outputs, and context variables.

## Schema Types

KIRun supports these primitive schema types:

| Type | Description | JavaScript | Java |
|------|-------------|------------|------|
| `INTEGER` | 32-bit integer | `number` | `Integer` / `int` |
| `LONG` | 64-bit integer | `number` | `Long` / `long` |
| `FLOAT` | 32-bit floating point | `number` | `Float` / `float` |
| `DOUBLE` | 64-bit floating point | `number` | `Double` / `double` |
| `STRING` | Text | `string` | `String` |
| `BOOLEAN` | True/false | `boolean` | `Boolean` / `boolean` |
| `OBJECT` | Key-value map | `object` | `JsonObject` |
| `ARRAY` | Ordered list | `Array` | `JsonArray` |
| `NULL` | Null value | `null` | `null` |

## Creating Schemas

### JavaScript/TypeScript

The `Schema` class provides static factory methods:

```typescript
import { Schema, SchemaType } from '@fincity/kirun-js';

// Primitive types
Schema.ofString('fieldName')
Schema.ofInteger('fieldName')
Schema.ofLong('fieldName')
Schema.ofFloat('fieldName')
Schema.ofDouble('fieldName')
Schema.ofNumber('fieldName')      // Any numeric type
Schema.ofBoolean('fieldName')
Schema.ofNull('fieldName')
Schema.ofAny('fieldName')         // Any type

// Complex types
Schema.ofObject('fieldName')
Schema.ofArray('fieldName', itemSchema)

// Generic constructor
Schema.of('fieldName', SchemaType.STRING)
```

### Java

```java
Schema.ofString("fieldName");
Schema.ofInteger("fieldName");
Schema.ofObject("fieldName");
Schema.ofArray("fieldName", itemSchema);
Schema.ofAny("fieldName");
```

## Schema Properties

### Constraints

```typescript
// String constraints
Schema.ofString('email')
    .setMinLength(5)
    .setMaxLength(255)
    .setPattern('^[a-z@.]+$')

// Number constraints
Schema.ofInteger('age')
    .setMinimum(0)
    .setMaximum(150)
    .setExclusiveMinimum(0)

// Array constraints
Schema.ofArray('items', Schema.ofString('item'))
    .setMinItems(1)
    .setMaxItems(100)
    .setUniqueItems(true)
```

### Default Values

```typescript
Schema.ofString('status')
    .setDefaultValue('active')

Schema.ofInteger('page')
    .setDefaultValue(1)
```

### Enums

```typescript
Schema.ofString('role')
    .setEnums(['admin', 'user', 'viewer'])
```

### Object Properties

```typescript
const userSchema = Schema.ofObject('user')
    .setProperties(new Map([
        ['name', Schema.ofString('name')],
        ['age', Schema.ofInteger('age')],
        ['email', Schema.ofString('email')],
    ]))
    .setRequired(['name', 'email']);
```

### Additional Properties

Allow or restrict extra properties on objects:

```typescript
import { AdditionalType } from '@fincity/kirun-js';

// Allow any additional properties
Schema.ofObject('config')
    .setAdditionalProperties(
        new AdditionalType().setSchemaValue(Schema.ofAny('value'))
    )

// Restrict to string values
Schema.ofObject('labels')
    .setAdditionalProperties(
        new AdditionalType().setSchemaValue(Schema.ofString('label'))
    )
```

### Array Items

```typescript
// Array of strings
Schema.ofArray('tags', Schema.ofString('tag'))

// Array of objects
Schema.ofArray('users', Schema.ofObject('user')
    .setProperties(new Map([
        ['id', Schema.ofInteger('id')],
        ['name', Schema.ofString('name')],
    ]))
)
```

### Tuple Validation

```typescript
const schema = Schema.ofArray('point')
    .setItems(Schema.ofNumber('coord'))
    .setMinItems(2)
    .setMaxItems(2)
```

## Schema Composition

### AnyOf

Value must match at least one of the sub-schemas:

```typescript
const schema = new Schema()
    .setAnyOf([
        Schema.ofString('str'),
        Schema.ofInteger('int'),
    ])
```

### AllOf

Value must match all sub-schemas:

```typescript
const schema = new Schema()
    .setAllOf([
        baseSchema,
        extensionSchema,
    ])
```

### OneOf

Value must match exactly one sub-schema:

```typescript
const schema = new Schema()
    .setOneOf([
        Schema.ofString('str'),
        Schema.ofInteger('int'),
    ])
```

### Not

Value must not match the sub-schema:

```typescript
const schema = new Schema()
    .setNot(Schema.ofNull('null'))
```

## Schema References

Schemas can reference other schemas by namespace and name:

```typescript
const schema = new Schema()
    .setRef('System.UserSchema')
```

Referenced schemas are resolved through the `SchemaRepository` at runtime.

## Schema Validation

### JavaScript

```typescript
import { SchemaValidator, KIRunSchemaRepository } from '@fincity/kirun-js';

const schema = Schema.ofObject('user')
    .setProperties(new Map([
        ['name', Schema.ofString('name')],
        ['age', Schema.ofInteger('age').setMinimum(0)],
    ]))
    .setRequired(['name']);

const repository = new KIRunSchemaRepository();

// Validate - returns validated/coerced value or throws
const validated = await SchemaValidator.validate(
    null,       // path (for error messages)
    schema,     // schema to validate against
    repository, // schema repository for $ref resolution
    userData,   // value to validate
);
```

### Java

```java
SchemaValidator.validate(null, schema, schemaRepository, userData);
```

Validation performs:
- Type checking
- Constraint validation (min/max, pattern, etc.)
- Required field checks
- Nested object/array validation
- Schema reference resolution
- Default value application

## JSON Representation

Schemas serialize to JSON for storage and transmission:

```json
{
    "name": "user",
    "type": ["OBJECT"],
    "properties": {
        "name": {
            "name": "name",
            "type": ["STRING"]
        },
        "age": {
            "name": "age",
            "type": ["INTEGER"],
            "minimum": 0,
            "maximum": 150
        },
        "roles": {
            "name": "roles",
            "type": ["ARRAY"],
            "items": {
                "type": ["STRING"],
                "enums": ["admin", "user", "viewer"]
            }
        }
    },
    "required": ["name"]
}
```

> **Note:** The `type` field is always an array (e.g., `["STRING"]`) to support multi-type schemas.

## Using Schemas with Parameters

When defining function parameters:

```typescript
import { Parameter, Schema } from '@fincity/kirun-js';

// Simple parameter
Parameter.of('userId', Schema.ofString('userId'))

// Parameter with constraints
Parameter.of('age', Schema.ofInteger('age').setMinimum(0).setMaximum(200))

// Variable argument parameter
Parameter.of('values', Schema.ofNumber('values'), true)  // true = varargs

// Object parameter
Parameter.of('config', Schema.ofObject('config')
    .setProperties(new Map([
        ['host', Schema.ofString('host')],
        ['port', Schema.ofInteger('port').setDefaultValue(8080)],
    ]))
)
```

## Using Schemas with Events

When defining function output events:

```typescript
import { Event, Schema } from '@fincity/kirun-js';

// Output event with typed results
const outputEvent = new Event('output', new Map([
    ['result', Schema.ofString('result')],
    ['count', Schema.ofInteger('count')],
]));

// Or using the helper
Event.outputEventMapEntry(new Map([
    ['result', Schema.ofAny('result')],
]))
```
