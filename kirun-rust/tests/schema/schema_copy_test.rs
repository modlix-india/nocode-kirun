//! Port of SchemaCopyTest.ts. JS `new Schema(source)` (copy constructor) is `Schema::clone`.

use kirun::json::schema::{AdditionalType, Schema};

// The Schema copy constructor used to read `not` and `contains` from the object being
// built instead of the source, so a copy lost `not` and turned `contains` into an empty schema.
#[test]
fn schema_copy_keeps_not_and_contains() {
    let source = Schema::of_array("list", vec![Schema::of_string("item")])
        .set_not(Schema::of_string("forbidden"))
        .set_contains(Schema::of_string("needed"));

    let copy = source.clone();

    assert_eq!(copy.get_not().and_then(|s| s.get_name()), Some("forbidden"));
    // JS `not.toBe`: the copy holds a different object, not the same reference.
    assert!(!std::ptr::eq(copy.get_not().unwrap(), source.get_not().unwrap()));
    assert_eq!(copy.get_contains().and_then(|s| s.get_name()), Some("needed"));
}

#[test]
fn schema_copy_keeps_a_boolean_additional_properties() {
    let source = Schema::of_object("row")
        .set_additional_properties(AdditionalType::new().set_boolean_value(false));

    let copy = source.clone();

    assert_eq!(copy.get_additional_properties().and_then(|a| a.get_boolean_value()), Some(false));
    assert!(copy.get_additional_properties().and_then(|a| a.get_schema_value()).is_none());
}
