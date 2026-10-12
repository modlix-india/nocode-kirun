//! Port of validator/StringFormatSchemaValidatorTest.ts.

use kirun::json::schema::r#type::{SchemaType, TypeUtil};
use kirun::json::schema::string::StringFormat;
use kirun::json::schema::validator::SchemaValidator;
use kirun::json::schema::Schema;
use kirun::repository::KIRunSchemaRepository;
use serde_json::json;

use crate::common::{assert_err_contains, assert_json_eq};
use crate::props;

fn string_with_format(format: StringFormat) -> Schema {
    Schema::new().set_format(format).set_type(TypeUtil::of(&[SchemaType::String]))
}

fn required(names: &[&str]) -> Vec<String> {
    names.iter().map(|s| s.to_string()).collect()
}

#[tokio::test]
async fn schema_validator_fail_with_date_test() {
    let repo = KIRunSchemaRepository::new();
    let schema = Schema::new().set_type(TypeUtil::of(&[SchemaType::Integer]));

    assert_json_eq!(SchemaValidator::validate(Some(vec![]), Some(&schema), Some(&repo), json!(2)).await.unwrap(), json!(2));

    let obj = json!({ "name": "shagil" });
    let obj_schema = Schema::of_object("testObj")
        .set_properties(props(vec![
            ("name", Schema::of_string("name")),
            ("date", Schema::new().set_format(StringFormat::Date)),
        ]))
        .set_required(required(&["name", "date"]));

    assert_err_contains(
        SchemaValidator::validate(Some(vec![]), Some(&obj_schema), Some(&repo), obj).await,
        "date is mandatory",
    );
    let date_obj = json!({ "name": "surendhar.s", "date": "1999-13-12" });
    let error_msg = "Type is missing in schema for declared DATE format.";

    assert_err_contains(
        SchemaValidator::validate(Some(vec![]), Some(&obj_schema), Some(&repo), date_obj).await,
        error_msg,
    );
}

#[tokio::test]
async fn schema_validator_pass_with_date_test() {
    let repo = KIRunSchemaRepository::new();
    let int_schema = Schema::new().set_type(TypeUtil::of(&[SchemaType::Integer])).set_minimum(100.0);

    let obj_schema = Schema::of_object("testObj")
        .set_properties(props(vec![
            ("intSchema", int_schema),
            ("name", Schema::of_string("name")),
            ("date", string_with_format(StringFormat::Date)),
        ]))
        .set_required(required(&["intSchema", "date"]));

    let date_obj = json!({ "intSchema": 1231, "date": "1999-09-12" });

    assert_json_eq!(
        SchemaValidator::validate(Some(vec![]), Some(&obj_schema), Some(&repo), date_obj.clone()).await.unwrap(),
        date_obj
    );
}

#[tokio::test]
async fn schema_validator_fail_with_time_string_type_missing_test() {
    let repo = KIRunSchemaRepository::new();
    let int_schema = Schema::new()
        .set_type(TypeUtil::of(&[SchemaType::Integer]))
        .set_maximum(100.0)
        .set_multiple_of(5.0);

    let obj_schema = Schema::of_object("testObj")
        .set_properties(props(vec![
            ("intSchema", int_schema),
            ("name", Schema::of_string("name").set_min_length(10)),
            ("time", Schema::new().set_format(StringFormat::Time)),
        ]))
        .set_required(required(&["intSchema", "time", "name"]));

    let time_obj = json!({ "intSchema": 95, "time": "22:23:61", "name": "s.surendhar" });

    let err_msg = "Type is missing in schema for declared TIME format.";

    assert_err_contains(
        SchemaValidator::validate(Some(vec![]), Some(&obj_schema), Some(&repo), time_obj).await,
        err_msg,
    );
}

#[tokio::test]
async fn schema_validator_fail_with_time_test() {
    let repo = KIRunSchemaRepository::new();
    let int_schema = Schema::new()
        .set_type(TypeUtil::of(&[SchemaType::Integer]))
        .set_maximum(100.0)
        .set_multiple_of(5.0);

    let obj_schema = Schema::of_object("testObj")
        .set_properties(props(vec![
            ("intSchema", int_schema),
            ("name", Schema::of_string("name").set_min_length(10)),
            ("time", string_with_format(StringFormat::Time)),
        ]))
        .set_required(required(&["intSchema", "time", "name"]));

    let time_obj = json!({ "intSchema": 95, "time": "22:23:61", "name": "s.surendhar" });

    let err_msg = "22:23:61 is not matched with the time pattern";

    assert_err_contains(
        SchemaValidator::validate(Some(vec![]), Some(&obj_schema), Some(&repo), time_obj).await,
        err_msg,
    );
}

#[tokio::test]
async fn schema_validator_pass_with_time_test() {
    let repo = KIRunSchemaRepository::new();
    let int_schema = Schema::new()
        .set_type(TypeUtil::of(&[SchemaType::Integer]))
        .set_maximum(100.0)
        .set_multiple_of(5.0);

    let obj_schema = Schema::of_object("testObj")
        .set_properties(props(vec![
            ("intSchema", int_schema),
            ("name", Schema::of_string("name").set_min_length(10)),
            ("time", string_with_format(StringFormat::Time)),
        ]))
        .set_required(required(&["intSchema", "time", "name"]));

    let time_obj = json!({ "intSchema": 95, "time": "22:23:24", "name": "s.surendhar" });

    assert_json_eq!(
        SchemaValidator::validate(Some(vec![]), Some(&obj_schema), Some(&repo), time_obj.clone()).await.unwrap(),
        time_obj
    );
}

