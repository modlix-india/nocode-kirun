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
