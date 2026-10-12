//! Port of kirun-js `function/system/array/AbstractArrayFunction.ts`.
//!
//! JS uses an abstract base class holding the shared parameters/events and a signature built
//! from (name, parameters, event). In Rust the shared definitions are associated functions
//! returning fresh values, each array function stores the `FunctionSignature` built by
//! [`AbstractArrayFunction::signature`], and the small JS-semantics helpers the array functions
//! share (argument access, JS equality and `<`, key paths) live here as `pub(crate)` items.

use crate::json::schema::r#type::{SchemaType, TypeUtil};
use crate::json::schema::Schema;
use crate::model::{Event, EventResult, FunctionOutput, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::util::primitive::primitive_util::PrimitiveUtil;
use crate::util::primitive::{js_string_to_number, js_to_string};
use crate::{KIRunError, Map, Result, Value};

pub struct AbstractArrayFunction;

fn one(name: &str, schema: Schema) -> Map<Schema> {
    let mut m = Map::new();
    m.insert(name.to_string(), schema);
    m
}

impl AbstractArrayFunction {
    pub const EVENT_INDEX_NAME: &'static str = "index";
    pub const EVENT_RESULT_NAME: &'static str = "result";

    pub fn new() -> Self {
        AbstractArrayFunction
    }

    // ---- events -------------------------------------------------------------------------

    pub fn event_index() -> Event {
        Event::new(Event::OUTPUT, one(Self::EVENT_INDEX_NAME, Schema::of_integer(Self::EVENT_INDEX_NAME)))
    }
    pub fn event_result_integer() -> Event {
        Event::new(Event::OUTPUT, one(Self::EVENT_RESULT_NAME, Schema::of_integer(Self::EVENT_RESULT_NAME)))
    }
    pub fn event_result_boolean() -> Event {
        Event::new(Event::OUTPUT, one(Self::EVENT_RESULT_NAME, Schema::of_boolean(Self::EVENT_RESULT_NAME)))
    }
    pub fn event_result_array() -> Event {
        Event::new(
            Event::OUTPUT,
            one(
                Self::EVENT_RESULT_NAME,
                Schema::of_array(Self::EVENT_RESULT_NAME, vec![Schema::of_any(Self::EVENT_RESULT_NAME)]),
            ),
        )
    }
    pub fn event_result_empty() -> Event {
        Event::new(Event::OUTPUT, Map::new())
    }
    pub fn event_result_any() -> Event {
        Event::new(Event::OUTPUT, one(Self::EVENT_RESULT_NAME, Schema::of_any(Self::EVENT_RESULT_NAME)))
    }
    pub fn event_result_object() -> Event {
        Event::new(Event::OUTPUT, one(Self::EVENT_RESULT_NAME, Schema::of_object(Self::EVENT_RESULT_NAME)))
    }

    // ---- parameters ---------------------------------------------------------------------

    pub fn parameter_int_length() -> Parameter {
        Parameter::of("length", Schema::of_integer("length").set_default_value(Value::from(-1)), false)
    }
    pub fn parameter_array_find() -> Parameter {
        Parameter::of("find", Schema::of_array("eachFind", vec![Schema::of_any("eachFind")]), false)
    }
    pub fn parameter_int_source_from() -> Parameter {
        Parameter::of(
            "srcFrom",
            Schema::of_integer("srcFrom").set_default_value(Value::from(0)).set_minimum(0.0),
            false,
        )
    }
    pub fn parameter_int_second_source_from() -> Parameter {
        Parameter::of("secondSrcFrom", Schema::of_integer("secondSrcFrom").set_default_value(Value::from(0)), false)
    }
    pub fn parameter_int_find_from() -> Parameter {
        Parameter::of("findFrom", Schema::of_integer("findFrom").set_default_value(Value::from(0)), false)
    }
    pub fn parameter_int_offset() -> Parameter {
        Parameter::of("offset", Schema::of_integer("offset").set_default_value(Value::from(0)), false)
    }
    pub fn parameter_rotate_length() -> Parameter {
        Parameter::of(
            "rotateLength",
            Schema::of_integer("rotateLength").set_default_value(Value::from(1)).set_minimum(1.0),
            false,
        )
    }
    pub fn parameter_boolean_ascending() -> Parameter {
        Parameter::of("ascending", Schema::of_boolean("ascending").set_default_value(Value::Bool(true)), false)
    }
    pub fn parameter_key_path() -> Parameter {
        Parameter::of("keyPath", Schema::of_string("keyPath").set_default_value(Value::from("")), false)
    }
    pub fn parameter_find_primitive() -> Parameter {
        Parameter::of(
            "findPrimitive",
            Schema::of(
                "findPrimitive",
                &[SchemaType::String, SchemaType::Double, SchemaType::Float, SchemaType::Integer, SchemaType::Long],
            ),
            false,
        )
    }
    pub fn parameter_array_source() -> Parameter {
        Parameter::of("source", Schema::of_array("eachSource", vec![Schema::of_any("eachSource")]), false)
    }
    pub fn parameter_array_second_source() -> Parameter {
        Parameter::of(
            "secondSource",
            Schema::of_array("eachSecondSource", vec![Schema::of_any("eachSecondSource")]),
            false,
        )
    }
    pub fn parameter_array_source_primitive() -> Parameter {
        Parameter::of(
            "source",
            Schema::of_array(
                "eachSource",
                vec![Schema::new().set_name("eachSource").set_type(TypeUtil::of(&[
                    SchemaType::String,
                    SchemaType::Null,
                    SchemaType::Integer,
                    SchemaType::Float,
                    SchemaType::Double,
                    SchemaType::Long,
                ]))],
            ),
            false,
        )
    }
    pub fn parameter_boolean_deep_copy() -> Parameter {
        Parameter::of("deepCopy", Schema::of_boolean("deepCopy").set_default_value(Value::Bool(true)), false)
    }
    pub fn parameter_any() -> Parameter {
        Parameter::of("element", Schema::of_any("element"), false)
    }
    pub fn parameter_any_element_object() -> Parameter {
        Parameter::of("elementObject", Schema::of_any("elementObject"), false)
    }
    pub fn parameter_any_var_args() -> Parameter {
        Parameter::of("element", Schema::of_any("element"), true)
    }
    pub fn parameter_array_result() -> Parameter {
        Parameter::of(
            Self::EVENT_RESULT_NAME,
            Schema::of_array("eachResult", vec![Schema::of_any("eachResult")]),
            false,
        )
    }

    /// The JS constructor: a `System.Array` signature with the given parameters and one event.
    pub fn signature(function_name: &str, parameters: Vec<Parameter>, event: Event) -> FunctionSignature {
        let param_map: Map<Parameter> =
            parameters.into_iter().map(|p| (p.get_parameter_name().to_string(), p)).collect();
        let mut events = Map::new();
        events.insert(event.get_name().to_string(), event);
        FunctionSignature::new(function_name)
            .set_namespace(Namespaces::SYSTEM_ARRAY)
            .set_parameters(param_map)
            .set_events(events)
    }
}

impl Default for AbstractArrayFunction {
    fn default() -> Self {
        Self::new()
    }
}

// ---- shared helpers (not part of the JS API) --------------------------------------------

/// `context.getArguments().get(name)`, `Value::Null` when absent.
pub(crate) fn arg(context: &FunctionExecutionParameters, name: &str) -> Value {
    context.get_arguments().and_then(|a| a.get(name)).cloned().unwrap_or(Value::Null)
}

/// An array argument. JS would throw a `TypeError` reading `.length` of a non-array.
pub(crate) fn arg_array(context: &FunctionExecutionParameters, name: &str) -> Result<Vec<Value>> {
    match arg(context, name) {
        Value::Array(a) => Ok(a),
        other => Err(KIRunError::runtime(format!(
            "Cannot read properties of {} (reading 'length')",
            js_to_string(&other)
        ))),
    }
}

/// A numeric argument as `i64` (validated integers; a missing value reads as 0).
pub(crate) fn arg_i64(context: &FunctionExecutionParameters, name: &str) -> i64 {
    arg(context, name).as_f64().map(|f| f as i64).unwrap_or(0)
}

/// `source[i]` with JS out-of-range reads (`undefined`, here `Value::Null`).
pub(crate) fn at(source: &[Value], i: i64) -> Value {
    if i < 0 {
        return Value::Null;
    }
    source.get(i as usize).cloned().unwrap_or(Value::Null)
}

/// `new FunctionOutput([EventResult.outputOf(new Map([[name, value]]))])`.
pub(crate) fn output_of(name: &str, value: Value) -> FunctionOutput {
    let mut m = Map::new();
    m.insert(name.to_string(), value);
    FunctionOutput::new(vec![EventResult::output_of(m)])
}

/// Structural equality standing in for JS `===` / SameValueZero: primitives by type and value
/// (numbers by numeric value), arrays and objects by contents (Rust values have no identity,
/// so two equal structures count as "the same object").
pub(crate) fn strict_equal(a: &Value, b: &Value) -> bool {
    match (a, b) {
        (Value::Number(x), Value::Number(y)) => x.as_f64() == y.as_f64(),
        (Value::Array(x), Value::Array(y)) => x.len() == y.len() && x.iter().zip(y).all(|(p, q)| strict_equal(p, q)),
        (Value::Object(x), Value::Object(y)) => {
            x.len() == y.len() && x.iter().all(|(k, v)| y.get(k).is_some_and(|w| strict_equal(v, w)))
        }
        _ => a == b,
    }
}

/// JS `PrimitiveUtil.compare`, where JS's `a === b` identity check on objects is replaced by
/// structural equality (the same object passed twice in JS is two equal values here).
pub(crate) fn compare(a: &Value, b: &Value) -> f64 {
    if (a.is_object() || a.is_array()) && strict_equal(a, b) {
        return 0.0;
    }
    PrimitiveUtil::compare(a, b)
}

/// JS ToPrimitive for a JSON value: arrays and objects become their string form.
fn to_primitive(v: &Value) -> Value {
    match v {
        Value::Array(_) | Value::Object(_) => Value::String(js_to_string(v)),
        _ => v.clone(),
    }
}

fn to_number(v: &Value) -> f64 {
    match v {
        Value::Null => 0.0,
        Value::Bool(b) => f64::from(u8::from(*b)),
        Value::Number(n) => n.as_f64().unwrap_or(f64::NAN),
        Value::String(s) => js_string_to_number(s),
        _ => f64::NAN,
    }
}

/// JS `a == b` (objects/arrays compared structurally among themselves, see [`strict_equal`]).
pub(crate) fn loose_equal(a: &Value, b: &Value) -> bool {
    let a_obj = a.is_object() || a.is_array();
    let b_obj = b.is_object() || b.is_array();
    if a_obj && b_obj {
        return strict_equal(a, b);
    }
    match (a, b) {
        (Value::Null, Value::Null) => true,
        (Value::Null, _) | (_, Value::Null) => false,
        _ => {
            let (pa, pb) = (to_primitive(a), to_primitive(b));
            match (&pa, &pb) {
                (Value::String(x), Value::String(y)) => x == y,
                _ => to_number(&pa) == to_number(&pb),
            }
        }
    }
}

/// JS `a < b`: two strings compare by UTF-16 code units, everything else numerically
/// (`NaN` comparisons are false).
pub(crate) fn js_less_than(a: &Value, b: &Value) -> bool {
    let (pa, pb) = (to_primitive(a), to_primitive(b));
    if let (Value::String(x), Value::String(y)) = (&pa, &pb) {
        return x.encode_utf16().lt(y.encode_utf16());
    }
    to_number(&pa) < to_number(&pb)
}

/// Reads a dotted path (`a.b`, `a[0].b`, `a["key"]`) from `store`, like
/// `new ObjectValueSetterExtractor(store, 'Data.').getValue('Data.' + path)`.
/// Missing values are `Value::Null` (JS `undefined`).
pub(crate) fn get_path_value(store: &Value, path: &str) -> Value {
    let mut parts: Vec<String> = Vec::new();
    for seg in path.split('.') {
        let mut rest = seg;
        if let Some(i) = rest.find('[') {
            if i > 0 {
                parts.push(rest[..i].to_string());
            }
            rest = &rest[i..];
            while let Some(stripped) = rest.strip_prefix('[') {
                let end = match stripped.find(']') {
                    Some(e) => e,
                    None => break,
                };
                let key = stripped[..end].trim();
                let key = key.trim_matches(|c| c == '"' || c == '\'');
                parts.push(key.to_string());
                rest = &stripped[end + 1..];
            }
        } else {
            parts.push(rest.to_string());
        }
    }

    let mut cur = store;
    for p in &parts {
        cur = match cur {
            Value::Object(m) => match m.get(p) {
                Some(v) => v,
                None => return Value::Null,
            },
            Value::Array(a) => {
                if p == "length" {
                    return Value::from(a.len());
                }
                match p.parse::<usize>().ok().and_then(|i| a.get(i)) {
                    Some(v) => v,
                    None => return Value::Null,
                }
            }
            Value::String(s) if p == "length" => return Value::from(s.encode_utf16().count()),
            _ => return Value::Null,
        };
    }
    cur.clone()
}
