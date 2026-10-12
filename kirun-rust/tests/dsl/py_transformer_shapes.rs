//! Port of kirun-py `tests/dsl/test_transformer_shapes.py` (no JS counterpart).
//!
//! Regression tests for stored-definition shapes that used to crash the decompiler.
//! Both shapes are legitimate and present in real saved functions; the transformers
//! assumed the narrower form and raised, failing the whole decompile.

use kirun::dsl::{DSLCompiler, SchemaTransformer};
use kirun::Value;
use serde_json::json;

/// `type` is stored as an array as often as a scalar, and may be a union.
mod schema_transformer_type_shapes {
    use super::*;

    #[test]
    fn scalar_type_still_renders_as_text() {
        assert_eq!(SchemaTransformer::to_text(&json!({ "type": "STRING" })), "STRING");
    }

    #[test]
    fn single_entry_array_type_renders_as_text() {
        assert_eq!(SchemaTransformer::to_text(&json!({ "type": ["STRING"] })), "STRING");
    }

    #[test]
    fn union_type_does_not_raise_and_falls_back_to_json() {
        // {"type": ["STRING", "NULL"]} is a nullable field. It is not a simple
        // type, so it renders as JSON rather than raising.
        let out = SchemaTransformer::to_text(&json!({ "type": ["STRING", "NULL"] }));
        assert!(out.contains("STRING") && out.contains("NULL"));
    }

    #[test]
    fn array_of_renders_through_an_array_typed_declaration() {
        assert_eq!(
            SchemaTransformer::to_text(&json!({ "type": ["ARRAY"], "items": { "type": ["INTEGER"] } })),
            "ARRAY OF INTEGER"
        );
    }

    #[test]
    fn is_simple_schema_handles_every_type_shape() {
        assert!(SchemaTransformer::is_simple_schema(&json!({ "type": "STRING" })));
        assert!(SchemaTransformer::is_simple_schema(&json!({ "type": ["STRING"] })));
        assert!(!SchemaTransformer::is_simple_schema(&json!({ "type": ["STRING", "NULL"] })));
    }
}

/// `dependentStatements` is normally a map, but is stored as a list too.
mod dependent_statements_shapes {
    use super::*;

    fn decompile(steps: Value) -> String {
        let definition = json!({
            "namespace": "Test", "name": "Fn",
            "parameters": {}, "events": {}, "steps": steps,
        });
        DSLCompiler::decompile(&definition).unwrap()
    }

    fn one_step(deps: Value) -> Value {
        json!({
            "first": {
                "statementName": "first", "namespace": "System", "name": "Print",
                "parameterMap": {},
            },
            "second": {
                "statementName": "second", "namespace": "System", "name": "Print",
                "parameterMap": {}, "dependentStatements": deps,
            },
        })
    }

    #[test]
    fn empty_list_dependent_statements_decompiles() {
        let text = decompile(one_step(json!([])));
        assert!(text.contains("second"));
        let after_second = text.split("second").nth(1).unwrap();
        assert!(!after_second.split('\n').next().unwrap().contains("AFTER"));
    }

    #[test]
    fn list_of_keys_becomes_an_after_clause() {
        let text = decompile(one_step(json!(["Steps.first.output"])));
        assert!(text.contains("AFTER Steps.first.output"));
    }

    #[test]
    fn map_form_still_works() {
        let text = decompile(one_step(json!({ "Steps.first.output": true })));
        assert!(text.contains("AFTER Steps.first.output"));
    }

    #[test]
    fn none_dependent_statements_decompiles() {
        let text = decompile(one_step(Value::Null));
        assert!(text.contains("second"));
    }
}
