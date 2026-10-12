//! Port of kirun-js `__tests__/engine/util/ErrorMessageFormatterTest.ts`.

use kirun::json::schema::array::ArraySchemaType;
use kirun::json::schema::Schema;
use kirun::util::error_message_formatter::ErrorMessageFormatter;
use kirun::{KIRunError, Value};
use serde_json::json;

mod format_value {
    use super::*;

    #[test]
    fn should_format_null() {
        assert_eq!(ErrorMessageFormatter::format_value(&Value::Null, 200), "null");
    }

    #[test]
    fn should_format_undefined() {
        // JS undefined → 'undefined'. Rust has no undefined (DESIGN.md: both are Value::Null),
        // so the closest port is that Null formats as 'null', never as 'undefined'.
        assert_eq!(ErrorMessageFormatter::format_value(&Value::Null, 200), "null");
    }

    #[test]
    fn should_format_strings_with_quotes() {
        assert_eq!(ErrorMessageFormatter::format_value(&json!("hello"), 200), "\"hello\"");
    }

    #[test]
    fn should_format_numbers() {
        assert_eq!(ErrorMessageFormatter::format_value(&json!(42), 200), "42");
        assert_eq!(ErrorMessageFormatter::format_value(&json!(3.14), 200), "3.14");
    }

    #[test]
    fn should_format_booleans() {
        assert_eq!(ErrorMessageFormatter::format_value(&json!(true), 200), "true");
        assert_eq!(ErrorMessageFormatter::format_value(&json!(false), 200), "false");
    }

    #[test]
    fn should_format_objects_as_json() {
        let obj = json!({ "name": "John", "age": 30 });
        let result = ErrorMessageFormatter::format_value(&obj, 200);
        assert!(result.contains("\"name\""));
        assert!(result.contains("\"John\""));
        assert!(result.contains("\"age\""));
        assert!(result.contains("30"));
    }

    #[test]
    fn should_format_arrays_as_json() {
        let arr = json!([1, 2, 3]);
        let result = ErrorMessageFormatter::format_value(&arr, 200);
        assert!(result.contains('['));
        assert!(result.contains('1'));
        assert!(result.contains('2'));
        assert!(result.contains('3'));
        assert!(result.contains(']'));
    }

    #[test]
    fn should_handle_circular_references() {
        // serde_json::Value is a tree and cannot hold a circular reference, so the JS
        // '[Circular]' case cannot arise. Keep the shape of the test: a self-similar object
        // still formats as plain JSON without '[Circular]'.
        let obj = json!({ "name": "test", "self": { "name": "test" } });
        let result = ErrorMessageFormatter::format_value(&obj, 200);
        assert!(!result.contains("[Circular]"));
        assert!(result.contains("\"self\""));
    }

    #[test]
    fn should_truncate_long_values() {
        let long_obj = json!({ "data": "x".repeat(300) });
        let result = ErrorMessageFormatter::format_value(&long_obj, 50);
        assert!(result.len() <= 53); // 50 + "..."
        assert!(result.contains("..."));
    }
}

mod format_function_name {
    use super::*;

    #[test]
    fn should_format_namespace_name() {
        assert_eq!(
            ErrorMessageFormatter::format_function_name("UIEngine", "SetStore"),
            "UIEngine.SetStore"
        );
    }

    #[test]
    fn should_handle_undefined_namespace() {
        // JS passes undefined; the Rust namespace is a &str, where "no namespace" is "".
        assert_eq!(ErrorMessageFormatter::format_function_name("", "loadEverything"), "loadEverything");
    }

    #[test]
    fn should_handle_null_namespace() {
        // JS passes null; the Rust namespace is a &str, where "no namespace" is "".
        assert_eq!(ErrorMessageFormatter::format_function_name("", "loadApp"), "loadApp");
    }

    #[test]
    fn should_handle_undefined_string() {
        assert_eq!(
            ErrorMessageFormatter::format_function_name("undefined", "loadEverything"),
            "loadEverything"
        );
    }
}

mod format_statement_name {
    use super::*;

    #[test]
    fn should_format_statement_name_with_quotes() {
        assert_eq!(
            ErrorMessageFormatter::format_statement_name(Some("storeString")),
            Some("'storeString'".to_string())
        );
    }

