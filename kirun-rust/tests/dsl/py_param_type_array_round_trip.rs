//! Port of the kirun-py cases in `tests/dsl/test_param_type_array_round_trip.py` that the JS
//! spec (`param_type_array_round_trip.rs`) does not already cover.
//!
//! Not ported: `test_parse_number_shapes` (kirun-py's `dsl/number_literal.parse_number` has no
//! JS or Rust counterpart, and its 20-digit case needs arbitrary-precision JSON numbers).

use kirun::model::{ParameterReference, ParameterReferenceType};
use kirun::Value;
use serde_json::json;

use super::param_type_array_round_trip::{refs_by_position, round_trip};

/// kirun-py `ParameterReferenceType.of(v)`, observed through `ParameterReference::from_value`
/// (the Rust port has no separate `of`).
fn type_of(t: Value) -> ParameterReferenceType {
    ParameterReference::from_value(&json!({ "key": "k", "type": t })).unwrap().get_type()
}

/// The model has to LOAD an array-typed reference before anything else matters.
mod parameter_reference_type_of {
    use super::*;

    #[test]
    fn scalar_type() {
        assert_eq!(type_of(json!("EXPRESSION")), ParameterReferenceType::Expression);
        assert_eq!(type_of(json!("VALUE")), ParameterReferenceType::Value);
    }

    #[test]
    fn single_entry_array_type() {
        assert_eq!(type_of(json!(["EXPRESSION"])), ParameterReferenceType::Expression);
        assert_eq!(type_of(json!(["VALUE"])), ParameterReferenceType::Value);
    }

    #[test]
    fn unknown_and_empty_fall_back_to_value() {
        assert_eq!(type_of(Value::Null), ParameterReferenceType::Value);
        assert_eq!(type_of(json!([])), ParameterReferenceType::Value);
        assert_eq!(type_of(json!("NONSENSE")), ParameterReferenceType::Value);
    }

    #[test]
    fn from_value_loads_an_array_typed_reference() {
        let r = ParameterReference::from_value(
            &json!({ "key": "k", "type": ["EXPRESSION"], "expression": "Page.recentNew", "order": 1 }),
        )
        .unwrap();
        assert_eq!(r.get_type(), ParameterReferenceType::Expression);
        assert_eq!(r.get_expression(), Some("Page.recentNew"));
    }

    #[test]
    fn to_json_writes_the_scalar_back() {
        let r = ParameterReference::from_value(&json!({ "key": "k", "type": ["EXPRESSION"], "expression": "A.b" }))
            .unwrap();
        assert_eq!(r.to_json()["type"], json!("EXPRESSION"));
    }
}

/// The py round-trip cases add `parameters`/`events` to the definition; otherwise the same
/// as the JS spec's two inline cases.
mod dsl_round_trip_keeps_array_typed_refs {
    use super::*;

    #[test]
    fn single_array_typed_expression() {
        let func = json!({
            "namespace": "Test",
            "name": "arrayTyped",
            "parameters": {},
            "events": {},
            "steps": {
                "fetch": {
                    "statementName": "fetch",
                    "namespace": "UIEngine",
                    "name": "FetchData",
                    "parameterMap": {
                        "url": {
                            "k1": {
                                "key": "k1",
                                "type": ["EXPRESSION"],
                                "expression": "'/api/security/applications/appCode/' + Url.pathParts[1]",
                                "value": null,
                                "order": 1
                            }
                        }
                    }
                }
            }
        });

        let refs = refs_by_position(&round_trip(&func));
        let r = &refs["fetch.url#1"];

        assert!(super::super::param_type_array_round_trip::is_expression(r));
        assert_eq!(r["expression"], json!("'/api/security/applications/appCode/' + Url.pathParts[1]"));
    }

    #[test]
    fn array_typed_values() {
        let func = json!({
            "namespace": "Test",
            "name": "arrayTypedValue",
            "parameters": {},
            "events": {},
            "steps": {
                "seed": {
                    "statementName": "seed",
                    "namespace": "UIEngine",
                    "name": "SetStore",
                    "parameterMap": {
                        "path": {
                            "k1": { "key": "k1", "type": ["VALUE"], "value": "Page.recentNew",
                                    "expression": null, "order": 1 }
                        },
                        "value": {
                            "k2": { "key": "k2", "type": ["VALUE"], "value": [],
                                    "expression": null, "order": 1 }
                        }
                    }
                }
            }
        });

        let refs = refs_by_position(&round_trip(&func));

        assert_eq!(refs["seed.path#1"]["value"], json!("Page.recentNew"));
        assert_eq!(refs["seed.value#1"]["value"], json!([]));
    }
}

/// int must not become float on the way through the DSL. Invisible in JS (one number type),
/// but serde_json keeps i64 and f64 apart, like Python.
mod number_fidelity {
    use super::*;

    fn value_round_trip(value: Value) -> Value {
        let func = json!({
            "namespace": "Test",
            "name": "numbers",
            "parameters": {},
            "events": {},
            "steps": {
                "seed": {
                    "statementName": "seed",
                    "namespace": "UIEngine",
                    "name": "SetStore",
                    "parameterMap": {
                        "value": {
                            "k": { "key": "k", "type": "VALUE", "value": value,
                                   "expression": null, "order": 1 }
                        }
                    }
                }
            }
        });
        refs_by_position(&round_trip(&func))["seed.value#1"]["value"].clone()
    }

    #[test]
    fn integer_stays_an_integer() {
        let out = value_round_trip(json!({ "order": 1, "size": 25 }));
        assert_eq!(out, json!({ "order": 1, "size": 25 }));
        assert!(out.as_object().unwrap().values().all(|v| v.is_i64() || v.is_u64()));
    }

    #[test]
    fn float_stays_a_float() {
        let out = value_round_trip(json!({ "ratio": 1.5 }));
        assert!(out["ratio"].is_f64());
        assert_eq!(out["ratio"].as_f64(), Some(1.5));
    }

    #[test]
    fn nested_integers_in_a_list_stay_integers() {
        let out = value_round_trip(json!([{ "key": "a", "order": 1 }, { "key": "b", "order": 2 }]));
        let orders: Vec<&Value> = out.as_array().unwrap().iter().map(|row| &row["order"]).collect();
        assert_eq!(orders, vec![&json!(1), &json!(2)]);
        assert!(orders.iter().all(|o| o.is_i64() || o.is_u64()));
    }
}
