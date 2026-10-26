//! Port of kirun-js `function/system/math/GenericMathFunction.ts`, answering as kirun-java
//! (`AbstractUnaryFunction` / `AbstractBinaryMathFunction`) does: stored server functions run on
//! kirun-java, which keeps a number's Java type in the result.

use async_trait::async_trait;

use crate::function::AbstractFunction;
use crate::json::schema::r#type::SchemaType;
use crate::json::schema::Schema;
use crate::model::{Event, EventResult, FunctionOutput, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::runtime::expression::operators::java_semantics::{number_kind, whole, JNum};
use crate::util::primitive::primitive_util::PrimitiveUtil;
use crate::{Map, Result, Value};

const VALUE: &str = "value";
const VALUE1: &str = "value1";
const VALUE2: &str = "value2";

/// JS `mathFunction: (v1: number, v2?: number) => number`.
pub type MathFunction = fn(f64, Option<f64>) -> f64;

/// The Java type of the result.
#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum MathOutput {
    /// kirun-java `ofEntryAnyNumberDoubleOutput` and `AbstractBinaryMathFunction.ofEntryDouble`:
    /// always a `double` (`Floor(5)` is `5.0`).
    Double,
    /// kirun-java `Absolute`: the argument's own type (`int` stays `int`), except that the
    /// smallest `int` or `long` widens instead of staying negative.
    SameType,
    /// kirun-java `Round`: `Math.round(float)` (an `int`) for an `int`, `Math.round(double)` (a
    /// `long`) for a `long` or `double`; halves go up (`-2.5` is `-2`).
    Round,
}

pub struct GenericMathFunction {
    signature: FunctionSignature,
    parameters_number: usize,
    math_function: MathFunction,
    output: MathOutput,
}

impl GenericMathFunction {
    /// JS `new GenericMathFunction(name, fn, parametersNumber = 1, ...returnType)`;
    /// an empty `return_type` means `[DOUBLE]`.
    pub fn new(
        function_name: &str,
        math_function: MathFunction,
        parameters_number: usize,
        return_type: &[SchemaType],
    ) -> Self {
        let return_type: &[SchemaType] = if return_type.is_empty() { &[SchemaType::Double] } else { return_type };
        let parameters = if parameters_number == 2 {
            Map::from([
                (VALUE1.to_string(), Parameter::new(VALUE1, Schema::of_number(VALUE1))),
                (VALUE2.to_string(), Parameter::new(VALUE2, Schema::of_number(VALUE2))),
            ])
        } else {
            Map::from([(VALUE.to_string(), Parameter::new(VALUE, Schema::of_number(VALUE)))])
        };
        Self {
            signature: FunctionSignature::new(function_name)
                .set_namespace(Namespaces::MATH)
                .set_parameters(parameters)
                .set_events(Map::from([Event::output_event_map_entry(Map::from([(
                    VALUE.to_string(),
                    Schema::of(VALUE, return_type),
                )]))])),
            parameters_number,
            math_function,
            output: MathOutput::Double,
        }
    }

    /// The Java type of the result (a `double` unless set).
    pub fn with_output(mut self, output: MathOutput) -> Self {
        self.output = output;
        self
    }
}

fn number_arg(context: &FunctionExecutionParameters, name: &str) -> Result<f64> {
    let v = context.get_arguments().and_then(|a| a.get(name)).cloned().unwrap_or(Value::Null);
    let (_, v) = PrimitiveUtil::find_primitive_number_type(&v)?;
    Ok(v.as_f64().unwrap_or(f64::NAN))
}

#[async_trait]
impl AbstractFunction for GenericMathFunction {
    fn get_signature(&self) -> &FunctionSignature {
        &self.signature
    }

    async fn internal_execute(&self, context: &mut FunctionExecutionParameters) -> Result<FunctionOutput> {
        let first = if self.parameters_number == 1 { VALUE } else { VALUE1 };
        let value = match self.output {
            MathOutput::Double => {
                let v1 = number_arg(context, first)?;
                let v2 = if self.parameters_number == 2 { Some(number_arg(context, VALUE2)?) } else { None };
                JNum::Double((self.math_function)(v1, v2)).to_value()
            }
            MathOutput::SameType => match java_number_arg(context, first)? {
                // `abs` of the smallest `int` is a `long`, of the smallest `long` a `double`.
                JNum::Int(i) => whole((i as i64).abs()),
                JNum::Long(l) => l.checked_abs().map_or(JNum::Double((l as f64).abs()), whole),
                JNum::Float(f) => JNum::Float(f.abs()),
                JNum::Double(d) => JNum::Double(d.abs()),
            }
            .to_value(),
            MathOutput::Round => match java_number_arg(context, first)? {
                // A whole number is already round (through a `float` it lost digits past 2^24).
                JNum::Int(i) => JNum::Int(i),
                JNum::Float(f) => JNum::Int(java_round_float(f)),
                JNum::Long(l) => JNum::Long(java_round(l as f64)),
                JNum::Double(d) => JNum::Long(java_round(d)),
            }
            .to_value(),
        };

        Ok(FunctionOutput::new(vec![EventResult::output_of(Map::from([(VALUE.to_string(), value)]))]))
    }
}

/// The argument as kirun-java's `PrimitiveUtil.findPrimitiveNumberType` types it.
fn java_number_arg(context: &FunctionExecutionParameters, name: &str) -> Result<JNum> {
    let v = context.get_arguments().and_then(|a| a.get(name)).cloned().unwrap_or(Value::Null);
    PrimitiveUtil::find_primitive_number_type(&v)?;
    match &v {
        Value::Number(n) => Ok(number_kind(n)),
        _ => Ok(JNum::Double(f64::NAN)),
    }
}

/// Java `Math.round(double)`: the closest `long`, halves towards positive infinity; `NaN` is
/// `0`, values beyond the range are `Long.MIN_VALUE` / `Long.MAX_VALUE`.
pub fn java_round(v: f64) -> i64 {
    let floor = v.floor();
    // `v - floor` is exact, so a half is seen as a half (`0.49999999999999994` stays `0`).
    let r = if v - floor >= 0.5 { floor + 1.0 } else { floor };
    r as i64
}

/// Java `Math.round(float)`: as [`java_round`], to an `int`.
pub fn java_round_float(v: f32) -> i32 {
    let floor = v.floor();
    let r = if v - floor >= 0.5 { floor + 1.0 } else { floor };
    r as i32
}

/// JS `Math.round`: halves round towards +Infinity.
pub fn js_round(v: f64) -> f64 {
    if !v.is_finite() {
        return v;
    }
    (v + 0.5).floor()
}
