#[path = "../common/mod.rs"]
mod common;

use common::*;
use serde_json::json;

#[test]
fn helpers_compile() {
    let m = args! { "a" => 1, "b" => json!([1, 2.0]) };
    assert_eq!(m.len(), 2);
    assert_json_eq!(json!([1, 2]), json!([1.0, 2]));
    assert_json_eq!(kirun::util::primitive::number_value(3.0), json!(3));
}
