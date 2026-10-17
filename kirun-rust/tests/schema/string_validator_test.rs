//! Port of validator/StringValidatorTest.ts.
// kirun-java prints the value as JSON in pattern messages (`"x" is not matched with the ...`);
// kirun-js printed it bare. Stored server functions run on kirun-java.

use kirun::json::schema::r#type::{SchemaType, TypeUtil};
use kirun::json::schema::string::StringFormat;
use kirun::json::schema::validator::{SchemaValidator, StringValidator};
use kirun::json::schema::Schema;
use serde_json::json;

use crate::common::{assert_err_contains, assert_json_eq};
use crate::schema_from;

#[test]
fn string_valid_case() {
    let value = json!("surendhar");
    let schema = Schema::new().set_type(TypeUtil::of(&[SchemaType::String]));

    assert_json_eq!(StringValidator::validate(&[], &schema, value.clone()).unwrap(), value);
}

#[test]
fn string_invalid_case() {
    let schema = Schema::new().set_type(TypeUtil::of(&[SchemaType::String]));

    // JS: `expect(() => StringValidator.validate([], schema, 123).toThrow(...))` - the misplaced
    // paren means the closure is never invoked and nothing is asserted. Mirrored as-is.
    let _never_invoked = || StringValidator::validate(&[], &schema, json!(123));
}

#[test]
fn string_min_length_invalid() {
    let value = json!("abcd");
    let schema = Schema::new().set_type(TypeUtil::of(&[SchemaType::String])).set_min_length(5);

    // JS asserts nothing here (closure never invoked, see string_invalid_case).
    let _never_invoked = || StringValidator::validate(&[], &schema, value.clone());
}

#[test]
fn string_max_length_invalid() {
    let value = json!("surendhar");
    let schema = Schema::new().set_type(TypeUtil::of(&[SchemaType::String])).set_max_length(8);

    // JS asserts nothing here (closure never invoked, see string_invalid_case).
    let _never_invoked = || StringValidator::validate(&[], &schema, value.clone());
}

#[test]
fn string_min_length() {
    let value = json!("abcdefg");
    let schema = Schema::new().set_type(TypeUtil::of(&[SchemaType::String])).set_min_length(5);

    assert_json_eq!(StringValidator::validate(&[], &schema, value.clone()).unwrap(), value);
}

#[test]
fn string_max_length() {
    let value = json!("surendhar");
    let schema = Schema::new().set_type(TypeUtil::of(&[SchemaType::String])).set_max_length(12323);

    assert_json_eq!(StringValidator::validate(&[], &schema, value.clone()).unwrap(), value);
}

#[test]
fn string_date_invalid_case() {
    let value = json!("1234-12-1245");

    let schema = Schema::new().set_format(StringFormat::Date);

    // JS asserts nothing here (closure never invoked, see string_invalid_case).
    let _never_invoked = || StringValidator::validate(&[], &schema, value.clone());
}

#[test]
fn string_date_valid_case() {
    let value = json!("2023-01-26");

    let schema = Schema::new().set_format(StringFormat::Date);

    assert_json_eq!(StringValidator::validate(&[], &schema, value.clone()).unwrap(), value);
}

#[test]
fn string_time_invalid_case() {
    let value = "231:45:56";

    let schema = Schema::new().set_format(StringFormat::Time);

    assert_err_contains(
        StringValidator::validate(&[], &schema, json!(value)),
        &format!("\"{value}\" is not matched with the time pattern"),
    );
}

#[test]
fn string_time_valid_case() {
    let value = json!("22:32:45");

    let schema = Schema::new().set_format(StringFormat::Time);

    assert_json_eq!(StringValidator::validate(&[], &schema, value.clone()).unwrap(), value);
}

#[test]
fn string_date_time_invalid_case() {
    let value = "26-jan-2023 231:45:56";

    let schema = Schema::new().set_format(StringFormat::DateTime);

    assert_err_contains(
        StringValidator::validate(&[], &schema, json!(value)),
        &format!("\"{value}\" is not matched with the date time pattern"),
    );
}

#[test]
fn string_date_time_valid_case() {
    let value = json!("2032-02-12T02:54:23");

    let schema = Schema::new().set_format(StringFormat::DateTime);

    assert_json_eq!(StringValidator::validate(&[], &schema, value.clone()).unwrap(), value);
}

#[test]
fn string_date_valid_on_the_10th_and_20th() {
    let schema = Schema::new().set_format(StringFormat::Date);

    for value in ["2026-01-10", "2026-01-20", "2026-01-19", "2026-01-29"] {
        assert_json_eq!(StringValidator::validate(&[], &schema, json!(value)).unwrap(), json!(value));
    }
}