    #[test]
    fn should_return_null_for_undefined_statement_name() {
        assert_eq!(ErrorMessageFormatter::format_statement_name(None), None);
    }

    #[test]
    fn should_return_null_for_null_statement_name() {
        assert_eq!(ErrorMessageFormatter::format_statement_name(None), None);
    }

    #[test]
    fn should_return_null_for_undefined_string() {
        assert_eq!(ErrorMessageFormatter::format_statement_name(Some("undefined")), None);
    }
}

mod build_function_execution_error {
    use super::*;

    #[test]
    fn should_build_error_message_with_statement_name() {
        let result = ErrorMessageFormatter::build_function_execution_error(
            "UIEngine.SetStore",
            &Some("'storeString'".to_string()),
            "Expected an array but found {\"key\": \"value\"}",
            None,
            None,
        );
        assert_eq!(
            result,
            "Error while executing the function UIEngine.SetStore in statement 'storeString': Expected an array but found {\"key\": \"value\"}"
        );
    }

    #[test]
    fn should_build_error_message_without_statement_name() {
        let result = ErrorMessageFormatter::build_function_execution_error(
            "loadEverything",
            &None,
            "Some error occurred",
            None,
            None,
        );
        assert_eq!(result, "Error while executing the function loadEverything: Some error occurred");
    }

    #[test]
    fn should_build_error_message_with_parameter_name() {
        let result = ErrorMessageFormatter::build_function_execution_error(
            "UIEngine.SetStore",
            &Some("'storeString'".to_string()),
            "Expected an array but found {}",
            Some("value"),
            None,
        );
        assert_eq!(
            result,
            "Error while executing the function UIEngine.SetStore's parameter value in statement 'storeString': Expected an array but found {}"
        );
    }

    #[test]
    fn should_build_error_message_with_parameter_but_no_statement_name() {
        let result = ErrorMessageFormatter::build_function_execution_error(
            "loadApp",
            &None,
            "Invalid parameter",
            Some("config"),
            None,
        );
        assert_eq!(
            result,
            "Error while executing the function loadApp's parameter config: Invalid parameter"
        );
    }

    #[test]
    fn should_build_error_message_with_parameter_schema_definition() {
        let ast = ArraySchemaType::new().set_single_schema(Schema::of_integer("item"));
        let schema = Schema::of_array("value", vec![]).set_items(ast);
        let result = ErrorMessageFormatter::build_function_execution_error(
            "System.Array.Concatenate",
            &None,
            "Expected an array but found null",
            Some("secondSource"),
            Some(&schema),
        );
        assert_eq!(
            result,
            "Error while executing the function System.Array.Concatenate's parameter secondSource [Expected: Array<Integer>]: Expected an array but found null"
        );
    }

    #[test]
    fn should_wrap_nested_error_messages_to_show_execution_path() {
        // Simulate nested function calls where inner error is already formatted
        let inner_error = "Error while executing the function UIEngine.SetStore in statement 'storeString' [Expected: Array]: Expected an array but found {}";

        // Wrap it from an outer function to show call chain
        let result =
            ErrorMessageFormatter::build_function_execution_error("loadApp", &None, inner_error, None, None);

        // Should wrap the error with a newline to show the full execution path
        assert_eq!(result, format!("Error while executing the function loadApp: \n{inner_error}"));
    }
}

mod format_error_message {
    use super::*;

    #[test]
    fn should_extract_message_from_error_object() {
        let error = KIRunError::runtime("Something went wrong");
        assert_eq!(ErrorMessageFormatter::format_error_message(&error), "Something went wrong");
    }

    #[test]
    fn should_handle_string_errors() {
        assert_eq!(ErrorMessageFormatter::format_error_value(&json!("Error message")), "Error message");
    }

    #[test]
    fn should_handle_undefined_error() {
        assert_eq!(ErrorMessageFormatter::format_error_value(&Value::Null), "Unknown error");
    }

    #[test]
    fn should_handle_object_errors() {
        let error = json!({ "message": "Expected an array but found something" });
        let result = ErrorMessageFormatter::format_error_value(&error);
        assert_eq!(result, "Expected an array but found something");
    }
}
