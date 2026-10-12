//! Port of covnertor/StringConvertorTest.ts.

use kirun::json::schema::convertor::{ConversionMode, StringConvertor};
use kirun::json::schema::Schema;
use kirun::{Result, Value};
use serde_json::json;

use crate::assert_conversion_error;
use crate::common::assert_json_eq;

fn convert_element(schema: &Schema, mode: ConversionMode, element: Value) -> Result<Value> {
    StringConvertor::convert(&[], schema, mode, &element)
}

#[test]
fn convert_valid_string_strict_mode() {
    let schema = Schema::new();
    let element = json!("test string");
    let result = convert_element(&schema, ConversionMode::Strict, element).unwrap();
    assert_json_eq!(result, json!("test string"));
}

#[test]
fn convert_null_element_strict_mode() {
    let schema = Schema::new();
    assert_conversion_error(convert_element(&schema, ConversionMode::Strict, Value::Null));
}

#[test]
fn convert_null_object_strict_mode() {
    let schema = Schema::new();
    assert_conversion_error(convert_element(&schema, ConversionMode::Strict, Value::Null));
}

#[test]
fn convert_empty_element_strict_mode() {
    let schema = Schema::new();
    let element = json!("");
    let result = convert_element(&schema, ConversionMode::Strict, element).unwrap();
    assert_json_eq!(result, json!(""));
}

#[test]
fn convert_non_primitive_element_strict_mode() {
    let schema = Schema::new();
    let element = json!(123);
    let result = convert_element(&schema, ConversionMode::Strict, element).unwrap();
    assert_json_eq!(result, json!("123"));
}

#[test]
fn convert_use_default_mode() {
    let schema = Schema::new().set_default_value(json!("default value"));
    let result = convert_element(&schema, ConversionMode::UseDefault, Value::Null).unwrap();
    assert_json_eq!(result, json!("default value"));
}

#[test]
fn convert_skip_mode() {
    let schema = Schema::new();
    let result = convert_element(&schema, ConversionMode::Skip, Value::Null).unwrap();
    assert_json_eq!(result, Value::Null);
}