#[test]
fn string_date_time_valid_on_the_10th_and_20th() {
    let schema = Schema::new().set_format(StringFormat::DateTime);

    for value in ["2026-01-10T10:00:00", "2026-01-20T10:00:00", "2026-01-20T10:00:00+05:30"] {
        assert_json_eq!(StringValidator::validate(&[], &schema, json!(value)).unwrap(), json!(value));
    }
}

fn expect_valid(format: StringFormat, values: &[&str]) {
    let schema = Schema::new().set_format(format);

    for value in values {
        assert_json_eq!(StringValidator::validate(&[], &schema, json!(value)).unwrap(), json!(value));
    }
}

fn expect_invalid(format: StringFormat, pattern_name: &str, values: &[&str]) {
    let schema = Schema::new().set_format(format);

    for value in values {
        assert_err_contains(
            StringValidator::validate(&[], &schema, json!(value)),
            &format!("\"{value}\" is not matched with the {pattern_name}"),
        );
    }
}

#[test]
fn string_date_time_and_time_with_fractional_seconds_and_z() {
    expect_valid(
        StringFormat::DateTime,
        &["2026-01-15T10:00:00Z", "2026-01-15T10:00:00.123Z", "2026-01-15T10:00:00.123456789+05:30"],
    );
    expect_invalid(
        StringFormat::DateTime,
        "date time pattern",
        &["2026-01-15T10:00.123", "2026-01-15T10:00:00.1234567890Z", "2026-01-15T10:00:00.Z"],
    );

    expect_valid(StringFormat::Time, &["10:00:00Z", "10:00:00.123", "10:00:00.123+05:30"]);
    expect_invalid(StringFormat::Time, "time pattern", &["10:00.5", "10:00:00."]);
}

#[test]
fn string_decimal() {
    expect_valid(StringFormat::Decimal, &["0", "100", "-12.50", "+3.14159"]);
    expect_invalid(StringFormat::Decimal, "decimal pattern", &["1e5", "12.", ".5", "1,000", "abc", ""]);
}

#[test]
fn string_id() {
    expect_valid(
        StringFormat::Id,
        &["65f1c2a9e4b0a1b2c3d4e5f6", "01J9ZQ3V8K2M4N6P8R0S2T4V6W", "01j9zq3v8k2m4n6p8r0s2t4v6w"],
    );
    expect_invalid(
        StringFormat::Id,
        "id pattern",
        &["65f1c2a9e4b0a1b2c3d4e5fg", "01J9ZQ3V8K2M4N6P8R0S2T4V6U", "01J9ZQ3V8K2M4N6P8R0S2T4V6", "abc"],
    );
}

#[test]
fn string_email_invalid_case() {
    let value = "testemail fai%6&8ls@gmail.com";

    let schema = Schema::new().set_format(StringFormat::Email);

    assert_err_contains(
        StringValidator::validate(&[], &schema, json!(value)),
        &format!("\"{value}\" is not matched with the email pattern"),
    );
}

#[test]
fn string_email_valid_case() {
    let value = json!("testemaifai%6&8lworkings@magil.com");

    let schema = Schema::new().set_format(StringFormat::Email);

    assert_json_eq!(StringValidator::validate(&[], &schema, value.clone()).unwrap(), value);
}

#[tokio::test]
async fn string_custom_message() {
    let schema = schema_from(json!({
        "type": "STRING",
        "minLength": 10,
        "details": {
            "validationMessages": {
                "minLength": "You must enter something with minimum of ten characters"
            }
        }
    }));

    assert_err_contains(
        SchemaValidator::validate(Some(vec![]), Some(&schema), None, json!("asdf")).await,
        "You must enter something with minimum of ten characters",
    );
}

#[test]
fn string_date_rejects_month_zero() {
    // The month alternation was ([0][0-9]|[1][0-2]), so 00 matched the first
    // branch. 2026-00-15 validated on all three runtimes and then failed wherever
    // something tried to make a real date out of it.
    let date = Schema::new().set_format(StringFormat::Date);

    assert!(StringValidator::validate(&[], &date, json!("2026-00-15")).is_err());
    assert!(StringValidator::validate(&[], &date, json!("2026-00-01")).is_err());

    for valid in ["2026-01-15", "2026-09-30", "2026-10-01", "2026-12-31"] {
        assert_json_eq!(StringValidator::validate(&[], &date, json!(valid)).unwrap(), json!(valid));
    }

    let date_time = Schema::new().set_format(StringFormat::DateTime);
    assert!(StringValidator::validate(&[], &date_time, json!("2026-00-15T10:00:00Z")).is_err());
}
