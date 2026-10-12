//! Port of validator/TypeValidatorTest.ts.

use kirun::json::schema::convertor::{BooleanConvertor, ConversionMode, NullConvertor, NumberConvertor, StringConvertor};
use kirun::json::schema::r#type::SchemaType;
use kirun::json::schema::validator::{ArrayValidator, BooleanValidator, NumberValidator, StringValidator, TypeValidator};
use kirun::json::schema::Schema;
use kirun::repository::KIRunSchemaRepository;
use kirun::Value;
use serde_json::json;

use crate::common::assert_json_eq;
use crate::props;

#[tokio::test]
async fn type_validator_for_number() {
    let repo = KIRunSchemaRepository::new();
    let element = json!({ "value": 123 });
    let schema = Schema::new();

    let result = TypeValidator::validate(&[], SchemaType::Integer, &schema, Some(&repo), element["value"].clone())
        .await
        .unwrap();
    assert_json_eq!(
        result,
        NumberValidator::validate(SchemaType::Integer, &[], &schema, element["value"].clone()).unwrap()
    );
}

#[tokio::test]
async fn type_validator_for_string() {
    let repo = KIRunSchemaRepository::new();
    let element = json!({ "value": "string" });
    let schema = Schema::new();

    let result = TypeValidator::validate(&[], SchemaType::String, &schema, Some(&repo), element["value"].clone())
        .await
        .unwrap();
    assert_json_eq!(result, StringValidator::validate(&[], &schema, element["value"].clone()).unwrap());
}

#[tokio::test]
async fn type_validator_for_boolean() {
    let repo = KIRunSchemaRepository::new();
    let element = json!({ "value": true });
    let schema = Schema::new();

    let result = TypeValidator::validate(&[], SchemaType::Boolean, &schema, Some(&repo), element["value"].clone())
        .await
        .unwrap();
    assert_json_eq!(result, BooleanValidator::validate(&[], &schema, element["value"].clone()).unwrap());
}

#[tokio::test]
async fn type_validator_for_array() {
    let repo = KIRunSchemaRepository::new();
    let schema = Schema::new();
    let array = json!(["abc"]);

    let result = TypeValidator::validate(&[], SchemaType::Array, &schema, Some(&repo), array.clone()).await.unwrap();
    let expected = ArrayValidator::validate(&[], &schema, Some(&repo), array).await.unwrap();
    assert_json_eq!(result, expected);
}

#[tokio::test]
async fn type_validator_for_null() {
    let repo = KIRunSchemaRepository::new();
    let schema = Schema::new();

    let result = TypeValidator::validate(&[], SchemaType::Null, &schema, Some(&repo), Value::Null).await.unwrap();
    assert_json_eq!(result, Value::Null);
}

#[tokio::test]
async fn type_validator_with_string_conversion() {
    let repo = KIRunSchemaRepository::new();
    let element = json!({ "value": "string" });
    let schema = Schema::new();

    let result = TypeValidator::validate_with(
        &[],
        SchemaType::String,
        &schema,
        Some(&repo),
        element["value"].clone(),
        true,
        Some(ConversionMode::Strict),
    )
    .await
    .unwrap();
    assert_json_eq!(
        result,
        StringConvertor::convert(&[], &schema, ConversionMode::Strict, &element["value"]).unwrap()
    );
}

#[tokio::test]
async fn type_validator_with_number_conversion() {
    let repo = KIRunSchemaRepository::new();
    let element = json!({ "value": "12345" });
    let schema = Schema::new();

    let result = TypeValidator::validate_with(
        &[],
        SchemaType::Integer,
        &schema,
        Some(&repo),
        element["value"].clone(),
        true,
        Some(ConversionMode::Strict),
    )
    .await
    .unwrap();
    assert_json_eq!(
        result,
        NumberConvertor::convert(&[], SchemaType::Integer, &schema, ConversionMode::Strict, &element["value"]).unwrap()
    );
}

#[tokio::test]
async fn type_validator_with_boolean_conversion() {
    let repo = KIRunSchemaRepository::new();
    let element = json!({ "value": "true" });
    let schema = Schema::new();

    let result = TypeValidator::validate_with(
        &[],
        SchemaType::Boolean,
        &schema,
        Some(&repo),
        element["value"].clone(),
        true,
        Some(ConversionMode::Strict),
    )
    .await
    .unwrap();
    assert_json_eq!(
        result,
        BooleanConvertor::convert(&[], &schema, ConversionMode::Strict, &element["value"]).unwrap()
    );
}

