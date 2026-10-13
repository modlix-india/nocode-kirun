//! Port of validator/SchemaTypeSpellingTest.ts.

use kirun::json::schema::validator::SchemaValidator;
use kirun::json::schema::Schema;
use kirun::repository::KIRunSchemaRepository;
use serde_json::json;

// SchemaType::parse reads a type in any case (and "number" as DOUBLE), so the Schema
// meta-schema must accept the same spellings now that anyOf is enforced.
#[tokio::test]
async fn schema_accepts_type_spellings() {
    let repo = KIRunSchemaRepository::new();
    for t in ["OBJECT", "Object", "object", "Number", "number", "NUMBER"] {
        let value = json!({ "type": t, "properties": { "a": { "type": "String" } } });
        assert!(
            SchemaValidator::validate(None, Some(Schema::schema()), Some(&repo), value).await.is_ok(),
            "type {t} rejected"
        );
    }
}

#[tokio::test]
async fn schema_accepts_mixed_type_array() {
    let repo = KIRunSchemaRepository::new();
    let value = json!({ "type": ["Object", "NULL", "string"] });
    assert!(SchemaValidator::validate(None, Some(Schema::schema()), Some(&repo), value).await.is_ok());
}

#[tokio::test]
async fn schema_rejects_unknown_type() {
    let repo = KIRunSchemaRepository::new();
    let value = json!({ "type": "Thing" });
    assert!(SchemaValidator::validate(None, Some(Schema::schema()), Some(&repo), value).await.is_err());
}
