# Built-in Functions Reference

KIRun ships with 120+ built-in functions across 9 namespaces. All functions are available in both the Java and JavaScript runtimes.

## System Functions

Core control flow and utility functions.

### If (`System.If`)

Conditional branching. Evaluates a condition and emits either a `true` or `false` event.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `condition` | Boolean | The condition to evaluate |

**Events:** `true`, `false`

**Usage:**
```json
{
    "statementName": "checkAge",
    "namespace": "System",
    "name": "If",
    "parameterMap": {
        "condition": {
            "one": { "key": "one", "type": "EXPRESSION", "expression": "Arguments.age >= 18" }
        }
    }
}
```

Downstream steps use `dependentStatements` and `executeIftrue` to branch on the result.

---

### GenerateEvent (`System.GenerateEvent`)

Emits a named event with result data. This is how functions return output to callers.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `eventName` | String | Name of the event to emit |
| `results` | Object | Key-value pairs to include in the event |

**Usage:**
```json
{
    "statementName": "output",
    "namespace": "System",
    "name": "GenerateEvent",
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

---

### Make (`System.Make`)

Constructs objects/arrays by evaluating expressions within a result shape template. Any string value wrapped in `{{ }}` is evaluated as an expression.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `resultShape` | Any | Template with `{{ }}` expressions |

**Events:** `output` with `value`

**Usage:**
```json
{
    "parameterMap": {
        "resultShape": {
            "one": {
                "key": "one",
                "type": "VALUE",
                "value": {
                    "name": "{{Arguments.firstName + ' ' + Arguments.lastName}}",
                    "age": "{{Arguments.age}}",
                    "items": ["{{Steps.fetch.output.data}}"]
                }
            }
        }
    }
}
```

---

### Print (`System.Print`)

Outputs a value for debugging purposes.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `values` | Any (varargs) | Values to print |

---

### Wait (`System.Wait`)

Pauses execution for a specified duration.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `millis` | Long | Duration in milliseconds |

---

### ValidateSchema (`System.ValidateSchema`)

Validates a value against a JSON Schema.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `schema` | Object | JSON Schema to validate against |
| `value` | Any | Value to validate |

---

## Math Functions (`System.Math`)

### Add

Addition with variable arguments. Sums all provided values.

**Parameters:**
| Name | Type | Varargs | Description |
|------|------|---------|-------------|
| `value` | Number | Yes | Numbers to add |

**Events:** `output` with `value` (sum)

---

### Minimum / Maximum

Find the minimum or maximum of provided values.

**Parameters:** `value` (Number, varargs)

**Events:** `output` with `value`

---

### Random

Generate a random number.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `minValue` | Number | Minimum (inclusive) |
| `maxValue` | Number | Maximum (exclusive) |

---

### RandomAny

Generate a random value within a broader set of rules.

---

### Hypotenuse

Calculate the hypotenuse given two sides.

**Parameters:** `value` (Number, varargs)

---

## String Functions (`System.String`)

### Concatenate

Join strings together.

**Parameters:**
| Name | Type | Varargs | Description |
|------|------|---------|-------------|
| `value` | String | Yes | Strings to concatenate |

**Events:** `output` with `value` (joined string)

---

### Split

Split a string by a delimiter.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `value` | String | String to split |
| `searchValue` | String | Delimiter |
| `limit` | Integer | Max splits (optional) |

**Events:** `output` with `value` (array of strings)

---

### Reverse

Reverse a string.

**Parameters:** `value` (String)

---

### Matches

Test a string against a regular expression.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `value` | String | String to test |
| `regex` | String | Regular expression pattern |

---

### PrePad / PostPad

Pad a string to a target length.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `value` | String | Original string |
| `length` | Integer | Target length |
| `padString` | String | Character(s) to pad with |

---

### TrimTo

Trim a string to a maximum length.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `value` | String | String to trim |
| `length` | Integer | Maximum length |

---

### ToString

Convert any value to its string representation.

**Parameters:** `value` (Any)

---

### InsertAtGivenPosition

Insert a substring at a specific position.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `value` | String | Original string |
| `insertString` | String | String to insert |
| `position` | Integer | Insert position |

---

### DeleteForGivenLength

Delete characters from a position for a given length.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `value` | String | Original string |
| `position` | Integer | Start position |
| `length` | Integer | Number of characters |

---

### ReplaceAtGivenPosition

Replace characters at a position.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `value` | String | Original string |
| `replaceString` | String | Replacement string |
| `position` | Integer | Start position |
| `length` | Integer | Number of characters to replace |

---

### RegionMatches

Compare regions of two strings.

---

## Array Functions (`System.Array`)

### AddFirst / InsertLast

Add an element to the beginning or end of an array.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `source` | Array | Source array |
| `element` | Any | Element to add |

---

### Insert

Insert an element at a specific index.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `source` | Array | Source array |
| `element` | Any | Element to insert |
| `index` | Integer | Insert position |

---

### Delete / DeleteFirst / DeleteLast / DeleteFrom

Remove elements from an array.

- `Delete` - Remove element at index
- `DeleteFirst` - Remove first element
- `DeleteLast` - Remove last element
- `DeleteFrom` - Remove range of elements

---

### Sort

Sort an array.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `source` | Array | Source array |
| `ascending` | Boolean | Sort direction (default: true) |
| `sortOnProperty` | String | Property to sort by (for object arrays) |

---

### Reverse

Reverse an array.

**Parameters:** `source` (Array)

---

### Shuffle

Randomly shuffle an array.

**Parameters:** `source` (Array)

---

### IndexOf / LastIndexOf

Find the first or last index of an element.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `source` | Array | Array to search |
| `element` | Any | Element to find |
| `findFrom` | Integer | Starting index (optional) |

---

### IndexOfArray / LastIndexOfArray

Find the first or last occurrence of a sub-array.

---

### SubArray

Extract a portion of an array.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `source` | Array | Source array |
| `srcPos` | Integer | Start index |
| `length` | Integer | Number of elements |

---

### Copy

Copy array elements.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `source` | Array | Source array |
| `srcPos` | Integer | Source start position |
| `destination` | Array | Destination array |
| `destPos` | Integer | Destination position |
| `length` | Integer | Number of elements |

---

### Concatenate

Concatenate multiple arrays.

**Parameters:** `source` (Array, varargs)

---

### Join

Join array elements into a string with a separator.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `source` | Array | Source array |
| `separator` | String | Delimiter between elements |

---

### Min / Max

Find the minimum or maximum element in an array.

---

### RemoveDuplicates

Remove duplicate elements from an array.

---

### BinarySearch

Perform binary search on a sorted array.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `source` | Array | Sorted source array |
| `element` | Any | Element to find |

---

### Fill

Fill an array with a value.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `source` | Array | Source array |
| `element` | Any | Value to fill with |
| `srcPos` | Integer | Start index |
| `length` | Integer | Number of elements |

---

### Rotate

Rotate array elements.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `source` | Array | Source array |
| `distance` | Integer | Rotation distance |

---

### Frequency

Count occurrences of an element in an array.

---

### Disjoint

Check if two arrays have no elements in common.

---

### MisMatch

Find the first index where two arrays differ.

---

### Compare / Equals

Compare two arrays for ordering or equality.

---

### ArrayToObject

Convert an array to an object using a key property.

---

### ArrayToArrayOfObjects

Convert an array of values to an array of objects.

---

## Object Functions (`System.Object`)

### ObjectKeys

Get all keys of an object as an array.

**Parameters:** `source` (Object)

**Events:** `output` with `value` (array of strings)

---

### ObjectValues

Get all values of an object as an array.

**Parameters:** `source` (Object)

---

### ObjectEntries

Get key-value pairs as an array of [key, value] arrays.

**Parameters:** `source` (Object)

---

### ObjectPutValue

Set a property on an object.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `source` | Object | Source object |
| `key` | String | Property key |
| `value` | Any | Value to set |

---

### ObjectDeleteKey

Remove a property from an object.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `source` | Object | Source object |
| `key` | String | Property key to remove |

---

### ObjectConvert

Convert an object to a different structure.

---

## Date Functions (`System.Date`)

All date functions work with ISO 8601 timestamp strings.

### GetCurrent (GetCurrentTimestamp)

Get the current date/time.

**Events:** `output` with `value` (ISO timestamp string)

---

### FromNow

Get relative time descriptions (e.g., "2 hours ago").

---

### AddSubtractTime

Add or subtract time from a timestamp.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `isoTimestamp` | String | Base timestamp |
| `value` | Integer | Amount to add/subtract |
| `unit` | String | Unit: years, months, days, hours, minutes, seconds, milliseconds |

---

### Difference

Calculate the difference between two timestamps.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `isoTimestamp1` | String | First timestamp |
| `isoTimestamp2` | String | Second timestamp |
| `unit` | String | Unit for the result |

---

### EpochToTimestamp

Convert epoch milliseconds to ISO timestamp.

---

### TimestampToEpoch

Convert ISO timestamp to epoch milliseconds.

---

### FromDateString

Parse a date string with a specific format.

---

### ToDateString

Format a timestamp to a specific format.

---

### IsBetween

Check if a timestamp falls between two others.

---

### IsValidISODate

Check if a string is a valid ISO date.

---

### StartEndOf

Get the start or end of a time period (day, month, year, etc.).

---

### LastFirstOf

Get the first or last occurrence of a weekday/month.

---

### TimeAs

Extract a specific component (year, month, day, hour, etc.).

---

### GetNames

Get localized names for months, weekdays, etc.

---

### SetTimeZone

Convert a timestamp to a different timezone.

---

## Loop Functions (`System.Loop`)

### CountLoop

Execute a block a fixed number of times.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `count` | Integer | Number of iterations |

**Events:** `iteration` (with `index`), `output`

The `iteration` event fires for each loop cycle. Steps that depend on `Steps.<loopStep>.iteration` will execute once per iteration.

---

### ForEachLoop

Iterate over each element in an array.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `source` | Array | Array to iterate over |

**Events:** `iteration` (with `index`, `each`), `output`

---

### RangeLoop

Iterate over a numeric range.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `from` | Integer | Start value (default: 0) |
| `to` | Integer | End value (exclusive) |
| `step` | Integer | Step size (default: 1) |

**Events:** `iteration` (with `index`), `output`

---

### Break

Break out of a loop.

---

## Context Functions (`System.Context`)

### Create

Create a context variable.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `name` | String | Variable name |
| `schema` | Object | JSON Schema for the variable |

After creation, the variable is accessible via `Context.<name>`.

---

### Get

Get the value of a context variable.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `name` | String | Variable name |

---

### Set (SetFunction)

Set the value of a context variable.

**Parameters:**
| Name | Type | Description |
|------|------|-------------|
| `name` | String | Variable name |
| `value` | Any | Value to set |

---

## JSON Functions (`System.Json`)

### JSONParse

Parse a JSON string into an object.

**Parameters:** `value` (String)

---

### JSONStringify

Convert an object to a JSON string.

**Parameters:** `value` (Any)
