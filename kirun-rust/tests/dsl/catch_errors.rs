//! `CATCH ERRORS` (kirun-rust only): the step's `catchErrors`, compiled and printed back. The
//! two words reserve nothing: a step named CATCH still parses.

use kirun::dsl::DSLCompiler;
use serde_json::json;

const SOURCE: &str = r#"FUNCTION Catching
    NAMESPACE Test
    LOGIC
        call: Test.Child(value = 1) AFTER Steps.start.output CATCH ERRORS /* the job */
        start: System.Make(resultShape = {})
        CATCH: System.Make(resultShape = {}) AFTER Steps.call.error
        plain: System.Make(resultShape = {}) AFTER Steps.CATCH.output"#;

#[test]
fn compiles_to_the_property_and_prints_back() {
    let json = DSLCompiler::compile(SOURCE).unwrap();
    let steps = &json["steps"];
    assert_eq!(steps["call"]["catchErrors"], json!(true));
    assert_eq!(steps["call"]["comment"], json!("the job"));
    assert_eq!(steps["call"]["dependentStatements"], json!({"Steps.start.output": true}));
    for other in ["start", "CATCH", "plain"] {
        assert!(steps[other].get("catchErrors").is_none(), "{other}");
    }
    assert_eq!(steps["CATCH"]["dependentStatements"], json!({"Steps.call.error": true}));

    let printed = DSLCompiler::decompile(&json).unwrap();
    assert!(printed.contains("AFTER Steps.start.output CATCH ERRORS /* the job */"), "{printed}");
    assert_eq!(printed.matches("CATCH ERRORS").count(), 1, "{printed}");
    // Parameter keys are new on every compile, so the round trip is checked on the text.
    let again = DSLCompiler::compile(&printed).unwrap();
    assert_eq!(again["steps"]["call"]["catchErrors"], json!(true));
    assert_eq!(DSLCompiler::decompile(&again).unwrap(), printed, "round trip");
}
