//! Port of kirun-js `function/system/math/GenericMathFunction.ts`.

use async_trait::async_trait;

use crate::function::AbstractFunction;
use crate::json::schema::r#type::SchemaType;
use crate::json::schema::Schema;
use crate::model::{Event, EventResult, FunctionOutput, FunctionSignature, Parameter};
use crate::namespaces::Namespaces;
use crate::runtime::function_execution_parameters::FunctionExecutionParameters;
use crate::util::primitive::number_value;
use crate::util::primitive::primitive_util::PrimitiveUtil;
use crate::{Map, Result, Value};

const VALUE: &str = "value";
const VALUE1: &str = "value1";
const VALUE2: &str = "value2";

/// JS `mathFunction: (v1: number, v2?: number) => number`.
pub type MathFunction = fn(f64, Option<f64>) -> f64;

pub struct GenericMathFunction {
    signature: FunctionSignature,
    parameters_number: usize,
    math_function: MathFunction,
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
        }
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
        let v1 = number_arg(context, if self.parameters_number == 1 { VALUE } else { VALUE1 })?;
        let v2 = if self.parameters_number == 2 { Some(number_arg(context, VALUE2)?) } else { None };

        Ok(FunctionOutput::new(vec![EventResult::output_of(Map::from([(
            VALUE.to_string(),
            number_value((self.math_function)(v1, v2)),
        )]))]))
    }
}

/// JS `Math.round`: halves round towards +Infinity.
pub fn js_round(v: f64) -> f64 {
    if !v.is_finite() {
        return v;
    }
    (v + 0.5).floor()
}
