//! Schema validation as kirun-java does it (stored server functions run on kirun-java): ports of
//! kirun-java's `NumberValidatorTest` and of the messages its `ReactiveStringFormatSchemaValidatorTest`
//! and `ReactiveArraySchemaTypeValidatorTest` assert, plus the rules kirun-js does not share
//! (K4: an `INTEGER` must fit a Java `int`). Expected answers were measured on kirun-java 4.9.2.

use kirun::json::schema::r#type::SchemaType;
use kirun::json::schema::validator::number_validator::NumberValidator;
use kirun::json::schema::validator::schema_validator::SchemaValidator;
use kirun::json::schema::Schema;
use kirun::Value;
use serde_json::json;

async fn validate(schema: Value, value: Value) -> Result<Value, String> {
    let schema = Schema::from_value(&schema).unwrap().unwrap();
    let repo = kirun::repository::KIRunSchemaRepository::new();
    SchemaValidator::validate(None, Some(&schema), Some(&repo), value).await.map_err(|e| e.to_string())
}

fn number(t: SchemaType, schema: Value, v: Value) -> Result<Value, String> {
    let schema = Schema::from_value(&schema).unwrap().unwrap();
    NumberValidator::validate(t, &[], &schema, v).map_err(|e| e.to_string())
}

#[test]
fn number_validator_test_ports() {
    // NumberValidatorValidateTestForNull / ForIsNumber.
    assert_eq!(number(SchemaType::Integer, json!({}), Value::Null), Err("Expected a number but found null".into()));
    assert_eq!(
        number(SchemaType::Integer, json!({}), json!({ "value": "123" })),
        Err("{\"value\":\"123\"} is not a Integer".into())
    );
    // ...RangeCheckMinimum / Maximum.
    assert_eq!(
        number(SchemaType::Integer, json!({ "type": "INTEGER", "minimum": 124 }), json!(123)),
        Err("123 should be greater than or equal to 124".into())
    );
    assert_eq!(
        number(SchemaType::Integer, json!({ "type": "INTEGER", "maximum": 124 }), json!(129)),
        Err("129 should be less than or equal to 124".into())
    );
    // ...ExclusiveMaximum / ExclusiveMinimum (a float).
    assert_eq!(
        number(SchemaType::Float, json!({ "type": "FLOAT", "exclusiveMaximum": 9 }), json!(10.7)),
        Err("10.7 should be less than 9".into())
    );
    assert_eq!(
        number(SchemaType::Float, json!({ "type": "FLOAT", "exclusiveMinimum": 9 }), json!(4)),
        Err("4 should be greater than 9".into())
    );
    // ...MultipleCheck (long) and ...ForDoubleValue (float), and the custom message.
    assert_eq!(
        number(SchemaType::Long, json!({ "type": "LONG", "multipleOf": 7738718 }), json!(154774374)),
        Err("154774374 is not multiple of 7738718".into())
    );
    assert_eq!(
        number(SchemaType::Float, json!({ "type": "FLOAT", "multipleOf": 444 }), json!(10.7)),
        Err("10.7 is not multiple of 444".into())
    );
    assert_eq!(
        number(
            SchemaType::Float,
            json!({ "type": "FLOAT", "multipleOf": 444, "details": { "validationMessages": { "multipleOf": "It is not a multiple of 444.33" } } }),
            json!(10.7)
        ),
        Err("It is not a multiple of 444.33".into())
    );
}

#[test]
fn integer_and_long_ranges_follow_java() {
    let int = json!({ "type": "INTEGER" });
    assert_eq!(number(SchemaType::Integer, int.clone(), json!(2147483647)), Ok(json!(2147483647)));
    for v in [json!(2147483648_i64), json!(-2147483649_i64), json!(9007199254740993_i64), json!(1e10)] {
        let shown = serde_json::to_string(&v).unwrap();
        assert_eq!(number(SchemaType::Integer, int.clone(), v), Err(format!("{shown} is not a number of type Integer")));
    }
    assert_eq!(number(SchemaType::Integer, int.clone(), json!(3.0)), Ok(json!(3.0)));
    assert_eq!(number(SchemaType::Integer, int, json!(3.5)), Err("3.5 is not a number of type Integer".into()));
    let long = json!({ "type": "LONG" });
    assert_eq!(number(SchemaType::Long, long.clone(), json!(9007199254740993_i64)), Ok(json!(9007199254740993_i64)));
    assert_eq!(
        number(SchemaType::Long, long, json!(9223372036854775808_u64)),
        Err("9223372036854775808 is not a number of type Long".into())
    );
    // A double's multipleOf divides; a quotient beyond an int fails (Java's `intValue` check).
    let mult = json!({ "type": "DOUBLE", "multipleOf": 2 });
    assert_eq!(number(SchemaType::Double, mult.clone(), json!(1e9)), Ok(json!(1e9)));
    assert_eq!(number(SchemaType::Double, mult, json!(1e10)), Err("10000000000.0 is not multiple of 2".into()));
}

