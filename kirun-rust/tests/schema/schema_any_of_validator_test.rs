//! Port of validator/SchemaAnyOfValidatorTest.ts. The JS file's other two tests
//! ('complex condition with ref schema', 'filter complex condition with ref schema') are
//! commented out there and are not ported.

use std::sync::Arc;

use kirun::hybrid_repository::HybridRepository;
use kirun::json::schema::array::ArraySchemaType;
use kirun::json::schema::validator::SchemaValidator;
use kirun::json::schema::Schema;
use kirun::repository::{KIRunSchemaRepository, Repository};
use serde_json::json;

use crate::common::assert_json_eq;
use crate::{props, MapSchemaRepository};

#[tokio::test]
async fn filter_condition_with_ref_schema() {
    let filter_operator = Schema::of_string("filterOperator")
        .set_namespace("Test")
        .set_enums(vec![json!("EQUALS"), json!("LESS_THAN"), json!("GREATER_THAN"), json!("LESS_THAN_EQUAL")]);

    let filter_condition = Schema::of_object("filterCondition").set_namespace("Test").set_properties(props(vec![
        ("negate", Schema::of_boolean("negate")),
        ("filterConditionOperator", Schema::of_ref("Test.filterOperator")),
        ("field", Schema::of_string("field")),
        ("value", Schema::of_any("value")),
        ("toValue", Schema::of_any("toValue")),
        (
            "multiValue",
            Schema::of_array("multiValue", vec![])
                .set_items(ArraySchemaType::new().set_single_schema(Schema::of_any("singleType"))),
        ),
        ("isValue", Schema::of_boolean("isValue")),
        ("isToValue", Schema::of_boolean("isToValue")),
    ]));

    let repo = HybridRepository::<Schema>::new(vec![
        Arc::new(MapSchemaRepository::new(
            Some("Test"),
            vec![("filterOperator", filter_operator), ("filterCondition", filter_condition)],
            true,
        )) as Arc<dyn Repository<Schema>>,
        Arc::new(KIRunSchemaRepository::new()),
    ]);

    let temp_ob = json!({
        "field": "a.b.c.d",
        "value": "surendhar",
        "filterConditionOperator": "LESS_THAN",
        "negate": true,
        "isValue": false,
    });

    assert_json_eq!(
        SchemaValidator::validate(Some(vec![]), Some(&Schema::of_ref("Test.filterCondition")), Some(&repo), temp_ob.clone())
            .await
            .unwrap(),
        temp_ob
    );
}
