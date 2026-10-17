//! Port of kirun-js `function/system/math/Add.ts`, adding as kirun-java's `Add` does: the sum
//! keeps the widest Java type of the values (`int` + `int` is an `int`, wrapping on overflow;
//! any `double` makes a `double`), and no values raise no event.

use async_trait::async_trait;

use crate::function::AbstractFunction;
use crate::json::schema::Schema;
use crate::model::{Event, EventResult, FunctionOutput, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::runtime::expression::operators::java_semantics::{find_number, JNum};
use crate::{Map, Result, Value};

const VALUE: &str = "value";

pub struct Add {
    signature: FunctionSignature,
}

impl Add {
    pub fn new() -> Self {
        Self {
            signature: FunctionSignature::new("Add")
                .set_namespace(Namespaces::MATH)
                .set_parameters(Map::from([(
                    VALUE.to_string(),
                    Parameter::new(VALUE, Schema::of_number(VALUE)).set_variable_argument(true),
                )]))
                .set_events(Map::from([Event::output_event_map_entry(Map::from([(
                    VALUE.to_string(),
                    Schema::of_number(VALUE),
                )]))])),
        }
    }
}

impl Default for Add {
    fn default() -> Self {
        Self::new()
    }
}

/// The variable-argument `value` array as JS numbers.
pub(crate) fn number_args(context: &FunctionExecutionParameters, name: &str) -> Vec<Value> {
    match context.get_arguments().and_then(|a| a.get(name)) {
        Some(Value::Array(a)) => a.clone(),
        _ => vec![],
    }
}

#[async_trait]
impl AbstractFunction for Add {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let mut sum: Option<JNum> = None;
        for n in number_args(context, VALUE) {
            let n = find_number(&n)?;
            sum = Some(match sum {
                None => n,
                Some(a) => java_add(a, n),
            });
        }

        // kirun-java reduces an empty Flux: nothing to output, so no event at all.
        Ok(FunctionOutput::new(
            sum.map(|s| EventResult::output_of(Map::from([(VALUE.to_string(), s.to_value())]))).into_iter().collect(),
        ))
    }
}

/// kirun-java `Add`'s reduce step.
fn java_add(a: JNum, b: JNum) -> JNum {
    let as_f64 = |n: JNum| match n {
        JNum::Int(i) => i as f64,
        JNum::Long(l) => l as f64,
        JNum::Float(f) => f as f64,
        JNum::Double(d) => d,
    };
    let as_f32 = |n: JNum| match n {
        JNum::Int(i) => i as f32,
        JNum::Long(l) => l as f32,
        JNum::Float(f) => f,
        JNum::Double(d) => d as f32,
    };
    let as_i64 = |n: JNum| match n {
        JNum::Int(i) => i as i64,
        JNum::Long(l) => l,
        JNum::Float(f) => f as i64,
        JNum::Double(d) => d as i64,
    };
    match (a, b) {
        (JNum::Double(_), _) | (_, JNum::Double(_)) => JNum::Double(as_f64(a) + as_f64(b)),
        (JNum::Float(_), _) | (_, JNum::Float(_)) => JNum::Float(as_f32(a) + as_f32(b)),
        (JNum::Long(_), _) | (_, JNum::Long(_)) => JNum::Long(as_i64(a).wrapping_add(as_i64(b))),
        (JNum::Int(x), JNum::Int(y)) => JNum::Int(x.wrapping_add(y)),
    }
}
