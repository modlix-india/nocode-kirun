//! Port of kirun-js `__tests__/engine/function/system/string/ToStringTest.ts`.
//! JS names the 2nd and 3rd tests `toString test2` and `toString test2 ` (trailing space);
//! the 3rd becomes `to_string_test2_2`.

use kirun::function::system::string::to_string::ToString;
use serde_json::json;

use crate::common::*;

#[tokio::test]
async fn to_string_test1() {
    let to_string = ToString::new();
    let fep = fep().set_arguments(args! { "anytype" => 123 });

    assert_json_eq!(first_value(&to_string, fep, "result").await.unwrap(), json!("123"));
}

#[tokio::test]
async fn to_string_test2() {
    let to_string = ToString::new();
    let array = vec![
        "I", "am", "using", "eclipse", "to", "test", "the", "changes", "with", "test", "Driven", "developement",
    ];
    let fep = fep().set_arguments(args! { "anytype" => array });

    // JS `JSON.stringify(value, null, 2)`.
    let expected = r#"[
  "I",
  "am",
  "using",
  "eclipse",
  "to",
  "test",
  "the",
  "changes",
  "with",
  "test",
  "Driven",
  "developement"
]"#;

    assert_json_eq!(first_value(&to_string, fep, "result").await.unwrap(), json!(expected));
}

#[tokio::test]
async fn to_string_test2_2() {
    let to_string = ToString::new();
    let fep = fep().set_arguments(args! { "anytype" => true });

    assert_json_eq!(first_value(&to_string, fep, "result").await.unwrap(), json!("true"));
}

// kirun-java's ToString is Gson with pretty printing (stored server functions regex-match its
// output). Every expected string below was measured on kirun-java 4.9.2.

async fn java_to_string(input: serde_json::Value) -> serde_json::Value {
    first_value(&ToString::new(), fep().set_arguments(args! { "anytype" => input }), "result").await.unwrap()
}

#[tokio::test]
async fn java_quotes_a_string() {
    // QA-0457: LeadZumpV2.rulesQueryCheck matches `\A("(ASC|DESC)"|...)\z` against this.
    assert_eq!(java_to_string(json!("DESC")).await, json!("\"DESC\""));
    assert_eq!(java_to_string(json!("")).await, json!("\"\""));
    assert_eq!(java_to_string(json!("01JA2B3C4D5E6F7G8H9JKMNPQR")).await, json!("\"01JA2B3C4D5E6F7G8H9JKMNPQR\""));
}

#[tokio::test]
async fn java_escapes_like_gson() {
    assert_eq!(java_to_string(json!("a<b>&='c'")).await, json!(r#""a\u003cb\u003e\u0026\u003d\u0027c\u0027""#));
    assert_eq!(java_to_string(json!("line\nbreak\ttab\u{1}\u{7f}")).await, json!("\"line\\nbreak\\ttab\\u0001\u{7f}\""));
    assert_eq!(java_to_string(json!("q\"uote\\back/slash")).await, json!(r#""q\"uote\\back/slash""#));
    assert_eq!(java_to_string(json!("\u{2028}x\u{2029}")).await, json!(r#""\u2028x\u2029""#));
    assert_eq!(java_to_string(json!("unicode é ✓ 😀")).await, json!("\"unicode é ✓ 😀\""));
}

#[tokio::test]
async fn java_prints_numbers_booleans_and_null() {
    assert_eq!(java_to_string(json!(7)).await, json!("7"));
    assert_eq!(java_to_string(json!(18000000000i64)).await, json!("18000000000"));
    assert_eq!(java_to_string(json!(5.0)).await, json!("5.0"));
    assert_eq!(java_to_string(json!(2.5)).await, json!("2.5"));
    assert_eq!(java_to_string(json!(1e7)).await, json!("1.0E7"));
    assert_eq!(java_to_string(json!(0.0001)).await, json!("1.0E-4"));
    assert_eq!(java_to_string(json!(-0.0)).await, json!("-0.0"));
    assert_eq!(java_to_string(json!(false)).await, json!("false"));
    assert_eq!(java_to_string(serde_json::Value::Null).await, json!("null"));
}

#[tokio::test]
async fn java_pretty_prints_objects_without_null_members() {
    let input = json!({ "a": 1, "b": null, "c": "x", "d": { "e": null } });
    assert_eq!(java_to_string(input).await, json!("{\n  \"a\": 1,\n  \"c\": \"x\",\n  \"d\": {}\n}"));
    let input = json!([1, null, "x", [], {}]);
    assert_eq!(java_to_string(input).await, json!("[\n  1,\n  null,\n  \"x\",\n  [],\n  {}\n]"));
    let input = json!({ "s": "<&>", "n": { "m": [1, { "k": "v" }] }, "f": 5.0 });
    assert_eq!(
        java_to_string(input).await,
        json!("{\n  \"s\": \"\\u003c\\u0026\\u003e\",\n  \"n\": {\n    \"m\": [\n      1,\n      {\n        \"k\": \"v\"\n      }\n    ]\n  },\n  \"f\": 5.0\n}")
    );
}

#[tokio::test]
async fn java_keeps_the_key_order_it_is_given() {
    let input = json!({ "field": "id", "operator": "EQUALS", "value": "01JA2B3C4D5E6F7G8H9JKMNPQR" });
    assert_eq!(
        java_to_string(input).await,
        json!("{\n  \"field\": \"id\",\n  \"operator\": \"EQUALS\",\n  \"value\": \"01JA2B3C4D5E6F7G8H9JKMNPQR\"\n}")
    );
}
