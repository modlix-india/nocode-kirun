//! Port of validator/SchemaValidatorTest.ts.

use std::sync::Arc;

use kirun::hybrid_repository::HybridRepository;
use kirun::json::schema::r#type::{SchemaType, TypeUtil};
use kirun::json::schema::validator::SchemaValidator;
use kirun::json::schema::{AdditionalType, Schema};
use kirun::repository::{KIRunSchemaRepository, Repository};
use kirun::Value;
use serde_json::json;

use crate::common::{assert_err_contains, assert_json_eq};
use crate::{match_object, props, schema_from, MapSchemaRepository};

#[tokio::test]
async fn schema_validator_test_1() {
    let repo = KIRunSchemaRepository::new();
    let schema = Schema::new().set_type(TypeUtil::of(&[SchemaType::Integer]));

    assert_json_eq!(SchemaValidator::validate(Some(vec![]), Some(&schema), Some(&repo), json!(2)).await.unwrap(), json!(2));

    let obj = json!({ "name": "shagil" });
    let obj_schema = Schema::of_object("testObj")
        .set_properties(props(vec![("name", Schema::of_string("name"))]))
        .set_required(vec!["name".to_string()]);
    assert_json_eq!(
        SchemaValidator::validate(Some(vec![]), Some(&obj_schema), Some(&repo), obj.clone()).await.unwrap(),
        obj
    );

    assert_err_contains(
        SchemaValidator::validate(Some(vec![]), Some(&obj_schema), Some(&repo), json!({ "name": 123 })).await,
        // kirun-java's wording (kirun-js: "Expected a string but found 123").
        "123 is not String",
    );

    // expect(await SchemaValidator.validate([], schema, repo, 2.5)).toThrowError(new SchemaValidationException('', '2.5 is not a number of type Integer'));
}

fn location_schema(default_value: Option<Value>, url_type: &str) -> Schema {
    let mut v = json!({
        "name": "Location",
        "namespace": "Test",
        "type": "OBJECT",
        "properties": {
            "url": { "name": "url", "type": url_type },
        },
        "required": ["url"],
    });
    if let Some(d) = default_value {
        v["defaultValue"] = d;
    }
    schema_from(v)
}

#[tokio::test]
async fn schema_validation_when_ref_of_ref() {
    let location_schema = location_schema(None, "STRING");

    let url_params_schema = Schema::of_object("UrlParameters")
        .set_namespace("Test")
        .set_additional_properties(AdditionalType::new().set_schema_value(Schema::of_ref("Test.Location")))
        .set_default_value(json!({}));

    let test_schema = Schema::of_object("TestSchema")
        .set_namespace("Test")
        .set_additional_properties(AdditionalType::new().set_schema_value(Schema::of_ref("Test.UrlParameters")))
        .set_default_value(json!({}));

    let x = MapSchemaRepository::new(
        Some("Test"),
        vec![("Location", location_schema), ("UrlParameters", url_params_schema), ("TestSchema", test_schema)],
        true,
    );

    let repo = HybridRepository::<Schema>::new(vec![
        Arc::new(x) as Arc<dyn Repository<Schema>>,
        Arc::new(KIRunSchemaRepository::new()),
    ]);
    let _obj = json!({ "obj": { "url": "http://xxxxxx.com" } });
    let query_params = json!({
        "obj": { "obj": { "url": "http://xxxxxx.com" } },
    });

    assert_json_eq!(
        SchemaValidator::validate(None, Some(&Schema::of_ref("Test.TestSchema")), Some(&repo), query_params.clone())
            .await
            .unwrap(),
        query_params
    );
}

#[tokio::test]
async fn schema_validator_test_2() {
    let location_schema = location_schema(None, "STRING");

    let repo = HybridRepository::<Schema>::new(vec![
        Arc::new(MapSchemaRepository::new(Some("Test"), vec![("Location", location_schema)], true))
            as Arc<dyn Repository<Schema>>,
        Arc::new(KIRunSchemaRepository::new()),
    ]);

    let obj = json!({ "url": "http://xxxx.com" });

    assert_json_eq!(
        SchemaValidator::validate(None, Some(&Schema::of_ref("Test.Location")), Some(&repo), obj.clone())
            .await
            .unwrap(),
        obj
    );
}

#[tokio::test]
async fn validate_null_for_of_any_schema() {
    // JS passes and expects `undefined`; both are Value::Null in Rust.
    assert_json_eq!(
        SchemaValidator::validate(None, Some(&Schema::of_any("ofanyundefined")), None, Value::Null)
            .await
            .unwrap(),
        Value::Null
    );
}

#[tokio::test]
async fn schema_validator_test_3() {
    let obj = json!({ "url": "http://xxxx.com" });

    let location_schema = location_schema(Some(obj.clone()), "String");

    let repo = HybridRepository::<Schema>::new(vec![
        Arc::new(MapSchemaRepository::new(Some("Test"), vec![("Location", location_schema)], true))
            as Arc<dyn Repository<Schema>>,
        Arc::new(KIRunSchemaRepository::new()),
    ]);

    let obj1 = json!({ "url": "http://yyyy.com" });

    let result = SchemaValidator::validate(
        None,
        Some(&Schema::of_ref("Test.Location").set_default_value(obj1.clone())),
        Some(&repo),
        Value::Null,
    )
    .await
    .unwrap();
    assert!(match_object(&result, &obj1), "{result} does not match {obj1}");
}
