//! Port of kirun-js `__tests__/engine/dsl/ParamTypeArrayRoundTrip.spec.ts`.
//!
//! A ParameterReference's `type` is a string in ParameterReference.SCHEMA (enums
//! EXPRESSION | VALUE), but a large amount of stored data carries it as a
//! single-element ARRAY, `["EXPRESSION"]`: that is what modlix-mcp and the
//! appbuilder generator tools have always written. The runtime does not care:
//! KIRuntime compares with `==`, and `['EXPRESSION'] == 'EXPRESSION'` is true in
//! JS, so those functions run correctly.
//!
//! `JSONToText.paramRefToText` compared with `===`. An array-typed reference
//! therefore missed the EXPRESSION branch, fell through to the value branch and
//! was emitted as its `value`, which for an expression ref is `null`. Compiling
//! that text back gives `{type: 'VALUE', value: null}`: the expression is gone.
//!
//! That is a DATA LOSS on a round trip that reads as a formatting operation.
//! Opening the appbuilder `workspace` page's onLoad in the DSL editor and toggling
//! to the graph editor and back destroyed 19 of its 20 expressions on 2026-09-02.
//! The one survivor is the control in the fixture below: `setApp.value` is the
//! only ref in that function whose `type` was already a plain string.

use kirun::dsl::DSLCompiler;
use kirun::{Map, Value};
use serde_json::json;

use crate::common::json_eq;

/// (step, param, order) -> ref. Never key on the ref's own key: a round trip regenerates it.
pub fn refs_by_position(func: &Value) -> Map<Value> {
    let mut out = Map::new();
    if let Some(steps) = func["steps"].as_object() {
        for (step_name, step) in steps {
            if let Some(pm) = step["parameterMap"].as_object() {
                for (param_name, refs) in pm {
                    if let Some(refs) = refs.as_object() {
                        for r in refs.values() {
                            // JS `${ref.order ?? 1}`; a JS number prints 1.0 as "1".
                            let order = match &r["order"] {
                                Value::Null => "1".to_string(),
                                Value::Number(n) => match n.as_f64() {
                                    Some(f) if f.fract() == 0.0 => format!("{}", f as i64),
                                    _ => n.to_string(),
                                },
                                other => other.to_string(),
                            };
                            out.insert(format!("{step_name}.{param_name}#{order}"), r.clone());
                        }
                    }
                }
            }
        }
    }
    out
}

pub fn is_expression(r: &Value) -> bool {
    match &r["type"] {
        Value::Array(a) => a.first().and_then(Value::as_str) == Some("EXPRESSION"),
        t => t.as_str() == Some("EXPRESSION"),
    }
}

pub fn round_trip(func: &Value) -> Value {
    DSLCompiler::compile(&DSLCompiler::decompile(func).unwrap()).unwrap()
}

pub fn on_load() -> Value {
    serde_json::from_str(include_str!("workspaceOnLoad.json")).unwrap()
}

mod dsl_round_trip_preserves_array_typed_parameter_reference_type {
    use super::*;

    #[test]
    fn keeps_the_expression_on_a_single_array_typed_reference() {
        let func = json!({
            "name": "arrayTyped",
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

        let out = round_trip(&func);
        let refs = refs_by_position(&out);
        let r = refs.get("fetch.url#1");

        assert!(r.is_some());
        let r = r.unwrap();
        assert!(is_expression(r));
        assert_eq!(
            r["expression"].as_str(),
            Some("'/api/security/applications/appCode/' + Url.pathParts[1]")
        );
    }

    #[test]
    fn keeps_an_array_typed_value_reference() {
        let func = json!({
            "name": "arrayTypedValue",
            "steps": {
                "seed": {
                    "statementName": "seed",
                    "namespace": "UIEngine",
                    "name": "SetStore",
                    "parameterMap": {
                        "path": {
                            "k1": {
                                "key": "k1",
                                "type": ["VALUE"],
                                "value": "Page.recentNew",
                                "expression": null,
                                "order": 1
                            }
                        },
                        "value": {
                            "k2": {
                                "key": "k2",
                                "type": ["VALUE"],
                                "value": [],
                                "expression": null,
                                "order": 1
                            }
                        }
                    }
                }
            }
        });

        let out = round_trip(&func);
        let refs = refs_by_position(&out);

        assert_eq!(refs["seed.path#1"]["value"], json!("Page.recentNew"));
        assert_eq!(refs["seed.value#1"]["value"], json!([]));
    }

    mod the_appbuilder_workspace_pages_on_load {
        use super::*;

        #[test]
        fn is_the_shape_this_bug_needs_42_steps_expressions_typed_as_arrays() {
            let on_load = on_load();
            let refs: Vec<Value> = refs_by_position(&on_load).into_values().collect();
            assert_eq!(on_load["steps"].as_object().unwrap().len(), 42);
            assert_eq!(refs.iter().filter(|r| r["type"].is_array()).count(), 75);
            // 19 array-typed expressions plus setApp.value, whose type is already
            // a plain string. That one is the control: it survived in production.
            assert_eq!(refs.iter().filter(|r| is_expression(r)).count(), 20);
        }

        #[test]
        fn loses_no_expression_through_decompile_compile() {
            let on_load = on_load();
            let before = refs_by_position(&on_load);
            let after = refs_by_position(&round_trip(&on_load));

            let mut lost: Vec<String> = Vec::new();
            for (position, r) in &before {
                // JS `!ref.expression`: null/missing or the empty string.
                if !is_expression(r) || r["expression"].as_str().map_or(true, str::is_empty) {
                    continue;
                }
                let now = after.get(position);
                if now.is_none_or(|now| !is_expression(now) || now["expression"] != r["expression"]) {
                    lost.push(format!("{position} ({})", r["expression"].as_str().unwrap()));
                }
            }

            assert_eq!(lost, Vec::<String>::new());
        }

        #[test]
        fn turns_no_expression_into_a_null_value_which_is_how_the_loss_presented() {
            let on_load = on_load();
            let before = refs_by_position(&on_load);
            let after = refs_by_position(&round_trip(&on_load));

            // JS `now.value === null` is false for a missing value (undefined); in Rust a
            // missing value is also Value::Null, so this check is slightly broader.
            let emptied: Vec<&String> = before
                .iter()
                .filter(|(position, r)| match after.get(*position) {
                    Some(now) => {
                        is_expression(r)
                            && !is_expression(now)
                            && now["value"].is_null()
                            && now["expression"].as_str().map_or(true, str::is_empty)
                    }
                    None => false,
                })
                .map(|(position, _)| position)
                .collect();

            assert_eq!(emptied, Vec::<&String>::new());
        }

        #[test]
        fn keeps_every_value_parameter_as_well() {
            let on_load = on_load();
            let before = refs_by_position(&on_load);
            let after = refs_by_position(&round_trip(&on_load));

            let mut changed: Vec<String> = Vec::new();
            for (position, r) in &before {
                if is_expression(r) {
                    continue;
                }
                // JS compares JSON.stringify output, where 0.0 and 0 both print "0": compare
                // numbers by value. A missing value and null are both Value::Null here.
                let now = after.get(position);
                if now.is_none_or(|now| !json_eq(&now["value"], &r["value"])) {
                    changed.push(position.clone());
                }
            }

            assert_eq!(changed, Vec::<String>::new());
        }
    }
}
