//! Port of kirun-js `function/system/GenerateEvent.ts`; runs as kirun-java's `GenerateEvent`.

use async_trait::async_trait;

use crate::function::AbstractFunction;
use crate::json::schema::Schema;
use crate::model::{Event, EventResult, FunctionOutput, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::runtime::expression::ExpressionEvaluator;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::json::schema::validator::schema_validator::java_json;
use crate::{KIRunError, Map, Result, Value};

const VALUE: &str = "value";
const EVENT_NAME: &str = "eventName";
const RESULTS: &str = "results";

pub struct GenerateEvent {
    signature: FunctionSignature,
}

impl GenerateEvent {
    pub fn new() -> Self {
        let mut props = Map::new();
        props.insert("name".to_string(), Schema::of_string("name"));
        props.insert(VALUE.to_string(), Parameter::expression().clone());
        Self {
            signature: FunctionSignature::new("GenerateEvent")
                .set_namespace(Namespaces::SYSTEM)
                .set_parameters(Map::from([
                    Parameter::of_entry(
                        EVENT_NAME,
                        Schema::of_string(EVENT_NAME).set_default_value(Value::from("output")),
                        false,
                    ),
                    Parameter::of_entry(RESULTS, Schema::of_object(RESULTS).set_properties(props), true),
                ]))
                .set_events(Map::from([Event::output_event_map_entry(Map::new())])),
        }
    }
}

impl Default for GenerateEvent {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for GenerateEvent {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    /// kirun-java `GenerateEvent.internalExecute`. Its failures leave the function as Java threw
    /// them (kirun-java wraps nothing a function throws while it runs), so they are
    /// [`KIRunError::Uncaught`] with the Java message: Gson's `NullPointerException` for a
    /// result without `value`, `isExpression` or `name`, Reactor's "t2" for a result value that
    /// is Java `null` (a missing `value`, or an expression that reads nothing), and
    /// `Collectors.toMap`'s "Duplicate key" for two results with one name. kirun-js reads all
    /// of these as `undefined` and raises the event.
    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let args = context.get_arguments().cloned().unwrap_or_default();

        // Always there: the parameter defaults to `output`.
        let event_name = as_string(args.get(EVENT_NAME).unwrap_or(&Value::Null))?;

        let mut map: Map<Value> = Map::new();
        if let Some(Value::Array(results)) = args.get(RESULTS) {
            for e in results {
                let Some(je) = e.get(VALUE) else {
                    return Err(npe("isJsonObject()", "\"je\""));
                };
                let Value::Object(jo) = je else {
                    return Err(KIRunError::uncaught("Expect a value object"));
                };

                let v = jo.get(VALUE);
                let is_expression = match jo.get("isExpression") {
                    Some(b) => as_boolean(b)?,
                    None => return Err(npe("getAsBoolean()", RETURN_OF_GET)),
                };

                let value: Option<Value> = if is_expression {
                    let expression = match v {
                        Some(v) => as_string(v)?,
                        None => return Err(npe("getAsString()", "\"v\"")),
                    };
                    ExpressionEvaluator::new(&expression).evaluate_or_missing(context.get_values_map())?
                } else {
                    v.cloned()
                };

                let name = match e.get("name") {
                    Some(n) => as_string(n)?,
                    None => return Err(npe("getAsString()", RETURN_OF_GET)),
                };
                let Some(value) = value else {
                    return Err(KIRunError::uncaught("t2"));
                };
                if let Some(existing) = map.get(&name) {
                    return Err(KIRunError::uncaught(format!(
                        "Duplicate key {name} (attempted merging values {} and {})",
                        java_json(existing),
                        java_json(&value)
                    )));
                }
                map.insert(name, value);
            }
        }

        if let Some(events) = context.get_events() {
            events.write().entry(event_name).or_default().push(map);
        }

        Ok(FunctionOutput::new(vec![EventResult::output_of(Map::new())]))
    }
}

/// The receiver Java names in a helpful `NullPointerException` for `jsonObject.get(key).x()`.
const RETURN_OF_GET: &str = "the return value of \"com.google.gson.JsonObject.get(String)\"";

/// The JVM's helpful `NullPointerException` message for calling `method` on a null `receiver`.
fn npe(method: &str, receiver: &str) -> KIRunError {
    KIRunError::uncaught(format!(
        "Cannot invoke \"com.google.gson.JsonElement.{method}\" because {receiver} is null"
    ))
}

/// Gson `JsonElement.getAsString()`.
fn as_string(v: &Value) -> Result<String> {
    match v {
        Value::String(s) => Ok(s.clone()),
        Value::Number(n) => Ok(n.to_string()),
        Value::Bool(b) => Ok(b.to_string()),
        Value::Null => Err(KIRunError::uncaught("JsonNull")),
        Value::Object(_) => Err(KIRunError::uncaught("JsonObject")),
        Value::Array(a) if a.len() == 1 => as_string(&a[0]),
        Value::Array(a) => Err(KIRunError::uncaught(format!("Array must have size 1, but has size {}", a.len()))),
    }
}

/// Gson `JsonElement.getAsBoolean()`: a string or number reads through `Boolean.parseBoolean`.
fn as_boolean(v: &Value) -> Result<bool> {
    match v {
        Value::Bool(b) => Ok(*b),
        Value::String(s) => Ok(s.eq_ignore_ascii_case("true")),
        Value::Number(_) => Ok(false),
        Value::Null => Err(KIRunError::uncaught("JsonNull")),
        Value::Object(_) => Err(KIRunError::uncaught("JsonObject")),
        Value::Array(a) if a.len() == 1 => as_boolean(&a[0]),
        Value::Array(a) => Err(KIRunError::uncaught(format!("Array must have size 1, but has size {}", a.len()))),
    }
}
