//! Port of covnertor/NumberConvertorTest.ts.

use kirun::json::schema::convertor::{ConversionMode, NumberConvertor};
use kirun::json::schema::r#type::SchemaType;
use kirun::json::schema::Schema;
use kirun::{Result, Value};
use serde_json::json;

use crate::assert_conversion_error;
use crate::common::assert_json_eq;

fn convert_element(schema_type: SchemaType, mode: ConversionMode, element: Value) -> Result<Value> {
    NumberConvertor::convert(&[], schema_type, &Schema::new(), mode, &element)
}

fn convert_element_with_schema(
    schema_type: SchemaType,
    schema: &Schema,
    mode: ConversionMode,
    element: Value,
) -> Result<Value> {
    NumberConvertor::convert(&[], schema_type, schema, mode, &element)
}

#[test]
fn convert_null_element_strict_mode() {
    assert_conversion_error(convert_element(SchemaType::Integer, ConversionMode::Strict, Value::Null));
}

#[test]
fn convert_non_number_element_strict_mode() {
    let non_number = json!("not a number");
    assert_conversion_error(convert_element(SchemaType::Integer, ConversionMode::Strict, non_number));
}

#[test]
fn convert_valid_integer_strict_mode() {
    let result = convert_element(SchemaType::Integer, ConversionMode::Strict, json!(42)).unwrap();
    assert_json_eq!(result, json!(42));
}

#[test]
fn convert_valid_long_strict_mode() {
    let result = convert_element(SchemaType::Long, ConversionMode::Strict, json!(42)).unwrap();
    assert_json_eq!(result, json!(42));
}

#[test]
fn convert_valid_double_strict_mode() {
    let result = convert_element(SchemaType::Double, ConversionMode::Strict, json!(42.0)).unwrap();
    assert_json_eq!(result, json!(42.0));
}

#[test]
fn convert_valid_float_strict_mode() {
    let result = convert_element(SchemaType::Float, ConversionMode::Strict, json!(42.0)).unwrap();
    assert_json_eq!(result, json!(42.0));
}

#[test]
fn convert_invalid_integer_strict_mode() {
    let invalid_integer = json!(42.1);
    assert_conversion_error(convert_element(SchemaType::Integer, ConversionMode::Strict, invalid_integer));
}

#[test]
fn convert_invalid_long_strict_mode() {
    let invalid_long = json!(2312.451);
    assert_conversion_error(convert_element(SchemaType::Long, ConversionMode::Strict, invalid_long));
}

#[test]
fn convert_integer_use_default_mode() {
    let result =
        convert_element_with_schema(SchemaType::Integer, &Schema::new(), ConversionMode::UseDefault, json!(42))
            .unwrap();
    assert_json_eq!(result, json!(42));
}

#[test]
fn convert_integer_skip_mode() {
    let result =
        convert_element_with_schema(SchemaType::Integer, &Schema::new(), ConversionMode::Skip, Value::Null).unwrap();
    assert_json_eq!(result, Value::Null);
}

#[test]
fn convert_integer_lenient_mode() {
    let result =
        convert_element_with_schema(SchemaType::Integer, &Schema::new(), ConversionMode::Lenient, json!("42"))
            .unwrap();
    assert_json_eq!(result, json!(42));
}

#[test]
fn convert_long_use_default_mode() {
    let result =
        convert_element_with_schema(SchemaType::Long, &Schema::new(), ConversionMode::UseDefault, json!(42)).unwrap();
    assert_json_eq!(result, json!(42));
}

#[test]
fn convert_long_skip_mode() {
    let result =
        convert_element_with_schema(SchemaType::Long, &Schema::new(), ConversionMode::Skip, Value::Null).unwrap();
    assert_json_eq!(result, Value::Null);
}

#[test]
fn convert_long_lenient_mode() {
    let result =
        convert_element_with_schema(SchemaType::Long, &Schema::new(), ConversionMode::Lenient, json!("42")).unwrap();
    assert_json_eq!(result, json!(42));
}

#[test]
fn convert_float_use_default_mode() {
    let result =
        convert_element_with_schema(SchemaType::Float, &Schema::new(), ConversionMode::UseDefault, json!(42.0))
            .unwrap();
    assert_json_eq!(result, json!(42.0));
}

#[test]
fn convert_float_skip_mode() {
    let result =
        convert_element_with_schema(SchemaType::Float, &Schema::new(), ConversionMode::Skip, Value::Null).unwrap();
    assert_json_eq!(result, Value::Null);
}

#[test]
fn convert_float_lenient_mode() {
    let result =
        convert_element_with_schema(SchemaType::Float, &Schema::new(), ConversionMode::Lenient, json!("42.0"))
            .unwrap();
    assert_json_eq!(result, json!(42.0));
}
