//! Port of kirun-js `__tests__/engine/function/system/string/StringFunctionRepoTest2.ts`.
//! The JS source contains literal tab characters; they are written as `\t` here.

use kirun::function::system::string::string_function_repository::StringFunctionRepository;
use kirun::namespaces::Namespaces;
use kirun::repository::Repository;
use serde_json::json;

use crate::common::*;

/// JS `'{20934 123 1[[23 245-0 34\\\\\\" 3434 \\\\\\" 123]]}'` (three backslashes then a quote).
const BRACKETED: &str = r#"{20934 123 1[[23 245-0 34\\\" 3434 \\\" 123]]}"#;

async fn run(name: &str, string: &str, search_string: &str) -> kirun::Value {
    let fun = StringFunctionRepository::new()
        .find(Namespaces::STRING, name)
        .await
        .expect("Function not available");
    let fep = fep().set_arguments(args! { "string" => string, "searchString" => search_string });
    first_value(fun.as_ref(), fep, "result").await.unwrap()
}

#[tokio::test]
async fn string_repo_contains() {
    assert_json_eq!(run("Contains", "\t\t\tno code  Kirun  PLATform\t\t", "no code").await, json!(true));
    assert_json_eq!(run("Contains", "   ", " ").await, json!(true));
    assert_json_eq!(run("Contains", BRACKETED, "4 123 1[[23 245-0 34").await, json!(true));
    assert_json_eq!(run("Contains", BRACKETED, "2093(.*)").await, json!(false));
}

#[tokio::test]
async fn string_function_repo_2() {
    assert_json_eq!(
        run("EndsWith", "\t\t\tno code  Kirun  PLATform\t\t", "PLATform\t\t").await,
        json!(true)
    );
    assert_json_eq!(run("EndsWith", "this is a new job\t", "job\t").await, json!(true));
    assert_json_eq!(run("EndsWith", BRACKETED, "\" 123]]}").await, json!(true));
}

#[tokio::test]
async fn string_function_repo_3() {
    assert_json_eq!(run("EndsWith", BRACKETED, "]]20934}").await, json!(false));
}
