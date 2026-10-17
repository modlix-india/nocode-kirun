# kirun-rust design rules

kirun-rust is a port of **kirun-js** ([`kirun-js/src/engine`](https://github.com/modlix-india/nocode-kirun/tree/master/kirun-js/src/engine)), the reference
implementation. When behaviour is unclear, kirun-js wins (except where kirun-rust answers as kirun-java: see the last section); kirun-java and kirun-py
are secondary references. Every rule below exists so the four ports stay
diffable file-by-file.

## Layout

`src/<js path in snake_case>` mirrors `kirun-js/src/engine/<js path>`:

| kirun-js                                            | kirun-rust                                              |
|-----------------------------------------------------|---------------------------------------------------------|
| `function/system/array/AddFirst.ts` → `AddFirst`     | `src/function/system/array/add_first.rs` → `AddFirst`    |
| `runtime/expression/ExpressionEvaluator.ts`          | `src/runtime/expression/expression_evaluator.rs`        |
| `json/schema/validator/SchemaValidator.ts`           | `src/json/schema/validator/schema_validator.rs`         |
| `repository/KIRunFunctionRepository.ts`              | `src/repository/kirun_function_repository.rs`           |

Type names keep their JS spelling (`KIRuntime`, `JSONParse`, `DSLCompiler`).
File names are the snake_case of the type (`ki_runtime.rs`, `json_parse.rs`,
`dsl_compiler.rs`). One JS file = one Rust file, except small enum families
(`json/schema/type`, `json/schema/string`, the DSL AST nodes), which share a `mod.rs`.
JS keywords as module names use raw identifiers (`system::r#if`, `system::r#loop`).

Code that reimplements a JS library lives next to its only user:
`function/system/date/luxon.rs` (luxon 3.7) and `util/string/js_string.rs`
(UTF-16 `String.prototype` methods).

Tests mirror `kirun-js/__tests__/engine/...`, grouped into **one integration
test binary per area** to keep link times sane:

```
tests/<area>/main.rs            # `mod common; mod add_first_test; ...`
tests/<area>/add_first_test.rs  # port of AddFirstTest.ts
tests/common/mod.rs             # shared helpers (included via #[path])
```

## Naming

* Methods: JS camelCase → snake_case, **keeping `get_`/`set_`/`is_`**
  (`getSignature()` → `get_signature()`), so a grep for a JS method finds the Rust one.
* Static constructors keep their names (`Schema.ofString` → `Schema::of_string`).
  Exceptions: JS `X.from(json)` → `X::from_value(&Value) -> Result<X>`;
  `toJSON()` → `to_json() -> Value`; `new X(...)` → `X::new(...)`.
* Builder setters consume and return `Self` (`fn set_name(mut self, ..) -> Self`).
  Data structs also expose `pub` fields so engine code can mutate in place.
* Static string constants: `Event.OUTPUT` → `Event::OUTPUT` (`&'static str`).

## Values

* Every JSON value is `serde_json::Value` (`crate::Value`), built with
  `preserve_order` so object key order matches JS insertion order.
* Ordered maps are `indexmap::IndexMap<String, V>` (`crate::Map<V>`) wherever JS
  uses `Map<string, V>`.
* **JS `undefined` and `null` are both `Value::Null`.** (Same choice as the Java
  and Python ports.) A JS test asserting `toBeUndefined()` asserts `Value::Null`
  in Rust; where JS distinguishes the two, note it in the test with a comment.
* JS has one number type. Arithmetic results that are integral and fit `i64`
  are produced as integers (`json!(3)`, not `json!(3.0)`); use
  `crate::util::primitive::number_value(f64)` to build them. Tests compare
  numbers with `assert_json_eq!` (in `tests/common`), which treats `3 == 3.0`.

## Errors

`crate::KIRunError` (see `src/exception`) replaces every JS exception class.
Fallible APIs return `crate::Result<T>`. Tests that use `rejects.toThrow(msg)`
assert `err.to_string()` contains / equals the JS message.

## Async and sharing

* `Function::execute`, `Repository::find/filter` and `SchemaValidator::validate`
  are `async` (`async_trait`, `Send`). The expression engine is synchronous.
* JS passes `context`, `steps`, `events` and `executionContext` maps **by
  reference** between a parent execution and its steps (and loop generators read
  `executionContext` lazily for `Break`). In Rust these are
  `crate::Shared<T> = Arc<parking_lot::RwLock<T>>`. Never hold a lock across an
  `.await`.
* Functions are `Arc<dyn Function>`; schemas from a repository are `Arc<Schema>`.

## Tests first

Each ported test keeps the JS test name (snake_case) and the same inputs and
expected outputs. Do not weaken an assertion to make it pass; if the Rust port
must differ (undefined vs null, float formatting), comment why at the assertion.

## Where kirun-rust answers as kirun-java

The Rust core runs stored server functions on this crate, and those were written against
kirun-java (the Java core's runtime). Since 2026-10-17 kirun-rust keeps kirun-js's layout and
names but answers as kirun-java 4.9.2 where the two differ in what a function returns (the
browser keeps kirun-js):

* **Arguments**: a variable argument drops references that come to null; an absent argument
  whose schema has no type fails with kirun-java's `value`.
* **Planning**: kirun-java's unguarded reads fail the run (a parameter the function lacks, an
  unnamed OBJECT schema given a VALUE object, a `{isExpression, value}` without its keys, a
  typeless parameter left out, events declared without `output`); definition errors print as
  its `[StatementMessage(...)]` list.
* **Errors**: a function's own runtime error is passed on unwrapped; only argument validation
  names the function and parameter. kirun-java's unchecked exceptions carry their Java message
  (`KIRunError::Uncaught`).
* **Schemas**: messages carry the schema path (`path - message`) and kirun-java's wording;
  `INTEGER` / `LONG` ranges; full-match `pattern`; zero maxima; content equality for enums and
  `uniqueItems`; `$ref` and typeless schemas in conversion; kirun-java's number and string
  convertors, and a refused primitive conversion leaving the type check at once.
* **Expressions** (`operators::java_semantics`, `java_null`): bare words are text, number literals
  follow `processNumbers`; operators use kirun-java's null, string, boolean and `int` / `long`
  rules; Java null and JSON null are told apart where a result can be Java null.
* **Dates** (`function/system/date/java_time.rs`, `java_format.rs`): `java.time` semantics,
  `DateTimeFormatter` patterns, kirun-java's output names, the JVM's default zone and locale.
* **Math** (`function/system/math`): result types as kirun-java's (`Floor`, `Ceiling`, the
  trigonometric and log functions, `SquareRoot`, `Power`, `ArcTangent2` always a double;
  `Absolute` keeps the type; `Round` is `Math.round`; `Add` keeps the widest type and raises no
  event for no values; `Maximum` / `Minimum` keep the later of equal values), and `ToDegrees` /
  `ToRadians` the right way round.

Not reproduced: kirun-java's 32-bit `float` arithmetic on decimals parsed from text, NaN and
Infinity values, output orders kirun-java takes from hash sets that change per JVM run, and the
last digit of some trigonometric, hyperbolic and exponential results (kirun-java's fdlibm or
CPU intrinsics against the platform libm).
Tests asserting a kirun-js answer that changed say so at the assertion; the `java_*` test files
and fixtures hold answers measured on kirun-java.
