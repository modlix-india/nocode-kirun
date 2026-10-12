//! Port of `tokenextractor/TokenValueExtractorTest.ts`.

use kirun::runtime::expression::tokenextractor::token_value_extractor::{retrieve_element_from, split_path};
use kirun::runtime::expression::tokenextractor::{TokenValueExtractor, ValuesMap};
use kirun::{Result, Value};
use serde_json::json;

use crate::common::assert_json_eq;

struct TestExtractor;

impl TokenValueExtractor for TestExtractor {
    fn get_value_internal(&self, _token: &str, _values_map: Option<&ValuesMap>) -> Result<Value> {
        Ok(Value::Null)
    }

    fn get_prefix(&self) -> &str {
        "Testing"
    }

    fn get_store(&self) -> Value {
        Value::Null
    }
}

impl TestExtractor {
    /// JS exposes the protected `retrieveElementFrom`.
    fn retrieve_element_from(&self, token: &str, parts: &[String], part_number: usize, json_element: &Value) -> Value {
        retrieve_element_from(token, parts, part_number, json_element, None).unwrap()
    }
}

/// JS `token.split(new RegExp('\\.'))`.
fn dot_split(token: &str) -> Vec<String> {
    token.split('.').map(String::from).collect()
}

#[test]
fn token_value_extractor_test() {
    let extractor = TestExtractor;

    let darr = json!([[2, 4, 6], [3, 6, 9], [4, 8, 12, 16]]);
    let arr = json!([0, 2, 4, 6]);
    let b = json!({ "c": "K", "arr": arr, "darr": darr });
    let a = json!({ "b": b });
    let obj = json!({ "a": a, "array": arr });

    let token = "[2]";
    assert_json_eq!(extractor.retrieve_element_from(token, &dot_split(token), 0, &arr), json!(4));

    let token = "[1][1]";
    assert_json_eq!(extractor.retrieve_element_from(token, &dot_split(token), 0, &darr), json!(6));

    let token = "[2].length";
    assert_json_eq!(extractor.retrieve_element_from(token, &dot_split(token), 0, &darr), json!(4));

    let token = "a.b.c";
    assert_json_eq!(extractor.retrieve_element_from(token, &dot_split(token), 0, &obj), json!("K"));

    // JS toBe(b): identity; Rust compares structurally.
    let token = "a.b";
    assert_json_eq!(extractor.retrieve_element_from(token, &dot_split(token), 0, &obj), b.clone());

    let token = "a.b.c";
    assert_json_eq!(extractor.retrieve_element_from(token, &dot_split(token), 1, &a), json!("K"));

    let token = "a.b.arr[2]";
    assert_json_eq!(extractor.retrieve_element_from(token, &dot_split(token), 1, &a), json!(4));

    let token = "a.b.darr[2][3]";
    assert_json_eq!(extractor.retrieve_element_from(token, &dot_split(token), 1, &a), json!(16));

    let token = "a.b.darr[2].length";
    assert_json_eq!(extractor.retrieve_element_from(token, &dot_split(token), 1, &a), json!(4));
}

#[test]
fn bracket_notation_with_dotted_keys() {
    let extractor = TestExtractor;

    let mut config = json!({
        "mail.props.port": 587,
        "mail.props.host": "smtp.example.com",
        "api.key.secret": "secret123",
        "simple": "value"
    });

    let obj = json!({ "config": config });

    let token = "config[\"mail.props.port\"]";
    assert_json_eq!(extractor.retrieve_element_from(token, &split_path(token), 0, &obj), json!(587));

    let token = "config['mail.props.host']";
    assert_json_eq!(extractor.retrieve_element_from(token, &split_path(token), 0, &obj), json!("smtp.example.com"));

    let token = "config['api.key.secret']";
    assert_json_eq!(extractor.retrieve_element_from(token, &split_path(token), 0, &obj), json!("secret123"));

    // JS mutates `config` after building `obj` (shared reference); rebuild obj in Rust.
    config["nested"] = json!({ "field.with.dots": "nestedValue" });
    let obj = json!({ "config": config });

    let token = "config.nested['field.with.dots']";
    assert_json_eq!(extractor.retrieve_element_from(token, &split_path(token), 0, &obj), json!("nestedValue"));

    let token = "config.simple";
    assert_json_eq!(extractor.retrieve_element_from(token, &split_path(token), 0, &obj), json!("value"));
}

#[test]
fn split_path_correctly_handles_bracket_notation() {
    let parts = split_path("Context.obj['mail.props.port']");
    assert_eq!(parts.len(), 2);
    assert_eq!(parts[0], "Context");
    assert_eq!(parts[1], "obj['mail.props.port']");

    let parts = split_path("Context.obj['mail.props.port'].value");
    assert_eq!(parts.len(), 3);
    assert_eq!(parts[0], "Context");
    assert_eq!(parts[1], "obj['mail.props.port']");
    assert_eq!(parts[2], "value");

    let parts = split_path("Steps.source.output['field.name']");
    assert_eq!(parts.len(), 3);
    assert_eq!(parts[0], "Steps");
    assert_eq!(parts[1], "source");
    assert_eq!(parts[2], "output['field.name']");

    // Range operator (..) is preserved
    let parts = split_path("array[0..5]");
    assert_eq!(parts.len(), 1);
    assert_eq!(parts[0], "array[0..5]");

    let parts = split_path("obj['key.one']['key.two']");
    assert_eq!(parts.len(), 1);
    assert_eq!(parts[0], "obj['key.one']['key.two']");
}