#[tokio::test]
async fn messages_carry_the_schema_path_like_java() {
    // ReactiveStringFormatSchemaValidatorTest: "intTest - intTest.intSchema - intTest.intSchema - ...".
    let schema = json!({
        "type": "OBJECT", "name": "intTest",
        "properties": { "value": { "type": "INTEGER", "name": "intSchema" } }
    });
    assert_eq!(
        validate(schema, json!({ "value": "1997" })).await,
        Err("intTest - intTest.intSchema - intTest.intSchema - \"1997\" is not a Integer".into())
    );
    // A storage row's integer field beyond an int (K4).
    let row = json!({ "type": "OBJECT", "name": "rpScrParent", "properties": { "age": { "type": "INTEGER" } } });
    assert_eq!(
        validate(row, json!({ "age": 9007199254740993_i64 })).await,
        Err("rpScrParent - rpScrParent - rpScrParent - 9007199254740993 is not a number of type Integer".into())
    );
}

#[tokio::test]
async fn validators_word_and_match_like_java() {
    assert_eq!(validate(json!({ "type": "STRING" }), json!(5)).await, Err("5 is not String".into()));
    assert_eq!(validate(json!({ "type": "BOOLEAN" }), json!("true")).await, Err("\"true\" is not a boolean".into()));
    assert_eq!(validate(json!({ "type": "ARRAY" }), json!("x")).await, Err("\"x\" is not an Array".into()));
    assert_eq!(validate(json!({ "type": "OBJECT" }), json!([1])).await, Err("[1] is not an Object".into()));
    assert_eq!(validate(json!({ "type": "NULL" }), json!("x")).await, Err("Expected a null but found \"x\"".into()));
    // A pattern matches the whole string (Java `matches`; kirun-js finds it anywhere).
    let digits = json!({ "type": "STRING", "pattern": "[0-9]+" });
    assert_eq!(validate(digits.clone(), json!("123")).await, Ok(json!("123")));
    assert_eq!(
        validate(digits, json!("abc123")).await,
        Err("\"abc123\" is not matched with the pattern [0-9]+".into())
    );
    // A maximum of zero counts.
    assert_eq!(
        validate(json!({ "type": "STRING", "maxLength": 0 }), json!("a")).await,
        Err("Expected a maximum of 0 characters".into())
    );
    assert_eq!(
        validate(json!({ "type": "ARRAY", "maxItems": 0 }), json!([1])).await,
        Err("Array can have  maximum of 0 elements".into())
    );
    // Equal objects are not unique; enums compare by content and print as a Java list.
    assert_eq!(
        validate(json!({ "type": "ARRAY", "uniqueItems": true }), json!([{ "a": 1 }, { "a": 1 }])).await,
        Err("Items on the array are not unique".into())
    );
    assert_eq!(validate(json!({ "enums": [{ "a": 1 }] }), json!({ "a": 1 })).await, Ok(json!({ "a": 1 })));
    assert_eq!(
        validate(json!({ "type": "STRING", "enums": ["A", "B"] }), json!("C")).await,
        Err("Value is not one of [\"A\", \"B\"]".into())
    );
    // Additional properties print as kirun-java's HashSet: [a, other, b, extra].
    let closed = json!({ "type": "OBJECT", "properties": { "name": { "type": "STRING" } }, "additionalProperties": false });
    assert_eq!(
        validate(closed, json!({ "name": "x", "other": 1, "extra": 2, "b": 3, "a": 4 })).await,
        Err("[a, other, b, extra] are additional properties which are not allowed.".into())
    );
}
