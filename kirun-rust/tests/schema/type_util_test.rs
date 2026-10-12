//! Port of type/TypeUtilTest.ts.

use kirun::json::schema::r#type::{SchemaType, TypeUtil};
use serde_json::json;

#[tokio::test]
async fn type_util_test() {
    assert!(TypeUtil::from_value(&json!("OBJECT")).unwrap().unwrap().contains(SchemaType::Object));

    let t = TypeUtil::from_value(&json!(["OBJECT", "STRING"])).unwrap().unwrap();

    assert!(t.contains(SchemaType::String));
    assert!(t.contains(SchemaType::Object));
}