#[tokio::test]
async fn schema_validator_fail_with_email_string_type_missing_test() {
    let repo = KIRunSchemaRepository::new();
    let int_schema = Schema::new().set_type(TypeUtil::of(&[SchemaType::Integer])).set_maximum(100.0);

    let obj_schema = Schema::of_object("testObj")
        .set_properties(props(vec![
            ("intSchema", int_schema),
            ("name", Schema::of_string("name").set_min_length(10)),
            ("email", Schema::new().set_format(StringFormat::Email)),
        ]))
        .set_required(required(&["intSchema", "email", "name"]));

    let email_obj = json!({ "intSchema": 95, "email": "iosdjfdf123--@gmail.com", "name": "s.surendhar" });

    let err_msg = "Type is missing in schema for declared EMAIL format.";

    assert_err_contains(
        SchemaValidator::validate(Some(vec![]), Some(&obj_schema), Some(&repo), email_obj).await,
        err_msg,
    );
}

#[tokio::test]
async fn schema_validator_fail_with_email_test() {
    let repo = KIRunSchemaRepository::new();
    let int_schema = Schema::new()
        .set_type(TypeUtil::of(&[SchemaType::Integer]))
        .set_maximum(3.0)
        .set_multiple_of(5.0);

    let obj_schema = Schema::of_object("testObj")
        .set_properties(props(vec![
            ("intSchema", int_schema),
            ("name", Schema::of_string("name").set_min_length(10)),
            ("email", string_with_format(StringFormat::Email)),
        ]))
        .set_required(required(&["intSchema", "email"]));

    let email_obj = json!({ "intSchema": 0, "email": "asdasdf@@*.com" });

    let err_msg = "asdasdf@@*.com is not matched with the email pattern";

    assert_err_contains(
        SchemaValidator::validate(Some(vec![]), Some(&obj_schema), Some(&repo), email_obj).await,
        err_msg,
    );
}

// Second JS test named 'Schema Validator pass with time test ' (email format).
#[tokio::test]
async fn schema_validator_pass_with_time_test_2() {
    let repo = KIRunSchemaRepository::new();
    let int_schema = Schema::new().set_type(TypeUtil::of(&[SchemaType::Integer]));

    let obj_schema = Schema::of_object("testObj")
        .set_properties(props(vec![
            ("intSchema", int_schema),
            ("name", Schema::of_string("name").set_min_length(10)),
            ("email", string_with_format(StringFormat::Email)),
        ]))
        .set_required(required(&["intSchema", "name"]));

    let email_obj = json!({ "intSchema": 95, "email": "surendhar.s@finc.c", "name": "s.surendhar" });

    assert_json_eq!(
        SchemaValidator::validate(Some(vec![]), Some(&obj_schema), Some(&repo), email_obj.clone()).await.unwrap(),
        email_obj
    );
}

#[tokio::test]
async fn schema_validator_fail_with_date_time_string_type_missing_test() {
    let repo = KIRunSchemaRepository::new();
    let int_schema = Schema::new().set_type(TypeUtil::of(&[SchemaType::Integer])).set_maximum(100.0);

    let obj_schema = Schema::of_object("testObj")
        .set_properties(props(vec![
            ("intSchema", int_schema),
            ("name", Schema::of_string("name").set_min_length(10)),
            ("dateTime", Schema::new().set_format(StringFormat::DateTime)),
        ]))
        .set_required(required(&["intSchema", "dateTime", "name"]));

    let email_obj = json!({ "intSchema": 95, "dateTime": "2023-08-21T07:56:45+12:12", "name": "s.surendhar" });

    let err_msg = "Type is missing in schema for declared DATETIME format.";

    assert_err_contains(
        SchemaValidator::validate(Some(vec![]), Some(&obj_schema), Some(&repo), email_obj).await,
        err_msg,
    );
}

#[tokio::test]
async fn schema_validator_fail_with_date_time_test() {
    let repo = KIRunSchemaRepository::new();
    let int_schema = Schema::new()
        .set_type(TypeUtil::of(&[SchemaType::Integer]))
        .set_maximum(3.0)
        .set_multiple_of(5.0);

    let obj_schema = Schema::of_object("testObj")
        .set_properties(props(vec![
            ("intSchema", int_schema),
            ("name", Schema::of_string("name").set_min_length(10)),
            ("dateTime", string_with_format(StringFormat::DateTime)),
        ]))
        .set_required(required(&["dateTime"]));

    let date_time_obj = json!({ "dateTime": "2023-08-221T07:56:45+12:12" });

    let err_msg = "2023-08-221T07:56:45+12:12 is not matched with the date time pattern";

    assert_err_contains(
        SchemaValidator::validate(Some(vec![]), Some(&obj_schema), Some(&repo), date_time_obj).await,
        err_msg,
    );
}

// Third JS test named 'Schema Validator pass with time test ' (date time format).
#[tokio::test]
async fn schema_validator_pass_with_time_test_3() {
    let repo = KIRunSchemaRepository::new();
    let int_schema = Schema::new().set_type(TypeUtil::of(&[SchemaType::Integer]));

    let obj_schema = Schema::of_object("testObj")
        .set_properties(props(vec![
            ("intSchema", int_schema),
            ("name", Schema::of_string("name").set_min_length(10)),
            ("dateTime", string_with_format(StringFormat::DateTime)),
        ]))
        .set_required(required(&["intSchema", "dateTime"]));

    let date_time_obj = json!({
        "intSchema": 95,
        "dateTime": "2023-08-21T07:56:45+12:12",
        "name": "s.surendhar",
    });

    assert_json_eq!(
        SchemaValidator::validate(Some(vec![]), Some(&obj_schema), Some(&repo), date_time_obj.clone())
            .await
            .unwrap(),
        date_time_obj
    );
}
