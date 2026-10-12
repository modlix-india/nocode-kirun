//! Port of kirun-js `function/system/json/JSONParse.ts`.

use async_trait::async_trait;

use crate::function::AbstractFunction;
use crate::json::schema::Schema;
use crate::model::{Event, EventResult, FunctionOutput, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::util::primitive::{js_truthy, number_value};
use crate::{Map, Result, Value};

const VALUE: &str = "value";
const ERROR: &str = "error";
const ERROR_MESSAGE: &str = "errorMessage";
const SOURCE: &str = "source";

pub struct JSONParse {
    signature: FunctionSignature,
}

impl JSONParse {
    pub fn new() -> Self {
        Self {
            signature: FunctionSignature::new("JSONParse")
                .set_namespace(Namespaces::SYSTEM_JSON)
                .set_parameters(Map::from([Parameter::of_entry(SOURCE, Schema::of_string(SOURCE), false)]))
                .set_events(Map::from([
                    Event::event_map_entry(
                        ERROR,
                        Map::from([(ERROR_MESSAGE.to_string(), Schema::of_string(ERROR_MESSAGE))]),
                    ),
                    Event::output_event_map_entry(Map::from([(VALUE.to_string(), Schema::of_any(VALUE))])),
                ])),
        }
    }
}

impl Default for JSONParse {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl AbstractFunction for JSONParse {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let source = context.get_arguments().and_then(|a| a.get(SOURCE)).cloned().unwrap_or(Value::Null);

        // JS `source ? JSON.parse(source) : null`
        if !js_truthy(&source) {
            return Ok(FunctionOutput::new(vec![EventResult::output_of(Map::from([(VALUE.to_string(), Value::Null)]))]));
        }
        let text = source.as_str().map(str::to_string).unwrap_or_else(|| source.to_string());

        match serde_json::from_str::<Value>(&text) {
            Ok(v) => Ok(FunctionOutput::new(vec![EventResult::output_of(Map::from([(
                VALUE.to_string(),
                normalize_numbers(v),
            )]))])),
            Err(err) => Ok(FunctionOutput::new(vec![
                EventResult::of(
                    ERROR,
                    Map::from([(ERROR_MESSAGE.to_string(), Value::String(v8_parse_error(&text, &err)))]),
                ),
                EventResult::output_of(Map::from([(VALUE.to_string(), Value::Null)])),
            ])),
        }
    }
}

/// JS numbers have no int/float split: `1.0` parses to `1`.
fn normalize_numbers(v: Value) -> Value {
    match v {
        Value::Number(n) if n.is_f64() => number_value(n.as_f64().unwrap_or(f64::NAN)),
        Value::Array(a) => Value::Array(a.into_iter().map(normalize_numbers).collect()),
        Value::Object(o) => Value::Object(o.into_iter().map(|(k, v)| (k, normalize_numbers(v))).collect()),
        other => other,
    }
}

/// Approximates V8's `JSON.parse` messages, which callers see as `errorMessage`.
fn v8_parse_error(text: &str, err: &serde_json::Error) -> String {
    if err.is_eof() {
        return "Unexpected end of JSON input".to_string();
    }

    // serde reports 1-based line/column; turn that into a char offset.
    let mut offset = 0usize;
    for (i, line) in text.split('\n').enumerate() {
        if i + 1 == err.line() {
            offset += err.column().saturating_sub(1);
            break;
        }
        offset += line.chars().count() + 1;
    }
    let chars: Vec<char> = text.chars().collect();
    let offset = offset.min(chars.len().saturating_sub(1));

    if err.to_string().starts_with("trailing characters") {
        return format!("Unexpected non-whitespace character after JSON at position {offset}");
    }

    let token = chars.get(offset).copied().unwrap_or(' ');
    // V8 quotes short sources whole and elides long ones around the bad position.
    const CONTEXT: usize = 10;
    let snippet = if chars.len() <= CONTEXT * 2 {
        format!("\"{text}\"")
    } else {
        let start = offset.saturating_sub(CONTEXT);
        let end = (offset + CONTEXT).min(chars.len());
        let mid: String = chars[start..end].iter().collect();
        format!(
            "{}\"{mid}\"{}",
            if start > 0 { "..." } else { "" },
            if end < chars.len() { "..." } else { "" }
        )
    };
    format!("Unexpected token '{token}', {snippet} is not valid JSON")
}
