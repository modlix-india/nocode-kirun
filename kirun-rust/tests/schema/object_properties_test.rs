//! Port of validator/ObjectPropertiesTest.ts.

use kirun::json::schema::r#type::SchemaType;
use kirun::json::schema::validator::SchemaValidator;
use kirun::repository::KIRunSchemaRepository;
use serde_json::json;

use crate::common::assert_json_eq;
use crate::schema_from;

#[tokio::test]
async fn schema_object_validator_test_schema_based_old_array_style() {
    let repo = KIRunSchemaRepository::new();
    let schema = schema_from(json!({
        "type": "OBJECT",
        "properties": { "name": { "type": "STRING" } },
        "additionalProperties": { "schemaValue": { "type": "ARRAY" } },
    }));
    assert!(schema.get_type().unwrap().contains(SchemaType::Object));

    let obj = json!({ "name": "Kiran", "num": [1, 2, 3] });
    assert_json_eq!(
        SchemaValidator::validate(Some(vec![]), Some(&schema), Some(&repo), obj.clone()).await.unwrap(),
        obj
    );

    assert!(SchemaValidator::validate(
        Some(vec![]),
        Some(&schema),
        Some(&repo),
        json!({ "name": "Kiran", "num": 23, "lastName": "grandhi" }),
    )
    .await
    .is_err());
}
