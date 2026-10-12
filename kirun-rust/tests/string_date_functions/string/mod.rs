//! Ports of kirun-js `__tests__/engine/function/system/string/*`.

mod concatenate_test;
mod delete_for_given_length_test;
mod insert_at_given_position_test;
mod matches_test;
mod post_pad_test;
mod pre_pad_test;
mod region_matches_test;
mod reverse_test;
mod split_test;
mod string_function_repo_test2;
mod string_function_repo_test3;
mod string_function_repo_test4;
mod string_function_repository_test;
mod to_string_test;
mod trim_to_test;

use kirun::Value;

/// JS truthiness, for `toBeTruthy()` / `toBeFalsy()`.
pub fn truthy(v: &Value) -> bool {
    match v {
        Value::Null => false,
        Value::Bool(b) => *b,
        Value::Number(n) => n.as_f64().is_some_and(|f| f != 0.0 && !f.is_nan()),
        Value::String(s) => !s.is_empty(),
        Value::Array(_) | Value::Object(_) => true,
    }
}
