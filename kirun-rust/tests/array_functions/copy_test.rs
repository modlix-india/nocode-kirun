//! Port of kirun-js `__tests__/engine/function/system/array/CopyTest.ts`.

use kirun::function::system::array::copy::Copy;
use serde_json::json;

use crate::common::*;

#[tokio::test]
async fn compy_test() {
    let copy = Copy::new();

    let source = json!([1, 2, 3, 4, 5]);

    let fep1 = crate::common::fep().set_arguments(args! { "source" => source, "srcFrom" => 2, "length" => 4 });

    // JS: rejects.toThrow() with no message.
    assert!(first_value(&copy, fep1, "result").await.is_err());

    // source.push(6)
    let source = json!([1, 2, 3, 4, 5, 6]);
    let result = json!([3, 4, 5, 6]);

    let fep = crate::common::fep().set_arguments(args! { "source" => source, "srcFrom" => 2, "length" => 4 });
    assert_json_eq!(first_value(&copy, fep, "result").await.unwrap(), result);

    let source = json!([{ "name": "Kiran" }, { "name": "Kumar" }]);
    // JS builds (and discards) an unused fep with srcFrom 2 / length 4 here.
    let result = json!([{ "name": "Kiran" }, { "name": "Kumar" }]);

    let fep = crate::common::fep().set_arguments(args! { "source" => source.clone() });
    let out = first_value(&copy, fep, "result").await.unwrap();
    assert_json_eq!(out.clone(), result);
    // JS `expect(source[0] == result[0]).toBeFalsy()` checks object identity (a deep copy is a
    // different object). Rust values have no identity; deep-copied values must still be equal.
    assert_json_eq!(out[0].clone(), source[0].clone());

    let fep = crate::common::fep().set_arguments(args! { "source" => source.clone(), "deepCopy" => false });
    let result = first_value(&copy, fep, "result").await.unwrap();
    // JS `expect(source[0] == result[0]).toBeTruthy()`: a shallow copy shares the object. Rust
    // can only check value equality.
    assert_json_eq!(source[0].clone(), result[0].clone());
}
