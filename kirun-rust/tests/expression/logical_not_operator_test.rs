//! Port of `operators/unary/LogicalNotOperatorTest.ts`.

use kirun::runtime::expression::operators::binary::{
    BinaryOperator, LogicalGreaterThanEqualOperator, LogicalGreaterThanOperator, LogicalLessThanEqualOperator,
    LogicalLessThanOperator,
};
use kirun::runtime::expression::operators::unary::{LogicalNotOperator, UnaryOperator};
use kirun::Value;
use serde_json::json;

use crate::is_truthy;

fn not(v: Value) -> bool {
    is_truthy(&LogicalNotOperator::new().apply(&v).unwrap())
}

#[test]
fn logical_not_test() {
    assert!(not(Value::Null));
    // JS `undefined`: same as null in Rust.
    assert!(not(Value::Null));
    assert!(not(json!(false)));
    assert!(not(json!(0)));

    assert!(!not(json!(true)));
    assert!(!not(json!(1)));

    assert!(!not(json!({ "name": "Kiran" })));

    assert!(!not(json!([])));
    assert!(!not(json!("")));
    assert!(!not(json!("TRUE")));
}

#[test]
fn logical_greate_than() {
    assert!(!is_truthy(&LogicalGreaterThanOperator::new().apply(&json!(0), &json!(4)).unwrap()));
    assert!(is_truthy(&LogicalLessThanOperator::new().apply(&json!(0), &json!(4)).unwrap()));
    assert!(is_truthy(&LogicalGreaterThanEqualOperator::new().apply(&json!(0), &json!(0)).unwrap()));
    assert!(is_truthy(&LogicalLessThanEqualOperator::new().apply(&json!(0), &json!(0)).unwrap()));
}