#[tokio::test]
async fn type_validator_with_null_conversion() {
    let repo = KIRunSchemaRepository::new();
    let element = Value::Null;
    let schema = Schema::new();

    let result = TypeValidator::validate_with(
        &[],
        SchemaType::Null,
        &schema,
        Some(&repo),
        element.clone(),
        true,
        Some(ConversionMode::Strict),
    )
    .await
    .unwrap();
    assert_json_eq!(result, NullConvertor::convert(&[], &schema, ConversionMode::Strict, &element).unwrap());
}

#[tokio::test]
async fn boolean_convertor_array() {
    let repo = KIRunSchemaRepository::new();
    let schema = Schema::of_array("boolean", vec![Schema::of_boolean("boolean")]);

    let boolean_array = json!([true, false, "yes", "no", "y", "n", 1, 0]);

    let expected_array = json!([true, false, true, false, true, false, true, false]);

    let result = TypeValidator::validate_with(
        &[],
        SchemaType::Array,
        &schema,
        Some(&repo),
        boolean_array,
        true,
        Some(ConversionMode::Strict),
    )
    .await
    .unwrap();

    assert_json_eq!(result, expected_array);
}

#[tokio::test]
async fn number_convertor_array() {
    let repo = KIRunSchemaRepository::new();
    let schema = Schema::of_array("number", vec![Schema::of_number("number")]);

    let number_array = json!(["11", "11.12", "11192371231", "1123.123", "0"]);

    let expected_array = json!([11, 11.12, 11192371231_i64, 1123.123, 0]);

    let result = TypeValidator::validate_with(
        &[],
        SchemaType::Array,
        &schema,
        Some(&repo),
        number_array,
        true,
        Some(ConversionMode::Lenient),
    )
    .await
    .unwrap();

    assert_json_eq!(result, expected_array);
}

#[tokio::test]
async fn object_conversion() {
    let repo = KIRunSchemaRepository::new();
    let number_array = json!(["11", "11.12", "11192371231", "1123.123", "0"]);

    let object = json!({
        "int": "1997",
        "long": "123123123",
        "float": "123.12",
        "double": "123.1232",
        "booleanTrue": "true",
        "booleanFalse": "false",
        "string": 12314,
        "numberArray": number_array,
    });

    let schema = Schema::of_object("jsonObject").set_properties(props(vec![
        ("int", Schema::of_number("int")),
        ("long", Schema::of_long("long")),
        ("float", Schema::of_float("float")),
        ("double", Schema::of_number("double")),
        ("booleanTrue", Schema::of_boolean("booleanTrue")),
        ("booleanFalse", Schema::of_boolean("booleanFalse")),
        ("string", Schema::of_string("string")),
        ("numberArray", Schema::of_array("number", vec![Schema::of_number("number")])),
    ]));

    let expected_object = json!({
        "int": 1997,
        "long": 123123123,
        "float": 123.12,
        "double": 123.1232,
        "booleanTrue": true,
        "booleanFalse": false,
        "string": "12314",
        "numberArray": [11, 11.12, 11192371231_i64, 1123.123, 0],
    });

    let result = TypeValidator::validate_with(
        &[],
        SchemaType::Object,
        &schema,
        Some(&repo),
        object,
        true,
        Some(ConversionMode::Strict),
    )
    .await
    .unwrap();

    assert_json_eq!(result, expected_object);
}

#[tokio::test]
async fn object_of_object_conversion() {
    let repo = KIRunSchemaRepository::new();
    let inner_object = json!({
        "innerInt": "42",
        "innerString": "innerValue",
    });

    let outer_object = json!({
        "innerObject": inner_object,
        "outerInt": "100",
    });

    let inner_schema = Schema::of_object("innerObject").set_properties(props(vec![
        ("innerInt", Schema::of_number("innerInt")),
        ("innerString", Schema::of_string("innerString")),
    ]));

    let outer_schema = Schema::of_object("outerObject").set_properties(props(vec![
        ("innerObject", inner_schema),
        ("outerInt", Schema::of_number("outerInt")),
    ]));

    let expected_outer_object = json!({
        "innerObject": {
            "innerInt": 42,
            "innerString": "innerValue",
        },
        "outerInt": 100,
    });

    let result = TypeValidator::validate_with(
        &[],
        SchemaType::Object,
        &outer_schema,
        Some(&repo),
        outer_object,
        true,
        Some(ConversionMode::Strict),
    )
    .await
    .unwrap();

    assert_json_eq!(result, expected_outer_object);
}
