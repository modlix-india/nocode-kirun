//! Port of validator/ArrayContainsValidatorTest.ts.

use kirun::json::schema::array::ArraySchemaType;
use kirun::json::schema::validator::ArrayValidator;
use kirun::json::schema::Schema;
use kirun::repository::KIRunSchemaRepository;
use kirun::Value;
use serde_json::json;

use crate::common::{assert_err_contains, assert_json_eq};

/// The 5-item tuple most tests use: string, integer, object, boolean, object.
fn tuple5() -> ArraySchemaType {
    ArraySchemaType::new().set_tuple_schema(vec![
        Schema::of_string("item1"),
        Schema::of_integer("item2"),
        Schema::of_object("item3"),
        Schema::of_boolean("item4"),
        Schema::of_object("item5"),
    ])
}

fn array5() -> Value {
    let obj = json!({ "val": false });
    let obj1 = json!({ "name": "mcgill" });
    json!(["jimmy", 31, obj, true, obj1])
}

#[tokio::test]
async fn schema_array_contains_test() {
    let repo = KIRunSchemaRepository::new();
    let ast = ArraySchemaType::new().set_tuple_schema(vec![
        Schema::of_string("item1"),
        Schema::of_integer("item2"),
        Schema::of_object("item3"),
    ]);

    let schema = Schema::of_array("arraySchema", vec![])
        .set_items(ast)
        .set_contains(Schema::of_object("containsS"));

    let obj = json!({ "val": false });

    let array = json!(["jimmy", 31, obj]);

    assert_json_eq!(ArrayValidator::validate(&[], &schema, Some(&repo), array.clone()).await.unwrap(), array);
}

#[tokio::test]
async fn schema_array_error_contains_test() {
    let repo = KIRunSchemaRepository::new();
    let ast = ArraySchemaType::new().set_tuple_schema(vec![
        Schema::of_string("item1"),
        Schema::of_integer("item2"),
        Schema::of_object("item3"),
    ]);

    let schema = Schema::of_array("arraySchema", vec![])
        .set_items(ast)
        .set_contains(Schema::of_boolean("containsS"));

    let obj = json!({ "val": false });

    let array = json!(["jimmy", 31, obj]);

    assert_err_contains(
        ArrayValidator::validate(&[], &schema, Some(&repo), array).await,
        "None of the items are of type contains schema",
    );
}

#[tokio::test]
async fn schema_array_min_contains_test() {
    let repo = KIRunSchemaRepository::new();
    let schema = Schema::of_array("arraySchema", vec![])
        .set_items(tuple5())
        .set_contains(Schema::of_object("containsS"))
        .set_min_contains(2);

    let array = array5();

    assert_json_eq!(ArrayValidator::validate(&[], &schema, Some(&repo), array.clone()).await.unwrap(), array);
}

#[tokio::test]
async fn schema_array_max_contains_test() {
    let repo = KIRunSchemaRepository::new();
    let schema = Schema::of_array("arraySchema", vec![])
        .set_items(tuple5())
        .set_contains(Schema::of_object("containsS"))
        .set_max_contains(3);

    let array = array5();

    assert_json_eq!(ArrayValidator::validate(&[], &schema, Some(&repo), array.clone()).await.unwrap(), array);
}

// Second JS test named 'schema array min contains test  ' (minContains 4, fails).
#[tokio::test]
async fn schema_array_min_contains_test_2() {
    let repo = KIRunSchemaRepository::new();
    let schema = Schema::of_array("arraySchema", vec![])
        .set_items(tuple5())
        .set_contains(Schema::of_object("containsS"))
        .set_min_contains(4);

    let array = array5();

    assert_err_contains(
        ArrayValidator::validate(&[], &schema, Some(&repo), array).await,
        &format!(
            "The minimum number of the items of type contains schema should be {} but found 2",
            schema.get_min_contains().unwrap()
        ),
    );
}

// Second JS test named 'schema array max contains test  ' (maxContains 1, fails).
#[tokio::test]
async fn schema_array_max_contains_test_2() {
    let repo = KIRunSchemaRepository::new();
    let schema = Schema::of_array("arraySchema", vec![])
        .set_items(tuple5())
        .set_contains(Schema::of_object("containsS"))
        .set_max_contains(1);

    let array = array5();

    assert_err_contains(
        ArrayValidator::validate(&[], &schema, Some(&repo), array).await,
        &format!(
            "The maximum number of the items of type contains schema should be {} but found 2",
            schema.get_max_contains().unwrap()
        ),
    );
}

#[tokio::test]
async fn schema_array_min_error_contains_test() {
    let repo = KIRunSchemaRepository::new();
    let schema = Schema::of_array("arraySchema", vec![])
        .set_items(tuple5())
        .set_contains(Schema::of_object("containsS"))
        .set_min_contains(4);

    let array = array5();

    assert_err_contains(
        ArrayValidator::validate(&[], &schema, Some(&repo), array).await,
        &format!(
            "The minimum number of the items of type contains schema should be {} but found 2",
            schema.get_min_contains().unwrap()
        ),
    );
}

#[tokio::test]
async fn schema_array_min_max_contains_test() {
    let repo = KIRunSchemaRepository::new();
    let schema = Schema::of_array("arraySchema", vec![])
        .set_items(tuple5())
        .set_contains(Schema::of_object("containsS"))
        .set_min_contains(1)
        .set_max_contains(3);

    let array = array5();

    assert_json_eq!(ArrayValidator::validate(&[], &schema, Some(&repo), array.clone()).await.unwrap(), array);
}

// Second JS test named 'schema array min max contains test  ' (maxContains 0, fails).
#[tokio::test]
async fn schema_array_min_max_contains_test_2() {
    let repo = KIRunSchemaRepository::new();
    let schema = Schema::of_array("arraySchema", vec![])
        .set_items(tuple5())
        .set_contains(Schema::of_object("containsS"))
        .set_min_contains(1)
        .set_max_contains(0);

    let array = array5();

    assert_err_contains(
        ArrayValidator::validate(&[], &schema, Some(&repo), array).await,
        &format!(
            "The maximum number of the items of type contains schema should be {} but found 2",
            schema.get_max_contains().unwrap()
        ),
    );
}

#[tokio::test]
async fn schema_array_min_max_contains_without_contains_test() {
    let repo = KIRunSchemaRepository::new();
    let schema = Schema::of_array("arraySchema", vec![])
        .set_items(tuple5())
        .set_min_contains(5)
        .set_max_contains(45);

    let array = array5();

    assert_json_eq!(ArrayValidator::validate(&[], &schema, Some(&repo), array.clone()).await.unwrap(), array);
}

// Second JS test named 'schema array min max contains without contains test  '.
#[tokio::test]
async fn schema_array_min_max_contains_without_contains_test_2() {
    let repo = KIRunSchemaRepository::new();
    let schema = Schema::of_array("arraySchema", vec![])
        .set_items(tuple5())
        .set_min_contains(1)
        .set_max_contains(0);

    let array = array5();

    assert_json_eq!(ArrayValidator::validate(&[], &schema, Some(&repo), array.clone()).await.unwrap(), array);
}
