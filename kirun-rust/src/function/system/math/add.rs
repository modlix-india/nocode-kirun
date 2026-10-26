//! Port of kirun-js `function/system/math/Add.ts`, adding as kirun-java's `Add` does: the sum
//! keeps the widest Java type of the values (`int` + `int` is an `int`, a `long` when it does
//! not fit, a `double` past the `long` range; any `double` makes a `double`), and no values
//! raise no event.

use async_trait::async_trait;

use crate::function::AbstractFunction;
use crate::json::schema::Schema;
use crate::model::{Event, EventResult, FunctionOutput, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::runtime::expression::operators::java_semantics::{arith_num, find_number, JNum};
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

/// kirun-java `Add`'s reduce step, without wrapping: whole sums widen to a `long`, then to a
/// `double` past the `long` range (kirun-js numbers never wrap).
fn java_add(a: JNum, b: JNum) -> JNum {
    arith_num(a, b, i64::checked_add, |x, y| x + y, |x, y| x + y)
}
