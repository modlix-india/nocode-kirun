//! Port of `tokenextractor/ObjectValueSetterExtractorTest.ts`.

use kirun::runtime::expression::tokenextractor::object_value_setter_extractor::ObjectValueSetterExtractor;
use kirun::runtime::expression::tokenextractor::TokenValueExtractor;
use kirun::Value;
use serde_json::json;

use crate::common::assert_json_eq;

#[tokio::test]
async fn object_value_setter_extractor_test() {
    let store = json!({
        "name": "Kiran",
        "addresses": [
            { "city": "Bangalore", "state": "Karnataka", "country": "India" },
            { "city": "Kakinada", "state": "Andhra Pradesh", "country": "India" },
            { "city": "Beaverton", "state": "Oregon" }
        ],
        "phone": {
            "home": "080-23456789",
            "office": "080-23456789",
            "mobile": "080-23456789",
            "mobile2": "503-23456789"
        },
        "plain": [1, 2, 3, 4]
    });

    let mut extractor = ObjectValueSetterExtractor::new(store, "Store");

    assert_json_eq!(extractor.get_value("Store.name").unwrap(), json!("Kiran"));

    extractor.set_value("Store.name", json!("Kiran Kumar"), true, false).unwrap();
    let store = extractor.get_store();

    assert_json_eq!(store["name"].clone(), json!("Kiran Kumar"));

    extractor.set_value("Store.addresses[0].city", json!("Bengaluru"), true, false).unwrap();
    assert_json_eq!(extractor.get_value("Store.addresses[0].city").unwrap(), json!("Bengaluru"));

    extractor.set_value("Store.otherAddress[1].country", json!("USA"), true, false).unwrap();
    assert_json_eq!(extractor.get_value("Store.otherAddress[1].country").unwrap(), json!("USA"));

    extractor.set_value("Store.otherSingleAddress.country", json!("USA"), true, false).unwrap();
    assert_json_eq!(extractor.get_value("Store.otherSingleAddress.country").unwrap(), json!("USA"));

    extractor.set_value("Store.plain[0]", json!("123"), true, false).unwrap();
    assert_json_eq!(extractor.get_value("Store.plain").unwrap(), json!(["123", 2, 3, 4]));

    extractor.set_value("Store.plain[0]", json!(1), false, false).unwrap();
    assert_json_eq!(extractor.get_value("Store.plain").unwrap(), json!(["123", 2, 3, 4]));

    // JS passes `undefined`; Rust has only Value::Null.
    extractor.set_value("Store.plain", Value::Null, true, true).unwrap();
    let whole = extractor.get_value("Store").unwrap();
    let keys: Vec<&str> = whole.as_object().expect("Store is an object").keys().map(|k| k.as_str()).collect();
    assert_eq!(keys, vec!["name", "addresses", "phone", "otherAddress", "otherSingleAddress"]);

    extractor.set_value("Store.plain", json!("plainString"), false, false).unwrap();
    assert_json_eq!(extractor.get_value("Store.plain").unwrap(), json!("plainString"));
}

#[tokio::test]
async fn object_value_setter_extractor_set_empty_object_then_set_nested_property_with_numeric_key() {
    let mut extractor = ObjectValueSetterExtractor::new(json!({}), "Store");

    extractor.set_value("Store.x", json!({}), true, false).unwrap();
    assert_json_eq!(extractor.get_value("Store.x").unwrap(), json!({}));

    extractor.set_value("Store.x.1", json!("kiran"), true, false).unwrap();
    assert_json_eq!(extractor.get_value("Store.x.1").unwrap(), json!("kiran"));
    assert_json_eq!(extractor.get_value("Store.x").unwrap(), json!({ "1": "kiran" }));
}

#[tokio::test]
async fn object_value_setter_extractor_set_empty_array_then_set_element_with_numeric_index() {
    let mut extractor = ObjectValueSetterExtractor::new(json!({}), "Store");

    extractor.set_value("Store.arr", json!([]), true, false).unwrap();
    assert_json_eq!(extractor.get_value("Store.arr").unwrap(), json!([]));

    extractor.set_value("Store.arr.1", json!("value"), true, false).unwrap();
    assert_json_eq!(extractor.get_value("Store.arr.1").unwrap(), json!("value"));
    let arr = extractor.get_value("Store.arr").unwrap();
    let arr = arr.as_array().expect("Store.arr is an array");
    assert_eq!(arr.len(), 2);
    assert_json_eq!(arr[1].clone(), json!("value"));
}
