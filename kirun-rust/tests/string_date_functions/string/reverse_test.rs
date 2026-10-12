//! Port of kirun-js `__tests__/engine/function/system/string/ReverseTest.ts`.
//! This is `string::Reverse`, not `array::Reverse`.

use kirun::function::system::string::reverse::Reverse;
use serde_json::json;

use crate::common::*;

async fn reverse(value: &str) -> kirun::Value {
    let reve = Reverse::new();
    let fep = fep().set_arguments(args! { "value" => value });
    first_value(&reve, fep, "value").await.unwrap()
}

#[tokio::test]
async fn reverse_test1() {
    let reveresed = "This is a no code pl\"atfo\"rm ";
    assert_json_eq!(reverse(" mr\"ofta\"lp edoc on a si sihT").await, json!(reveresed));
}

#[tokio::test]
async fn reverse_test2() {
    let reveresed = " ";
    assert_json_eq!(reverse(" ").await, json!(reveresed));
}

#[tokio::test]
async fn reverse_test_empty_string() {
    assert_json_eq!(reverse("").await, json!(""));
}

#[tokio::test]
async fn reverse_test_single_character() {
    assert_json_eq!(reverse("a").await, json!("a"));
}

#[tokio::test]
async fn reverse_test_palindrome() {
    assert_json_eq!(reverse("racecar").await, json!("racecar"));
}

#[tokio::test]
async fn reverse_test_special_characters() {
    assert_json_eq!(reverse("!@#$%").await, json!("%$#@!"));
}

#[tokio::test]
async fn reverse_test_numbers_in_string() {
    assert_json_eq!(reverse("12345").await, json!("54321"));
}
