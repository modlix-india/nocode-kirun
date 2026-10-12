//! Port of kirun-js `__tests__/engine/runtime/expression/**`.

#[path = "../common/mod.rs"]
mod common;

mod expression_array_string_indexing;
mod expression_double_equals_chip_label_test;
mod expression_equality_test;
mod expression_evaluation_test;
mod expression_evaluator_bracket_notation_test;
mod expression_evaluator_length_subtraction_bug_test;
mod expression_evaluator_length_subtraction_test;
mod expression_evaluator_string_literal_test;
mod expression_evaluator_ternary_operator_test;
mod expression_parsing_test;
mod expression_test;
mod logical_not_operator_test;
mod object_value_setter_extractor_test;
mod output_map_token_value_extractor_test;
mod token_value_extractor_test;

use std::sync::Arc;

use kirun::runtime::expression::tokenextractor::{TokenValueExtractor, ValuesMap};
use kirun::runtime::function_execution_parameters::StepsMap;
use kirun::{Map, Value};

/// JS `MapUtil.of(a.getPrefix(), a, b.getPrefix(), b, ...)` / `new Map([[x.getPrefix(), x], ...])`.
pub fn values_map(extractors: Vec<Arc<dyn TokenValueExtractor>>) -> ValuesMap {
    let mut m = ValuesMap::new();
    for e in extractors {
        m.insert(e.get_prefix().to_string(), e);
    }
    m
}

/// JS truthiness (`toBeTruthy` / `toBeFalsy`). `undefined` and `null` are both `Value::Null`.
pub fn is_truthy(v: &Value) -> bool {
    match v {
        Value::Null => false,
        Value::Bool(b) => *b,
        Value::Number(n) => n.as_f64().is_some_and(|f| f != 0.0 && !f.is_nan()),
        Value::String(s) => !s.is_empty(),
        Value::Array(_) | Value::Object(_) => true,
    }
}

/// Builds a `StepsMap` from `(step, event, results)` triples
/// (JS `new Map([[step, new Map([[event, new Map(results)]])]])`).
pub fn steps_map(entries: Vec<(&str, &str, Map<Value>)>) -> StepsMap {
    let mut steps = StepsMap::new();
    for (step, event, results) in entries {
        steps.entry(step.to_string()).or_default().insert(event.to_string(), results);
    }
    steps
}
