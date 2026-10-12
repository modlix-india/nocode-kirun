//! Port of validator/NotValidatorTest.ts.

use kirun::json::schema::r#type::SchemaType;
use kirun::json::schema::validator::SchemaValidator;
use kirun::json::schema::Schema;
use kirun::Value;
use serde_json::json;

use crate::common::assert_json_eq;

#[tokio::test]
async fn not_validation() {
    let sch = Schema::of(
        "Not Schema",
        &[SchemaType::Integer, SchemaType::Long, SchemaType::Float, SchemaType::Double, SchemaType::String],
    )
    .set_default_value(json!(1))
    .set_not(Schema::of("Not String", &[SchemaType::String]));

    let value = SchemaValidator::validate(None, Some(&sch), None, json!(0)).await.unwrap();
    assert_json_eq!(value, json!(0));

    let sch = Schema::of(
        "Not Schema",
        &[SchemaType::Integer, SchemaType::Long, SchemaType::Float, SchemaType::Double, SchemaType::String],
    )
    .set_default_value(json!(1))
    .set_not(Schema::of("Not Integer", &[SchemaType::Integer]));

    assert!(SchemaValidator::validate(None, Some(&sch), None, json!(0)).await.is_err());

    let sch = Schema::of("Not Schema", &[SchemaType::Integer, SchemaType::Long, SchemaType::Float])
        .set_default_value(json!(1))
        .set_not(Schema::new().set_constant(json!(0)));

    assert!(SchemaValidator::validate(None, Some(&sch), None, json!(0)).await.is_err());
    assert_json_eq!(SchemaValidator::validate(None, Some(&sch), None, Value::Null).await.unwrap(), json!(1));
    assert_json_eq!(SchemaValidator::validate(None, Some(&sch), None, json!(2)).await.unwrap(), json!(2));
}

#[tokio::test]
async fn constant_validation() {
    let sch = Schema::of("Constant Schema", &[SchemaType::Integer]).set_constant(json!(1));

    let value = SchemaValidator::validate(None, Some(&sch), None, json!(1)).await.unwrap();
    assert_json_eq!(value, json!(1));

    assert!(SchemaValidator::validate(None, Some(&sch), None, json!(0)).await.is_err());
}
