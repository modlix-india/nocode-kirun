//! Port of covnertor/BooleanConvertorTest.ts.

use kirun::json::schema::convertor::{BooleanConvertor, ConversionMode};
use kirun::json::schema::Schema;
use kirun::{Result, Value};
use serde_json::json;

use crate::assert_conversion_error;
use crate::common::assert_json_eq;

fn convert_element(element: Value, mode: ConversionMode) -> Result<Value> {
    BooleanConvertor::convert(&[], &Schema::new(), mode, &element)
}

#[test]
fn boolean_convert_null_element_strict_mode() {
    assert_conversion_error(convert_element(Value::Null, ConversionMode::Strict));
}

#[test]
fn boolean_convert_true_string_strict_mode() {
    let result = convert_element(json!("true"), ConversionMode::Strict).unwrap();
    assert_json_eq!(result, json!(true));
}

#[test]
fn boolean_convert_false_string_strict_mode() {
    let result = convert_element(json!("false"), ConversionMode::Strict).unwrap();
    assert_json_eq!(result, json!(false));
}

#[test]
fn boolean_convert_one_as_number_strict_mode() {
    let result = convert_element(json!(1), ConversionMode::Strict).unwrap();
    assert_json_eq!(result, json!(true));
}

#[test]
fn boolean_convert_zero_as_number_strict_mode() {
    let result = convert_element(json!(0), ConversionMode::Strict).unwrap();
    assert_json_eq!(result, json!(false));
}

#[test]
fn boolean_convert_invalid_string_strict_mode() {
    let invalid = json!("invalid");
    assert_conversion_error(convert_element(invalid, ConversionMode::Strict));
}

#[test]
fn boolean_convert_invalid_string_lenient_mode() {
    let result = convert_element(json!("invalid"), ConversionMode::Lenient).unwrap();
    assert_json_eq!(result, Value::Null); // Assuming lenient mode returns null for invalid input
}

#[test]
fn boolean_convert_use_default_mode() {
    let default_value = json!(true);
    let schema = Schema::new().set_default_value(default_value.clone());
    let result = BooleanConvertor::convert(&[], &schema, ConversionMode::UseDefault, &json!("invalid")).unwrap();
    assert_json_eq!(result, default_value);
}

#[test]
fn boolean_convert_skip_mode() {
    let element = json!("invalid");
    let result = convert_element(element.clone(), ConversionMode::Skip).unwrap();
    assert_json_eq!(result, element);
}
