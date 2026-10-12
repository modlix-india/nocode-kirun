//! Port of `tokenextractor/OutputMapTokenValueExtractorTest.ts`.

use kirun::runtime::expression::tokenextractor::TokenValueExtractor;
use kirun::runtime::tokenextractor::OutputMapTokenValueExtractor;
use kirun::{shared, Map, Value};
use serde_json::json;

use crate::common::{args, assert_json_eq};
use crate::steps_map;

#[test]
fn output_map_token_value_extractor_test() {
    let phone = json!({ "phone1": "1234", "phone2": "5678", "phone3": "5678" });
    let address = json!({
        "line1": "Flat 202, PVR Estates",
        "line2": "Nagvara",
        "city": "Benguluru",
        "pin": "560048",
        "phone": phone
    });
    let obj = json!({ "studentName": "Kumar", "math": 20, "isStudent": true, "address": address });

    let output = steps_map(vec![("step1", "output", args! { "zero" => 0, "name" => "Kiran", "obj" => obj })]);

    let omtv = OutputMapTokenValueExtractor::new(shared(output));
    assert_json_eq!(omtv.get_value("Steps.step1.output.zero").unwrap(), json!(0));
    assert_json_eq!(omtv.get_value("Steps.step1.output.obj.address.phone.phone2").unwrap(), json!("5678"));
}

#[test]
fn output_map_token_value_extractor_test_2() {
    let output = steps_map(vec![
        ("step1", "output", args! { "arr" => json!(["a", "b", "c"]) }),
        ("step2", "output", Map::<Value>::new()),
    ]);

    let omtv = OutputMapTokenValueExtractor::new(shared(output));
    assert_json_eq!(omtv.get_value("Steps.step1.output.arr[1]").unwrap(), json!("b"));
    // JS toBeUndefined
    assert_json_eq!(omtv.get_value("Steps.step1.output.arr1[1]").unwrap(), Value::Null);
    // JS toBeDefined: the (empty) output map
    assert!(!omtv.get_value("Steps.step2.output").unwrap().is_null());
}
