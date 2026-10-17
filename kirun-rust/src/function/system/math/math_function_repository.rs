//! Port of kirun-js `function/system/math/MathFunctionRepository.ts`, with kirun-java's result
//! types (`MathOutput`) and kirun-java's `ToDegrees` / `ToRadians` (kirun-js has them swapped).

use std::sync::Arc;

use async_trait::async_trait;

use super::generic_math_function::{js_round, GenericMathFunction, MathOutput};
use super::{Add, Hypotenuse, Maximum, Minimum, Random, RandomAny};
use crate::function::Function;
use crate::json::schema::r#type::SchemaType;
use crate::json::schema::Schema;
use crate::model::Parameter;
use crate::namespaces::Namespaces;
use crate::repository::Repository;
use crate::{Map, Value};

pub struct MathFunctionRepository {
    function_objects_index: Map<Arc<dyn Function>>,
    filterable_names: Vec<String>,
}

fn generic(name: &str, f: fn(f64, Option<f64>) -> f64) -> GenericMathFunction {
    GenericMathFunction::new(name, f, 1, &[])
}

impl MathFunctionRepository {
    pub fn new() -> Self {
        use SchemaType::*;
        let functions: Vec<(&str, Arc<dyn Function>)> = vec![
            (
                "Absolute",
                Arc::new(
                    GenericMathFunction::new("Absolute", |v, _| v.abs(), 1, &[Integer, Long, Float, Double])
                        .with_output(MathOutput::SameType),
                ),
            ),
            ("ArcCosine", Arc::new(generic("ArcCosine", |v, _| v.acos()))),
            ("ArcSine", Arc::new(generic("ArcSine", |v, _| v.asin()))),
            ("ArcTangent", Arc::new(generic("ArcTangent", |v, _| v.atan()))),
            ("Ceiling", Arc::new(generic("Ceiling", |v, _| v.ceil()))),
            ("Cosine", Arc::new(generic("Cosine", |v, _| v.cos()))),
            ("HyperbolicCosine", Arc::new(generic("HyperbolicCosine", |v, _| v.cosh()))),
            ("CubeRoot", Arc::new(generic("CubeRoot", |v, _| v.cbrt()))),
            ("Exponential", Arc::new(generic("Exponential", |v, _| v.exp()))),
            ("ExponentialMinus1", Arc::new(generic("ExponentialMinus1", |v, _| v.exp_m1()))),
            ("Floor", Arc::new(generic("Floor", |v, _| v.floor()))),
            ("LogNatural", Arc::new(generic("LogNatural", |v, _| v.ln()))),
            ("Log10", Arc::new(generic("Log10", |v, _| v.log10()))),
            (
                "Round",
                Arc::new(GenericMathFunction::new("Round", |v, _| js_round(v), 1, &[Integer, Long]).with_output(MathOutput::Round)),
            ),
            ("Sine", Arc::new(generic("Sine", |v, _| v.sin()))),
            ("HyperbolicSine", Arc::new(generic("HyperbolicSine", |v, _| v.sinh()))),
            ("Tangent", Arc::new(generic("Tangent", |v, _| v.tan()))),
            ("HyperbolicTangent", Arc::new(generic("HyperbolicTangent", |v, _| v.tanh()))),
            // kirun-java `Math.toDegrees` / `Math.toRadians` (kirun-js has the two swapped).
            ("ToDegrees", Arc::new(generic("ToDegrees", |v, _| v * 57.295_779_513_082_32))),
            ("ToRadians", Arc::new(generic("ToRadians", |v, _| v * 0.017_453_292_519_943_295))),
            ("SquareRoot", Arc::new(generic("SquareRoot", |v, _| v.sqrt()))),
            (
                "ArcTangent2",
                Arc::new(GenericMathFunction::new("ArcTangent2", |v1, v2| v1.atan2(v2.unwrap_or(f64::NAN)), 2, &[])),
            ),
            ("Power", Arc::new(GenericMathFunction::new("Power", |v1, v2| v1.powf(v2.unwrap_or(f64::NAN)), 2, &[]))),
            ("Add", Arc::new(Add::new())),
            ("Hypotenuse", Arc::new(Hypotenuse::new())),
            ("Maximum", Arc::new(Maximum::new())),
            ("Minimum", Arc::new(Minimum::new())),
            ("Random", Arc::new(Random::new())),
            (
                "RandomFloat",
                Arc::new(RandomAny::new(
                    "RandomFloat",
                    Parameter::of(
                        RandomAny::MIN_VALUE,
                        Schema::of_float(RandomAny::MIN_VALUE).set_default_value(Value::from(0)),
                        false,
                    ),
                    Parameter::of(
                        RandomAny::MAX_VALUE,
                        Schema::of_float(RandomAny::MAX_VALUE).set_default_value(Value::from(2147483647)),
                        false,
                    ),
                    Schema::of_float(RandomAny::VALUE),
                    |min, max| rand::random::<f64>() * (max - min) + min,
                )),
            ),
            (
                "RandomInt",
                Arc::new(RandomAny::new(
                    "RandomInt",
                    Parameter::of(
                        RandomAny::MIN_VALUE,
                        Schema::of_integer(RandomAny::MIN_VALUE).set_default_value(Value::from(0)),
                        false,
                    ),
                    Parameter::of(
                        RandomAny::MAX_VALUE,
                        Schema::of_integer(RandomAny::MAX_VALUE).set_default_value(Value::from(2147483647)),
                        false,
                    ),
                    Schema::of_integer(RandomAny::VALUE),
                    |min, max| js_round(rand::random::<f64>() * (max - min) + min),
                )),
            ),
            (
                "RandomLong",
                Arc::new(RandomAny::new(
                    "RandomLong",
                    Parameter::of(
                        RandomAny::MIN_VALUE,
                        Schema::of_long(RandomAny::MIN_VALUE).set_default_value(Value::from(0)),
                        false,
                    ),
                    Parameter::of(
                        RandomAny::MAX_VALUE,
                        // JS Number.MAX_SAFE_INTEGER
                        Schema::of_long(RandomAny::MAX_VALUE).set_default_value(Value::from(9007199254740991i64)),
                        false,
                    ),
                    Schema::of_long(RandomAny::VALUE),
                    |min, max| js_round(rand::random::<f64>() * (max - min) + min),
                )),
            ),
            (
                "RandomDouble",
                Arc::new(RandomAny::new(
                    "RandomDouble",
                    Parameter::of(
                        RandomAny::MIN_VALUE,
                        Schema::of_double(RandomAny::MIN_VALUE).set_default_value(Value::from(0)),
                        false,
                    ),
                    Parameter::of(
                        RandomAny::MAX_VALUE,
                        // JS Number.MAX_VALUE
                        Schema::of_double(RandomAny::MAX_VALUE).set_default_value(Value::from(f64::MAX)),
                        false,
                    ),
                    Schema::of_double(RandomAny::VALUE),
                    |min, max| rand::random::<f64>() * (max - min) + min,
                )),
            ),
        ];

        let function_objects_index: Map<Arc<dyn Function>> =
            functions.into_iter().map(|(k, v)| (k.to_string(), v)).collect();
        let filterable_names =
            function_objects_index.values().map(|f| f.get_signature().get_full_name()).collect();

        Self { function_objects_index, filterable_names }
    }
}

impl Default for MathFunctionRepository {
    fn default() -> Self {
        Self::new()
    }
}

#[async_trait]
impl Repository<dyn Function> for MathFunctionRepository {
    async fn find(&self, namespace: &str, name: &str) -> Option<Arc<dyn Function>> {
        if namespace != Namespaces::MATH {
            return None;
        }
        self.function_objects_index.get(name).cloned()
    }

    async fn filter(&self, name: &str) -> Vec<String> {
        let name = name.to_lowercase();
        self.filterable_names.iter().filter(|e| e.to_lowercase().contains(&name)).cloned().collect()
    }
}
