//! Port of covnertor/NullConvertorTest.ts.

use kirun::json::schema::convertor::{ConversionMode, NullConvertor};
use kirun::json::schema::Schema;
use kirun::{Result, Value};
use serde_json::json;

use crate::assert_conversion_error;
use crate::common::assert_json_eq;

fn convert_element(mode: ConversionMode, element: Value) -> Result<Value> {
    NullConvertor::convert(&[], &Schema::new(), mode, &element)
}

#[test]
fn null_convert_null_element_strict_mode() {
    let result = convert_element(ConversionMode::Strict, Value::Null).unwrap();
    assert_json_eq!(result, Value::Null);
}

#[test]
fn null_convert_null_json_element_strict_mode() {
    let result = convert_element(ConversionMode::Strict, Value::Null).unwrap();
    assert_json_eq!(result, Value::Null);
}

#[test]
fn null_convert_string_null_strict_mode() {
    let element = json!("null");
    let result = convert_element(ConversionMode::Strict, element).unwrap();
    assert_json_eq!(result, Value::Null);
}

#[test]
fn null_convert_invalid_element_strict_mode() {
    let element = json!("invalid");
    assert_conversion_error(convert_element(ConversionMode::Strict, element));
}

#[test]
fn null_convert_invalid_element_lenient_mode() {
    let element = json!("invalid");
    let result = convert_element(ConversionMode::Lenient, element).unwrap();
    assert_json_eq!(result, Value::Null);
}

#[test]
fn null_convert_use_default_mode() {
    let element = json!("invalid");
    let result = convert_element(ConversionMode::UseDefault, element).unwrap();
    assert_json_eq!(result, Value::Null);
}

#[test]
fn null_convert_skip_mode() {
    let element = json!("invalid");
    let result = convert_element(ConversionMode::Skip, element.clone()).unwrap();
    assert_json_eq!(result, element);
}
